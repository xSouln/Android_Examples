package com.example.habiband.ui.home

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.habiband.bluetooth.Scanner
import com.example.habiband.bluetooth.interfaces.IControlEventListener

class HomeViewModel : ViewModel(), IControlEventListener {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }

    private var observableDevices = MutableLiveData<ArrayList<BluetoothDevice>>()
   // private var devices = ArrayList<Device>()

    val text: LiveData<String> = _text

    init {
        observableDevices.value = Scanner.getDiscoveredDevices()
    }

    fun getDevices(): LiveData<ArrayList<BluetoothDevice>>{
        return observableDevices
    }

    override fun discoveredDevicesChanged() {
        observableDevices.value = Scanner.getDiscoveredDevices()
    }

    override fun updateCount(count: Long) {
        _text.value = "circle: $count"
    }
}