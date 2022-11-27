package com.example.habiband.bootloader.requests

import com.example.habiband.bootloader.types.Operations
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RequestStartBoot(val sessionKey: Int)
{
    companion object
    {
        const val KEY_MASK = 0x4E62B165
        const val SIZE_BYTES = (RequestHeader.SIZE_BYTES + UInt.SIZE_BYTES)
    }

    val data: ByteArray
        get()
        {
            val buffer = ByteBuffer.allocate(SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN)

            buffer.put(RequestHeader(Operations.StartBoot).header)

            buffer.putInt(sessionKey xor KEY_MASK)

            return buffer.array()
        }
}