package com.example.habiband.bootloader.requests

import com.example.habiband.bootloader.types.Operations
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RequestGetSessionKey
{
    companion object
    {
        const val KEY_MASK = 0x548632ac
        const val SIZE_BYTES = (RequestHeader.SIZE_BYTES)
    }

    val data: ByteArray
        get()
        {
            val buffer = ByteBuffer.allocate(SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN)
            val header = RequestHeader(Operations.GetSessionKey).header

            buffer.put(header)

            return buffer.array()
        }
}