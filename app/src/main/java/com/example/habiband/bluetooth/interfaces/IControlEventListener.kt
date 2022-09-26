package com.example.habiband.bluetooth.interfaces

interface IControlEventListener {
    fun discoveredDevicesChanged()
    fun updateCount(count: Long)
}