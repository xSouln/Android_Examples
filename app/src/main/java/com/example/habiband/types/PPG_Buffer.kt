package com.example.habiband.types

class PPG_Buffer(var sizeMask: Int = 0x3ff) {
    var points: Array<PPG_Value> = Array(sizeMask + 1){ PPG_Value(0, 0, 0, 0) }
    var totalIndex = 0
    var handlerIndex = 0

    fun getMinMaxVoltage(points: Int): MinMax{
        var startIndex = 0
        val minMax: MinMax = MinMax()
        var ppgValue: PPG_Value

        var count = points
        if (count > sizeMask + 1){
            count = sizeMask + 1
        }

        if(count > 0){
            startIndex = (totalIndex - count).and(sizeMask)
            ppgValue = this.points[startIndex]
            minMax.set(ppgValue.getMinVoltage(), ppgValue.getMaxVoltage())

            count--

            while (count > 0){
                startIndex++
                startIndex = startIndex.and(sizeMask)

                ppgValue = this.points[startIndex]
                minMax.compare(ppgValue.getMinVoltage(), ppgValue.getMaxVoltage())

                count--
            }
        }

        return minMax
    }

    fun put(value: PPG_Value){
        points[totalIndex] = value

        totalIndex++
        totalIndex = totalIndex.and(sizeMask)
    }

    fun put(redAc: Int, irAc: Int, redDc: Int, irDc: Int){
        points[totalIndex].redAc = redAc
        points[totalIndex].irAc = irAc
        points[totalIndex].redDc = redDc
        points[totalIndex].irDc = irDc

        totalIndex++
        totalIndex = totalIndex.and(sizeMask)
    }

    fun pointsCount(): Int{
        return (totalIndex - handlerIndex).and(sizeMask)
    }

    fun read(): PPG_Value{
        val result = points[handlerIndex]
        handlerIndex++
        handlerIndex = handlerIndex.and(sizeMask)

        return result
    }
}