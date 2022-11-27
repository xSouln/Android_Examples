package com.example.habiband.bootloader.types

import java.nio.ByteBuffer
import java.nio.ByteOrder

enum class OperationResults
{
    Accept,
    Error,
    InvalidParameter,
    Busy,
    TimeOut,
    NotSupported,
    ValueIsNotFound,
    RequestIsNotFound,
    LinkError,
    ComponentInitializationError,
    OutOfRange,
    TypeMismatch,
    PermissionError,

    Undefined,
    DecodeError;

    companion object
    {
        const val SIZE_BYTES = UShort.SIZE_BYTES

        fun fromByteArray(data: ByteArray?, offset: Int): OperationResults
        {
            if (data == null)
            {
                return DecodeError
            }

            val size = data.size - offset

            if (size < SIZE_BYTES)
            {
                return Undefined
            }

            val buffer = ByteBuffer.wrap(data, offset, SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN)

            return fromInt(buffer.short.toInt())
        }

        fun fromInt(value: Int): OperationResults
        {
            when(value)
            {
                Accept.ordinal -> return Accept
                Error.ordinal -> return Error
                InvalidParameter.ordinal -> return InvalidParameter
                TimeOut.ordinal -> return TimeOut
                NotSupported.ordinal -> return NotSupported
                ValueIsNotFound.ordinal -> return ValueIsNotFound
                RequestIsNotFound.ordinal -> return RequestIsNotFound
                Busy.ordinal -> return Busy
                ComponentInitializationError.ordinal -> return ComponentInitializationError
                OutOfRange.ordinal -> return OutOfRange
                TypeMismatch.ordinal -> return TypeMismatch
                PermissionError.ordinal -> return PermissionError
                LinkError.ordinal -> return LinkError

                else -> return Undefined
            }
        }
    }
}