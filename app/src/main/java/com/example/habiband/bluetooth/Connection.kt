package com.example.habiband.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.example.habiband.bluetooth.Scanner.UUID_CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR
import com.example.habiband.bluetooth.interfaces.IConnectionEventListener
import com.example.habiband.bootloader.Firmware
import com.example.habiband.bootloader.requests.RequestInitLoadFirmware
import kotlin.math.truncate

@SuppressLint("MissingPermission")
object Connection {
    //private var context: Context? = null
    private var notificationRequests: ArrayList<NotificationRequest> = ArrayList<NotificationRequest>()
    private var characteristicWriteRequests: ArrayList<CharacteristicWriteRequest> = ArrayList<CharacteristicWriteRequest>()
    private var isInit = false

    private var eventListener: IConnectionEventListener? = null
    private var gatt: BluetoothGatt? = null
    var connectedGatt: BluetoothGatt? = null
    var device: BluetoothDevice? = null
    var state: State = State.Undefined
    var isWrite = 0

    fun setState(value: Int)
    {
        val lastState = state
        state = when (value)
        {
            BluetoothGatt.STATE_CONNECTING -> State.Connecting
            BluetoothGatt.STATE_CONNECTED -> State.Connected
            BluetoothGatt.STATE_DISCONNECTING -> State.Disconnecting
            BluetoothGatt.STATE_DISCONNECTED -> State.Disconnected
            else -> State.Undefined
        }

        if (lastState != state)
        {
            eventListener?.connectionStateChanged(this@Connection)
        }
    }

    fun setEventListener(eventListener: IConnectionEventListener?)
    {
        this.eventListener = eventListener
    }

    private val gattCallback = object : BluetoothGattCallback()
    {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int)
        {
            when (newState)
            {
                BluetoothProfile.STATE_CONNECTED ->
                {
                    setState(newState)
                    gatt.discoverServices()
                    connectedGatt = gatt
                    Scanner.connectedGatt = gatt
                    Scanner.connectedDevices.add(gatt)
                }

                BluetoothProfile.STATE_DISCONNECTED ->
                {
                    setState(newState)
                    connectedGatt = null
                    Scanner.connectedGatt = null
                    Scanner.connectedDevices.remove(gatt)
                }

                BluetoothProfile.STATE_CONNECTING ->
                {
                    setState(newState)
                }

                BluetoothProfile.STATE_DISCONNECTING ->
                {
                    setState(newState)
                    Scanner.connectedGatt = null
                    close()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int)
        {
            when (status)
            {
                BluetoothGatt.GATT_SUCCESS -> eventListener?.servicesDiscovered(this@Connection)
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic? )
        {
            eventListener?.characteristicChanged(this@Connection, characteristic)
        }
    }

    fun characteristicWrite(characteristic: BluetoothGattCharacteristic?, data: ByteArray?): Boolean
    {
        if (connectedGatt != null && characteristic != null && data != null)
        {
            connectedGatt?.apply {
                if (characteristic.setValue(data))
                {
                    characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    return connectedGatt?.writeCharacteristic(characteristic) == true
                }
                else
                {
                    return false
                }
            }
        }
        return false
    }

    fun characteristicWrite(uuid: String?, data: ByteArray?): Boolean
    {
        return characteristicWrite(findCharacteristic(uuid), data)
    }

    fun findCharacteristic(uuid: String?): BluetoothGattCharacteristic?
    {
        if (connectedGatt != null && connectedGatt?.services != null && uuid != null)
        {
            for (service in connectedGatt?.services!!)
            {
                for (characteristic in service.characteristics)
                {
                    if(characteristic.uuid.toString() == uuid)
                    {
                        return characteristic
                    }
                }
            }
        }
        return null
    }

    fun setNotification(characteristic: BluetoothGattCharacteristic?, state: Boolean): Boolean
    {
        if (connectedGatt != null && characteristic != null)
        {
            connectedGatt?.apply {
                var result = connectedGatt?.setCharacteristicNotification(characteristic, state) == true

                val descriptor = characteristic.getDescriptor(UUID_CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR)

                if (descriptor != null)
                {
                    if (state) { descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE }
                    else { descriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE }

                    result = connectedGatt?.writeDescriptor(descriptor) == true
                }
            }

            return true
        }
        return false
    }

    fun setNotification(uuid: String?, state: Boolean): Boolean
    {
        return setNotification(findCharacteristic(uuid), state)
    }

    fun bindContext(context: Context)
    {

    }

    fun broadcastUpdate(context: Context, action: String)
    {
        val intent = Intent(action)
        context.sendBroadcast(intent)
    }

    fun registerReceiver(context: Context, gattUpdateReceiver: BroadcastReceiver)
    {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Scanner.ACTION_GATT_CONNECTED)
        intentFilter.addAction(Scanner.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(Scanner.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(Scanner.ACTION_DATA_AVAILABLE)
        context.registerReceiver(gattUpdateReceiver, intentFilter)
    }

    fun unregisterReceiver(context: Context, gattUpdateReceiver: BroadcastReceiver)
    {
        context.unregisterReceiver(gattUpdateReceiver)
    }


    fun open(device: BluetoothDevice?, context: Context)
    {
        if (device != null)
        {
            /*
            if (gatt != null)
            {
                if (device.address == this.device?.address)
                {
                    if (state != State.Connected)
                    {
                        gatt?.connect()
                        setState(gatt?.getConnectionState(device)!!)
                    }
                    return
                }
                gatt?.disconnect()
            }
*/
            this.device = device

            gatt = device.connectGatt(context, false, gattCallback)
        }
    }

    fun close()
    {
        gatt?.close()
        gatt = null
        connectedGatt = null
    }

    fun dispose()
    {
        close()
        this.device = null
        connectedGatt = null
    }

    fun updateServices()
    {
        gatt?.discoverServices()
    }

    class NotificationRequest(val gatt: BluetoothGatt,
                              val characteristic: BluetoothGattCharacteristic,
                              val state: Boolean)
    {

        fun set(): Boolean
        {
            connectedGatt?.apply {
                setCharacteristicNotification(characteristic, state)

                val descriptor = characteristic.getDescriptor(UUID_CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR)

                if (descriptor != null)
                {
                    if (state){ descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE }
                    else{ descriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE }
                    gatt.writeDescriptor(descriptor)

                    return true
                }
            }
            return false
        }
    }

    class CharacteristicWriteRequest(val gatt: BluetoothGatt,
                                     val characteristic: BluetoothGattCharacteristic,
                                     val data: ByteArray)
    {
        private var tryCount = 10
        fun write(): Boolean
        {
            gatt.apply {
                characteristic.value = data
                var result = gatt.writeCharacteristic(characteristic)
                while(!result && tryCount > 0)
                {
                    Thread.sleep(100)
                    result = gatt.writeCharacteristic(characteristic)
                    tryCount--
                }
                return result
            }
        }
    }

    fun notificationControlRun()
    {
        if (!isInit)
        {
            isInit = true
            notificationHandlerThread.start()
            //characteristicWriteHandlerThread.start()
        }
    }

    fun notificationControlStop()
    {
        notificationHandlerThread.interrupt()
        characteristicWriteHandlerThread.interrupt()
    }

    private val characteristicWriteHandlerThread = Thread(Runnable {
        while (true)
        {
            var element: CharacteristicWriteRequest? = null

            synchronized(characteristicWriteRequests)
            {
                if (characteristicWriteRequests.size > 0)
                {
                    element = characteristicWriteRequests[0]
                    characteristicWriteRequests.removeAt(0)
                }
            }

            if (element != null)
            {
                if (element?.write() == true)
                {
                    Thread.sleep(100)
                }
                else
                {
                    Thread.sleep(100)
                }
            }

            Thread.sleep(10)
        }
    })

    private val notificationHandlerThread = Thread(Runnable {
        while (true)
        {
            var element: NotificationRequest? = null

            synchronized(notificationRequests)
            {
                if (notificationRequests.size > 0)
                {
                    element = notificationRequests[0]
                    notificationRequests.removeAt(0)
                }
            }

            element?.set()
            Thread.sleep(150)
        }
    })

    fun addNotificationRequest(uuid: String?, state: Boolean): Boolean
    {
        if(connectedGatt != null && uuid != null)
        {
            val characteristic = findCharacteristic(uuid)
            if (characteristic != null)
            {
                synchronized(notificationRequests)
                {
                    notificationRequests.add(NotificationRequest(connectedGatt!!, characteristic, state))
                }
                return true
            }
        }
        return false
    }

    fun characteristicWriteRequest(characteristic: BluetoothGattCharacteristic?, data: ByteArray?): Boolean
    {
        if (connectedGatt != null && characteristic != null && data != null)
        {
            synchronized(characteristicWriteRequests)
            {
                return characteristicWriteRequests.add(CharacteristicWriteRequest(connectedGatt!!, characteristic, data))
            }
        }
        return false
    }
/*
    fun characteristicWriteRequest(uuid: String?, data: ByteArray?): Boolean
    {
        if (connectedGatt != null && uuid != null && data != null)
        {
            val characteristic = findCharacteristic(uuid)
            if (characteristic != null)
            {
                synchronized(characteristicWriteRequests)
                {
                    return characteristicWriteRequests.add(CharacteristicWriteRequest(connectedGatt!!, characteristic, data))
                }
            }
        }
        return false
    }
*/
    fun clearNotificationRequests()
    {
        synchronized(notificationRequests)
        {
            notificationRequests.clear()
        }
    }

    fun clearCharacteristicWriteRequests()
    {
        synchronized(characteristicWriteRequests)
        {
            characteristicWriteRequests.clear()
        }
    }

    enum class State
    {
        Undefined,
        Disconnected,
        Disconnecting,
        Connecting,
        Connected,
    }
}