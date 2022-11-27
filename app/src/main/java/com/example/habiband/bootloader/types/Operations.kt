package com.example.habiband.bootloader.types

import java.nio.ByteBuffer
import java.nio.ByteOrder

enum class Operations
{
    Idle,
    Reset,
    StartBoot,
    StartApp,
    InitLoadFirmware,
    ConfirmationLoadFirmware,
    Write,
    Read,
    Load,
    CancelLoadFirmware,
    GetSessionKey,
    ConfirmSessionKey,

    Undefined;

    companion object
    {
        const val shift = 0
        const val mask = 0x3f
        const val SIZE_BYTES = UShort.SIZE_BYTES

        fun fromByteArray(data: ByteArray?, offset: Int): Operations
        {
            if (data == null)
            {
                return Undefined
            }

            val size = data.size - offset

            if (size < SIZE_BYTES)
            {
                return Undefined
            }

            val buffer = ByteBuffer.wrap(data, offset, SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()

            return fromInt(buffer[0].toInt())
        }

        fun fromInt(value: Int): Operations
        {
            when(value shr shift)
            {
                Idle.ordinal -> return Idle
                Reset.ordinal -> return Reset
                StartBoot.ordinal -> return StartBoot
                StartApp.ordinal -> return StartApp
                InitLoadFirmware.ordinal -> return InitLoadFirmware
                ConfirmationLoadFirmware.ordinal -> return ConfirmationLoadFirmware
                Write.ordinal -> return Write
                Read.ordinal -> return Read
                Load.ordinal -> return Load
                GetSessionKey.ordinal -> return GetSessionKey
                CancelLoadFirmware.ordinal -> return CancelLoadFirmware
                ConfirmSessionKey.ordinal -> return ConfirmSessionKey

                else -> return Undefined
            }
        }
    }
}