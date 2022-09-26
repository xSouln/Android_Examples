package com.example.habiband.ui.dashboard

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }

    private var observableGatt = MutableLiveData<BluetoothGatt>()

    init {
        observableGatt = MutableLiveData<BluetoothGatt>()
    }

    val text: LiveData<String> = _text
}