package com.example.habiband.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.CountDownTimer
import com.example.habiband.bluetooth.interfaces.IConnectionEventListener
import com.example.habiband.bluetooth.interfaces.IControlEventListener
import java.util.*

@SuppressLint("MissingPermission")
object Control {
    private const val SCAN_DEVICES_PERIOD: Long = 5000

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var discoveredDevices: ArrayList<BluetoothDevice> = ArrayList<BluetoothDevice>()
    private var bluetoothManager: BluetoothManager? = null

    var count: Long = 0
    var selectedDevice: BluetoothDevice? = null
    var connectedDevices: ArrayList<BluetoothGatt> = ArrayList<BluetoothGatt>()

    private var eventListener: IControlEventListener? = null

    fun start(context: Context)
    {
        registerReceiver(context)
        setManager(context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
    }

    init {

    }

    fun getDiscoveredDevices(): ArrayList<BluetoothDevice> {
        return discoveredDevices
    }

    fun setEventListener(listener: IControlEventListener?){
        eventListener = listener
    }

    private fun addDevice(device: BluetoothDevice){
        if (device.bondState == BluetoothDevice.BOND_BONDED){
            return;
        }

        for (discoveredDevice in discoveredDevices) {
            if (discoveredDevice.address == device.address){
                return
            }
        }

        discoveredDevices.add(device)
        eventListener?.discoveredDevicesChanged()
    }

    private val discoverReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (BluetoothDevice.ACTION_FOUND == intent.action) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                device?.apply {
                    addDevice(this)
                }
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == intent.action) {
                eventListener?.discoveredDevicesChanged()
            }
        }
    }

    fun setManager(manager: BluetoothManager){
        bluetoothManager = manager
        bluetoothAdapter = bluetoothManager!!.adapter
    }

    private fun registerReceiver(context: Context) {
        context.registerReceiver(discoverReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        context.registerReceiver(discoverReceiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
    }

    fun dispose(){
        timer.cancel()
        timer.onFinish()
        //connection?.dispose()
    }

    fun selectDevice(selectedDevice: BluetoothDevice){
        this.selectedDevice = selectedDevice
    }

    private val timer = object: CountDownTimer(Long.MAX_VALUE, 1000) {
        override fun onTick(millisUntilFinished: Long){
            if (bluetoothAdapter?.isDiscovering != true){
                count++
                eventListener?.updateCount(count)
                bluetoothAdapter?.startDiscovery()
            }
        }

        override fun onFinish() {

        }
    }

    fun startDiscoverDevices(){
        timer.start()
    }

    fun stopDiscoverDevices(){
        timer.cancel()
        bluetoothAdapter?.cancelDiscovery()
    }

    fun resetDiscoverDevices(){
        discoveredDevices.clear()
        eventListener?.discoveredDevicesChanged()
    }

    private const val STATE_DISCONNECTED = 0
    private const val STATE_CONNECTING = 1
    private const val STATE_CONNECTED = 2
    const val ACTION_GATT_CONNECTED = "com.habiband.service.bluetooth.ACTION_GATT_CONNECTED"
    const val ACTION_GATT_DISCONNECTED = "com.habiband.service.bluetooth.ACTION_GATT_DISCONNECTED"
    const val ACTION_GATT_CONNECTING = "com.habiband.service.bluetooth.ACTION_GATT_CONNECTING"
    const val ACTION_GATT_DISCONNECTING = "com.habiband.service.bluetooth.ACTION_GATT_DISCONNECTING"
    const val ACTION_GATT_SERVICES_DISCOVERED = "com.habiband.service.bluetooth.ACTION_GATT_SERVICES_DISCOVERED"
    const val ACTION_DATA_AVAILABLE = "com.habiband.service.bluetooth.ACTION_DATA_AVAILABLE"
    val UUID_RX_DATA_CHARACTERISTIC: UUID = UUID.fromString(GattAttributes.UUID_RX_DATA_CHARACTERISTIC)
    val UUID_CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR: UUID =  UUID.fromString(GattAttributes.UUID_CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR)
}