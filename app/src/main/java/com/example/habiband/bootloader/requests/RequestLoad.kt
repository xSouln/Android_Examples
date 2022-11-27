package com.example.habiband.bootloader.requests

import com.example.habiband.bootloader.types.Operations
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RequestLoad(val area: ByteArray, val offset: Int, val size: Int)
{
    companion object
    {
        const val maximumPacketSize = 128
    }

    val data: ByteArray
        get()
        {
            val buffer = ByteBuffer.allocate((RequestHeader.SIZE_BYTES + UInt.SIZE_BYTES + size)).order(ByteOrder.LITTLE_ENDIAN)

            buffer.put(RequestHeader(Operations.Load).header)

            buffer.putInt(size)
            buffer.put(area, offset, size)

            return buffer.array()
        }
}