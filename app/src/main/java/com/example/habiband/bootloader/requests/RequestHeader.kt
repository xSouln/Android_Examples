package com.example.habiband.bootloader.requests

import com.example.habiband.bootloader.types.Operations
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RequestHeader(var operation: Operations)
{
    companion object
    {
        const val SIZE_BYTES = (Operations.SIZE_BYTES + Short.SIZE_BYTES)
    }

    var reserved: Short = 0

    val header: ByteArray
        get()
        {
            val buffer = ByteBuffer.allocate(SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN)

            buffer.putShort(operation.ordinal.toShort())
            buffer.putShort(reserved)

            return buffer.array()
        }
}