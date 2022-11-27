package com.example.habiband.ui.bootloader

import android.annotation.SuppressLint
import android.bluetooth.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.habiband.BLEServices
import com.example.habiband.bluetooth.Connection
import com.example.habiband.bluetooth.interfaces.IConnectionEventListener
import com.example.habiband.bluetooth.interfaces.IRequestConnect
import com.example.habiband.bootloader.Firmware
import com.example.habiband.bootloader.requests.*
import com.example.habiband.bootloader.responses.ResponseGetSessionKey
import com.example.habiband.bootloader.responses.ResponseHeader
import com.example.habiband.bootloader.types.OperationResults
import com.example.habiband.bootloader.types.Operations
import com.example.habiband.bootloader.types.Updates
import com.example.habiband.bootloader.updates.UpdateBootloaderStatus
import com.example.habiband.bootloader.updates.UpdateFirmwareLoadingStatus
import java.nio.ByteBuffer
import java.nio.ByteOrder

class BootloaderViewModel : ViewModel(), IConnectionEventListener
{
    private enum class ThreadState
    {
        ThreadStopped,
        ThreadStarting,
        ThreadRun,
        ThreadStopping,
    }

    private enum class UpdateState
    {
        Idle,
        RequestLoad,
        PrepareService,
        InitializationLoadFirmware,
        WaiteInitializationLoadFirmware,
        LoadFirmware,
        ConfirmationLoadFirmware,
        Complete,
        ErrorGetCharacteristic,
        ErrorNoData
    }

    private enum class LoadFirmwareInit
    {
        Idle,
        NotificationRxAccepted,
        NotificationStatusAccepted,
        MtuAccepted
    }

    private enum class BootState
    {
        Idle,
        RequestStarting,
        Starting,
    }

    private enum class SessionKeyInit
    {
        Idle,
        Request,
    }

    private var updateState: UpdateState = UpdateState.Idle

    var bootloaderStatus = UpdateBootloaderStatus()
    private val _bootloaderStatus = MutableLiveData<UpdateBootloaderStatus>().apply { value = bootloaderStatus }
    var observeBootloaderStatus: LiveData<UpdateBootloaderStatus> = _bootloaderStatus

    var firmwareLoadingStatus = UpdateFirmwareLoadingStatus()
    private val _firmwareLoadingStatus = MutableLiveData<UpdateFirmwareLoadingStatus>().apply { value = firmwareLoadingStatus }
    var observeFirmwareLoadingStatus: LiveData<UpdateFirmwareLoadingStatus> = _firmwareLoadingStatus

    private val _sessionKey = MutableLiveData<String>().apply { value = "not available" }
    var observeSessionKey: LiveData<String> = _sessionKey

    var sessionKey = -1
    var sessionAccepted = false

    private var requestStartApp = false
    private var requestStartBoot = BootState.Idle

    private var device: BluetoothDevice? = null
    private var responseDelay = 0

    private var threadState = ThreadState.ThreadStopped
    private var requestConnect: IRequestConnect? = null

    private var appStart = OperationResults.Undefined

    private val _text = MutableLiveData<String>().apply {
        value = "This is bootloader Fragment"
    }

    private var observableGatt = MutableLiveData<BluetoothGatt>()

    init
    {
        observableGatt = MutableLiveData<BluetoothGatt>()
    }

    val text: LiveData<String> = _text

    fun notificationUpdateStateEnable()
    {

        Connection.addNotificationRequest(BLEServices.Bootloader.Characteristics.PORT_TX, true)
        Connection.addNotificationRequest(BLEServices.Bootloader.Characteristics.STATUS, true)
    }

    fun bindModel(requestConnect: IRequestConnect?)
    {
        this.requestConnect = requestConnect
    }

    fun startApp()
    {
        Connection.characteristicWrite(BLEServices.Bootloader.Characteristics.PORT_RX, RequestStartApp().data)
    }

    fun startBoot()
    {
        requestStartBoot = BootState.RequestStarting

        try
        {
            if (threadState == ThreadState.ThreadStopped)
            {
                threadState = ThreadState.ThreadStarting
                characteristicWriteHandlerThread.start()
            }

            requestStartBoot = BootState.RequestStarting
        }
        catch (e: NumberFormatException)
        {

        }
    }

    fun startUpdate()
    {
        try
        {
            if (threadState == ThreadState.ThreadStopped)
            {
                threadState = ThreadState.ThreadStarting
                characteristicWriteHandlerThread.start()
            }

            updateState = UpdateState.RequestLoad
        }
        catch (e: NumberFormatException)
        {

        }
    }

    @SuppressLint("MissingPermission")
    private val characteristicWriteHandlerThread = Thread(Runnable {
        var rx: BluetoothGattCharacteristic? = null
        var data: ByteArray ?= null
        var byteTransmitted = 0
        var notificationRxEnabled = false
        var notificationStatusEnabled = false
        var dfuIsSet = false

        threadState = ThreadState.ThreadRun

        while (true)
        {
            if (threadState == ThreadState.ThreadStopping)
            {
                threadState = ThreadState.ThreadStopped
                return@Runnable
            }

            Thread.sleep(10)

            if (responseDelay > 0)
            {
                responseDelay--
                continue
            }

            if (Connection.connectedGatt != null)
            {
                if (rx == null)
                {
                    rx = Connection.findCharacteristic(BLEServices.Bootloader.Characteristics.PORT_RX)
                    responseDelay = 10
                    continue
                }

                if (!notificationRxEnabled)
                {
                    notificationRxEnabled = Connection.setNotification(BLEServices.Bootloader.Characteristics.PORT_TX, true)
                    responseDelay = 10
                    continue
                }

                if (!notificationStatusEnabled)
                {
                    notificationStatusEnabled = Connection.setNotification(BLEServices.Bootloader.Characteristics.STATUS, true)
                    responseDelay = 10
                    continue
                }

                if(!dfuIsSet)
                {
                    dfuIsSet = Connection.connectedGatt?.requestMtu(200) == true
                    responseDelay = 100
                    continue
                }

                if (sessionKey == -1)
                {
                    Connection.characteristicWrite(rx, RequestGetSessionKey().data)
                    responseDelay = 30
                    sessionAccepted = false
                    continue
                }

                if (!sessionAccepted)
                {
                    Connection.characteristicWrite(rx, RequestConfirmSessionKey(sessionKey).data)
                    responseDelay = 10
                    continue
                }

                if(requestStartBoot == BootState.RequestStarting)
                {
                    rx = null
                    notificationRxEnabled = false
                    notificationStatusEnabled = false
                    dfuIsSet = false
                    sessionAccepted = false

                    requestStartBoot = BootState.Starting
                    continue
                }

                if (requestStartBoot == BootState.Starting)
                {
                    if (Connection.characteristicWrite(BLEServices.Bootloader.Characteristics.PORT_RX, RequestStartBoot(sessionKey).data))
                    {
                        requestStartBoot = BootState.Idle
                    }
                    continue
                }
            }

            if (updateState == UpdateState.Idle)
            {
                continue
            }

            if (Connection.connectedGatt == null)
            {
                rx = null
                byteTransmitted = 0
                notificationRxEnabled = false
                notificationStatusEnabled = false
                dfuIsSet = false
                sessionAccepted = false

                requestConnect?.Connect()
                responseDelay = 200
                continue
            }

            if (updateState == UpdateState.RequestLoad)
            {
                if (Firmware.size == 0)
                {
                    updateState = UpdateState.Idle
                }

                notificationRxEnabled = false
                notificationStatusEnabled = false
                dfuIsSet = false
                rx = null

                data = Firmware.file.toByteArray()
                byteTransmitted = 0

                updateState = UpdateState.PrepareService
            }

            if (bootloaderStatus.operationInProgress && updateState != UpdateState.LoadFirmware)
            {
                responseDelay = 10
                continue
            }

            if (updateState == UpdateState.PrepareService)
            {

                updateState = UpdateState.InitializationLoadFirmware
            }

            if (!bootloaderStatus.sessionKeyConfirmed)
            {
                sessionKey = -1
                continue
            }

            if (updateState == UpdateState.InitializationLoadFirmware)
            {
                if (!bootloaderStatus.firmwareLoadingInit)
                {
                    Connection.characteristicWrite(rx, RequestInitLoadFirmware(sessionKey, Firmware.size, Firmware.crc).data)
                    dfuIsSet = false
                    responseDelay = 200
                    continue
                }

                responseDelay = 5
                updateState = UpdateState.LoadFirmware
                continue
            }

            if (updateState == UpdateState.LoadFirmware)
            {
                if (byteTransmitted < Firmware.size)
                {
                    var packetSize = Firmware.size - byteTransmitted

                    if (packetSize > RequestLoad.maximumPacketSize)
                    {
                        packetSize = RequestLoad.maximumPacketSize
                    }

                    if (Connection.characteristicWrite(rx, RequestLoad(data!!, byteTransmitted, packetSize).data))
                    {
                        byteTransmitted += packetSize
                    }
                    responseDelay = 5
                    continue
                }

                updateState = UpdateState.ConfirmationLoadFirmware
            }

            if (updateState == UpdateState.ConfirmationLoadFirmware)
            {
                if (!Connection.characteristicWrite(rx, RequestConfirmationLoadFirmware(sessionKey).data))
                {
                    responseDelay = 100
                    continue
                }

                updateState = UpdateState.Idle
            }
        }
    })

    private fun portTxCharacteristicChangedHandler(data: ByteArray)
    {
        val key = ByteBuffer.wrap(data, 0, UShort.SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN).short.toInt()

        if ((key and Updates.flag) > 0)
        {
            when(Updates.fromByteArray(data, 0))
            {
                Updates.LoadingFirmware ->
                {
                    firmwareLoadingStatus.receiveData(data, Updates.SIZE_BYTES)
                    _firmwareLoadingStatus.postValue(firmwareLoadingStatus)
                }
                else -> return
            }
        }
        else
        {
            val response = ResponseHeader()
            response.receive(data, 0)

            when(response.operation)
            {
                Operations.StartApp ->
                    appStart = response.result

                Operations.GetSessionKey ->
                    if (response.result == OperationResults.Accept)
                    {
                        val result = ResponseGetSessionKey()
                        if (result.receive(data, ResponseHeader.SIZE_BYTES))
                        {
                            sessionKey = result.value
                            _sessionKey.postValue(sessionKey.toString())
                        }
                    }

                Operations.ConfirmSessionKey ->
                    if (response.result == OperationResults.Accept)
                    {
                        sessionAccepted = true
                    }
                    else
                    {
                        sessionKey = -1
                    }

                else -> return
            }
        }
    }

    private fun statusCharacteristicChangedHandler(data: ByteArray)
    {
        bootloaderStatus.receiveData(data, 0)
        _bootloaderStatus.postValue(bootloaderStatus)
    }

    override fun servicesDiscovered(connection: Connection)
    {
        notificationUpdateStateEnable()
    }

    override fun connectionStateChanged(connection: Connection)
    {

    }

    override fun characteristicChanged(connection: Connection, characteristic: BluetoothGattCharacteristic?)
    {
        if (characteristic != null)
        {
            when(characteristic.uuid.toString())
            {
                BLEServices.Bootloader.Characteristics.STATUS -> statusCharacteristicChangedHandler(characteristic.value)
                BLEServices.Bootloader.Characteristics.PORT_TX -> portTxCharacteristicChangedHandler(characteristic.value)
            }
        }
    }

    override fun characteristicWrite(connection: Connection, characteristic: BluetoothGattCharacteristic?)
    {

    }
}