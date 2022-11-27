package com.example.habiband.bluetooth.interfaces

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import com.example.habiband.bluetooth.Connection

interface IConnectionEventListener {
    fun servicesDiscovered(connection: Connection)
    fun connectionStateChanged(connection: Connection)
    fun characteristicChanged(connection: Connection,
                              characteristic: BluetoothGattCharacteristic?)
    fun characteristicWrite(connection: Connection,
                            characteristic: BluetoothGattCharacteristic?)
}