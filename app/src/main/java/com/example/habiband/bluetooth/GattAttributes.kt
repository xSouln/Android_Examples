package com.example.habiband.bluetooth

import java.util.HashMap

object GattAttributes {
    const val UUID_CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb"
    const val UUID_UART_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e"
    const val UUID_RX_DATA_CHARACTERISTIC = "6e400003-b5a3-f393-e0a9-e50e24dcca9e"
    const val UUID_TX_DATA_CHARACTERISTIC = "6e400002-b5a3-f393-e0a9-e50e24dcca9e"
    const val NAME_CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR = "Update Notification Descriptor"
    const val NAME_UART_SERVICE = "UART Service"
    const val NAME_RX_DATA_CHARACTERISTIC = "RX Data Characteristic"
    const val NAME_TX_DATA_CHARACTERISTIC = "TX Data Characteristic"

    private val attributes: HashMap<String, String> = hashMapOf(
        UUID_CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR to NAME_CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR,
        UUID_UART_SERVICE to NAME_UART_SERVICE,
        UUID_RX_DATA_CHARACTERISTIC to NAME_RX_DATA_CHARACTERISTIC,
        UUID_TX_DATA_CHARACTERISTIC to NAME_TX_DATA_CHARACTERISTIC
    )

    fun lookup(uuid: String?): String? {
        uuid?.apply {
            return attributes[uuid]
        }

        return null
    }
}