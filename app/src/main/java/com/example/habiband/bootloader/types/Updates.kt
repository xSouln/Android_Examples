package com.example.habiband.bootloader.types

import java.nio.ByteBuffer
import java.nio.ByteOrder

enum class Updates
{
    Idle,
    LoadingFirmware,

    Undefined;

    companion object
    {
        const val shift = 6
        const val flag = 0x40
        const val SIZE_BYTES = UShort.SIZE_BYTES

        fun fromByteArray(data: ByteArray, offset: Int): Updates
        {
            val size = data.size - offset

            if (size < SIZE_BYTES)
            {
                return Undefined
            }

            val buffer = ByteBuffer.wrap(data, offset, SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()

            return fromInt(buffer[0].toInt() - flag)
        }

        fun fromInt(value: Int): Updates
        {
            return when(value)
            {
                Idle.ordinal -> Idle
                LoadingFirmware.ordinal -> LoadingFirmware

                else -> Undefined
            }
        }
    }
}