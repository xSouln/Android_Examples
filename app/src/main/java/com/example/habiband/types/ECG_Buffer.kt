package com.example.habiband.types

class ECG_Buffer(var sizeMask: Int = 0x3ff) {
    var points: Array<ECG_Value> = Array(sizeMask + 1){ ECG_Value(0) }
    var totalIndex = 0
    var handlerIndex = 0

    fun put(value: ECG_Value){
        points[totalIndex] = value

        totalIndex++
        totalIndex = totalIndex.and(sizeMask)
    }

    fun put(value: Int){
        points[totalIndex].value = value

        totalIndex++
        totalIndex = totalIndex.and(sizeMask)
    }

    fun pointsCount(): Int{
        return (totalIndex - handlerIndex).and(sizeMask)
    }

    fun read(): ECG_Value{
        val result = points[handlerIndex]
        handlerIndex++
        handlerIndex = handlerIndex.and(sizeMask)

        return result
    }
}