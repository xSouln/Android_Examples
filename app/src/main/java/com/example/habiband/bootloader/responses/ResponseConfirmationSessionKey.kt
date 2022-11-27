package com.example.habiband.bootloader.responses

import java.nio.ByteBuffer
import java.nio.ByteOrder

class ResponseConfirmationSessionKey
{
    companion object
    {
        const val SIZE_BYTES = UInt.SIZE_BYTES
    }

    var value = 0

    fun receive(data: ByteArray?, offset: Int): Boolean
    {
        if (data != null && (data.size - offset) >= SIZE_BYTES)
        {
            val buffer = ByteBuffer.wrap(data, offset, SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN)

            value = buffer.int

            return false
        }

        return true
    }
}