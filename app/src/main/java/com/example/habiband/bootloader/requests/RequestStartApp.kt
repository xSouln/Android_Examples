package com.example.habiband.bootloader.requests

import com.example.habiband.bootloader.types.Operations
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RequestStartApp()
{
    companion object
    {
        const val KEY_MASK = 0x87A27999
        const val SIZE_BYTES = (RequestHeader.SIZE_BYTES)
    }

    val data: ByteArray
    get()
    {
        val buffer = ByteBuffer.allocate(SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN)

        buffer.put(RequestHeader(Operations.StartApp).header)

        return buffer.array()
    }
}