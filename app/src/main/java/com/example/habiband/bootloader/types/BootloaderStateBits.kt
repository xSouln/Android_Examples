package com.example.habiband.bootloader.types

enum class BootloaderStateBits
{
    InBootSection,
    OperationInProgress,
    FirmwareLoadingInit,
    RequestUpdate,
    SessionKeyConfirmed,

    BLE_IsConected,
    BLE_NotificationTxIsEnabled,
    BLE_NotificationStatus;

    companion object
    {
        fun isEnabled(state: BootloaderStateBits, value: Int) : Boolean
        {
            val mask = 1 shl state.ordinal

            return mask and value > 0
        }
    }
}