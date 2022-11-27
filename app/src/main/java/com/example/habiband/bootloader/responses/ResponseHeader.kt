package com.example.habiband.bootloader.responses

import com.example.habiband.bootloader.types.OperationResults
import com.example.habiband.bootloader.types.Operations
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ResponseHeader()
{
    companion object
    {
        const val SIZE_BYTES = (OperationResults.SIZE_BYTES + Operations.SIZE_BYTES)
    }

    var result = OperationResults.Undefined
    var operation = Operations.Undefined

    fun receive(data: ByteArray?, offset: Int)
    {
        var _offset = offset

        operation = Operations.fromByteArray(data, _offset)
        _offset += OperationResults.SIZE_BYTES

        result = OperationResults.fromByteArray(data, _offset)
    }
}