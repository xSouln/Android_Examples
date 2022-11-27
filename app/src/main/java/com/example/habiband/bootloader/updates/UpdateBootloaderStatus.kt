package com.example.habiband.bootloader.updates

import com.example.habiband.bootloader.types.BootloaderStateBits
import com.example.habiband.bootloader.types.OperationResults
import com.example.habiband.bootloader.types.Operations
import java.nio.ByteBuffer
import java.nio.ByteOrder

class UpdateBootloaderStatus()
{
    var operation: Operations = Operations.Idle
    var operationResult: OperationResults = OperationResults.Accept
    var operationInProgress = false
    var inBootSection = false
    var sessionKeyConfirmed = false
    var firmwareLoadingInit = false

    companion object
    {
        const val SIZE_BYTES = UShort.SIZE_BYTES + UByte.SIZE_BYTES * 4
    }

    fun receiveData(data: ByteArray, offset: Int)
    {
        val size = data.size - offset

        if (size <= 0)
        {
            return
        }

        val buffer = ByteBuffer.wrap(data, offset, size).order(ByteOrder.LITTLE_ENDIAN)

        if (buffer.capacity() >= SIZE_BYTES)
        {
            val state = buffer.short.toInt()

            operationInProgress = BootloaderStateBits.isEnabled(BootloaderStateBits.OperationInProgress, state)
            inBootSection = BootloaderStateBits.isEnabled(BootloaderStateBits.InBootSection, state)
            sessionKeyConfirmed = BootloaderStateBits.isEnabled(BootloaderStateBits.SessionKeyConfirmed, state)
            firmwareLoadingInit = BootloaderStateBits.isEnabled(BootloaderStateBits.FirmwareLoadingInit, state)

            operation = Operations.fromInt(buffer[2].toInt())
            operationResult = OperationResults.fromInt(buffer[3].toInt())
        }
    }

}