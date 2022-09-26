package com.example.habiband.ui.notifications

import android.bluetooth.BluetoothGattCharacteristic
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.habiband.bluetooth.Connection
import com.example.habiband.bluetooth.Control
import com.example.habiband.bluetooth.interfaces.IConnectionEventListener
import com.example.habiband.ui.dashboard.ExpandableServicesAdapter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class NotificationsViewModel : ViewModel(), IConnectionEventListener {

    private val _temperature = MutableLiveData<Int>()
    var temperature: LiveData<Int> = _temperature

    private var connection: Connection? = null

    fun setConnection(connection: Connection?){
        this.connection = connection
        connection?.setEventListener(this)
    }

    override fun servicesDiscovered(connection: Connection) {

    }

    override fun connectionStateChanged(connection: Connection) {

    }

    override fun characteristicChanged(
        connection: Connection,
        characteristic: BluetoothGattCharacteristic?
    ) {
        if (characteristic != null && characteristic.value.size == Int.SIZE_BYTES){
            val buffer = ByteBuffer.wrap(characteristic.value)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            val value = buffer.int
            _temperature.postValue(value)
        }
    }
}