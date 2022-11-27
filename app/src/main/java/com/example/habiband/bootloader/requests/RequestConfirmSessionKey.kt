package com.example.habiband.bootloader.requests

import com.example.habiband.bootloader.types.Operations
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RequestConfirmSessionKey(val sessionKey: Int)
{
    companion object
    {
        const val KEY_MASK = 0xf2e5d23c
        const val SIZE_BYTES = (RequestHeader.SIZE_BYTES + UInt.SIZE_BYTES)
    }

    val data: ByteArray
        get()
        {
            val buffer = ByteBuffer.allocate(SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN)
            val header = RequestHeader(Operations.ConfirmSessionKey).header

            buffer.put(header)
            buffer.putInt(sessionKey xor KEY_MASK.toInt())

            return buffer.array()
        }
}