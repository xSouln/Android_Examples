package com.example.habiband.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.util.Log
import android.widget.Toast
import com.example.habiband.bluetooth.Control.UUID_CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR
import com.example.habiband.bluetooth.interfaces.IConnectionEventListener
import com.example.habiband.ui.notifications.NotificationsViewModel

@SuppressLint("MissingPermission")
class Connection(private var context: Context) {
    //private var context: Context? = null
    private var eventListener: IConnectionEventListener? = null
    var gatt: BluetoothGatt? = null
    var device: BluetoothDevice? = null
    private var status: Status = Status.Disconnected

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): Connection {
            return this@Connection
        }
    }

    init {
        //registerReceiver()
    }

    fun setStatus(value: Status){
        status = value
        eventListener?.connectionStateChanged(this@Connection)
    }

    fun getStatus(): Status{
        return status
    }

    fun setEventListener(eventListener: IConnectionEventListener){
        this.eventListener = eventListener
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        context.sendBroadcast(intent)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            val intentAction: String
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    intentAction = Control.ACTION_GATT_CONNECTED
                    broadcastUpdate(intentAction)
                    setStatus(Status.Connected)
                    gatt.discoverServices()
                    Control.connectedDevices.add(gatt)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    intentAction = Control.ACTION_GATT_DISCONNECTED
                    setStatus(Status.Disconnected)
                    broadcastUpdate(intentAction)
                    Control.connectedDevices.remove(gatt)
                }
                BluetoothProfile.STATE_CONNECTING ->{
                    intentAction = Control.ACTION_GATT_CONNECTING
                    setStatus(Status.Connecting)
                    broadcastUpdate(intentAction)
                }
                BluetoothProfile.STATE_DISCONNECTING ->{
                    intentAction = Control.ACTION_GATT_DISCONNECTING
                    close()
                    broadcastUpdate(intentAction)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    eventListener?.servicesDiscovered(this@Connection)
                    broadcastUpdate(Control.ACTION_GATT_SERVICES_DISCOVERED)
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            /*
            characteristic?.apply {
                when (uuid) {
                    Control.UUID_RX_DATA_CHARACTERISTIC -> {
                        //bluetoothDataReceiveEventListener?.onDataReceive(value)
                    }
                }
                eventListener?.characteristicChanged(this@Connection, characteristic)
            }
            */
            eventListener?.characteristicChanged(this@Connection, characteristic)

            //val intentAction = Control.ACTION_DATA_AVAILABLE
            //broadcastUpdate(intentAction)
        }
    }

    fun registerReceiver(gattUpdateReceiver: BroadcastReceiver) {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Control.ACTION_GATT_CONNECTED)
        intentFilter.addAction(Control.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(Control.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(Control.ACTION_DATA_AVAILABLE)
        context.registerReceiver(gattUpdateReceiver, intentFilter)
    }

    companion object{

    }

    fun findCharacteristic(uuid: String): BluetoothGattCharacteristic? {
        if (gatt != null && gatt?.services != null){
            for (service in gatt?.services!!){
                for (characteristic in service.characteristics){
                    if(characteristic.uuid.toString() == uuid){
                        return characteristic
                    }
                }
            }
        }
        return null
    }

    fun setNotification(characteristic: BluetoothGattCharacteristic?){
        if (gatt != null && characteristic != null){
            gatt?.apply {
                setCharacteristicNotification(characteristic, true)
                val descriptor = characteristic.getDescriptor(UUID_CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR)
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt?.writeDescriptor(descriptor)
            }
        }
    }

    fun unregisterReceiver(gattUpdateReceiver: BroadcastReceiver) {
        context.unregisterReceiver(gattUpdateReceiver)
    }

    fun open(device: BluetoothDevice?){
        if (device != null){
            if (gatt != null){
                if (device.address == this.device?.address) {
                    if (getStatus() != Status.Connected)
                    {
                        gatt!!.connect()
                    }
                    return
                }
                gatt!!.disconnect()
            }

            this.device = device

            gatt = device.connectGatt(context, false, gattCallback)
        }
    }

    fun close(){
        gatt?.close()
        gatt = null
        setStatus(Status.Disconnected)
    }

    fun dispose(){
        close()
        this.device = null
    }

     enum class Status{
        Disconnected,
        Disconnecting,
        Connecting,
        Connected,
    }
}