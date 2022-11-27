package com.example.habiband.types

class GyroscopeBuffer(var sizeMask: Int = 0x3ff) {
    var points: Array<GyroscopeValue> = Array(sizeMask + 1){ GyroscopeValue(0,0,0) }
    var totalIndex = 0
    var handlerIndex = 0

    fun put(value: GyroscopeValue){
        points[totalIndex].x = value.x
        points[totalIndex].y = value.y
        points[totalIndex].z = value.z

        totalIndex++
        totalIndex = totalIndex.and(sizeMask)
    }

    fun put(x: Short, y: Short, z: Short){
        points[totalIndex].x = x
        points[totalIndex].y = y
        points[totalIndex].z = z

        totalIndex++
        totalIndex = totalIndex.and(sizeMask)
    }

    fun pointsCount(): Int{
        return (totalIndex - handlerIndex).and(sizeMask)
    }

    fun read(): GyroscopeValue{
        val result = points[handlerIndex]
        handlerIndex++
        handlerIndex = handlerIndex.and(sizeMask)

        return result
    }
}