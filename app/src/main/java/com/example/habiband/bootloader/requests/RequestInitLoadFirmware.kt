package com.example.habiband.bootloader.requests

import com.example.habiband.bootloader.types.Operations
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RequestInitLoadFirmware(val sessionKey: Int, val size: Int, val crc: Int)
{
    companion object
    {
        const val KEY_MASK = 0x548632ac
        const val SIZE_BYTES = (RequestHeader.SIZE_BYTES + UInt.SIZE_BYTES + UInt.SIZE_BYTES * 2)
    }

    val data: ByteArray
        get()
        {
            val buffer = ByteBuffer.allocate(SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN)
            val header = RequestHeader(Operations.InitLoadFirmware).header

            buffer.put(header)
            buffer.putInt(sessionKey xor KEY_MASK)
            buffer.putInt(size)
            buffer.putInt(crc)

            return buffer.array()
        }
}