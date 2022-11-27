package com.example.habiband.bootloader.responses

import com.example.habiband.bootloader.requests.RequestHeader
import com.example.habiband.bootloader.types.OperationResults
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ResponseGetSessionKey
{
    companion object
    {
        const val SIZE_BYTES = UInt.SIZE_BYTES
        const val KEY_MASK = 0x35cd87ba
    }

    var value = -1

    fun receive(data: ByteArray?, offset: Int): Boolean
    {
        if (data != null && (data.size - offset) >= SIZE_BYTES)
        {
            val buffer = ByteBuffer.wrap(data, offset, SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN)

            value = buffer.int xor KEY_MASK

            return true
        }

        return false
    }
}