package com.example.habiband.bootloader.updates

import com.example.habiband.bootloader.requests.RequestHeader
import java.nio.ByteBuffer
import java.nio.ByteOrder

class UpdateFirmwareLoadingStatus()
{
    companion object
    {
        const val SIZE_BYTES = (UInt.SIZE_BYTES * 2)
    }

    var loadedImageSize = 0
    var loadedImageCrc = 0

    fun receiveData(data: ByteArray, offset: Int)
    {
        val size = data.size - offset

        if (size < SIZE_BYTES)
        {
            return
        }

        val buffer = ByteBuffer.wrap(data, offset, SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer()

        loadedImageSize = buffer[0]
        loadedImageCrc = buffer[1]
    }
}