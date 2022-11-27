package com.example.habiband.types

class PPG_Value(var redAc: Int, var irAc: Int, var redDc: Int, var irDc: Int) {

    fun getRedAcVoltage(): Float{
        return k * getRedAcADC()
    }

    fun getMinVoltage(): Float{
        var result = getRedAcVoltage()
        val irAcVoltage = getIrAcVoltage()
        val redDcVoltage = getRedDcVoltage()
        val irDcVoltage = getIrDcVoltage()

        if (result > irAcVoltage) result = irAcVoltage
        if (result > redDcVoltage) result = redDcVoltage
        if (result > irDcVoltage) result = irDcVoltage

        return  result
    }

    fun getMaxVoltage(): Float{
        var result = getRedAcVoltage()
        val irAcVoltage = getIrAcVoltage()
        val redDcVoltage = getRedDcVoltage()
        val irDcVoltage = getIrDcVoltage()

        if (result < irAcVoltage) result = irAcVoltage
        if (result < redDcVoltage) result = redDcVoltage
        if (result < irDcVoltage) result = irDcVoltage

        return  result
    }

    fun getRedAcADC(): Int{
        return if (redAc >= negativeLevel) { redAc - maxLevel } else { redAc }
    }

    fun getIrAcVoltage(): Float{
        return k * getIrAcADC()
    }

    fun getIrAcADC(): Int{
        return if (irAc >= negativeLevel) { irAc - maxLevel } else { irAc }
    }

    fun getRedDcVoltage(): Float{
        return k * getRedDcADC()
    }

    fun getRedDcADC(): Int{
        return if (redDc >= negativeLevel) { redDc - maxLevel } else { redDc }
    }

    fun getIrDcVoltage(): Float{
        return k * getIrDcADC()
    }

    fun getIrDcADC(): Int{
        return if (irDc >= negativeLevel) { irDc - maxLevel } else { irDc }
    }

    companion object{
        private const val negativeLevel = 8388608 // 2 ^ 23
        private const val maxLevel = 16777216 // 2 ^ 24
        private const val k = 1.2f / 2097152 // 1.2 / (2 ^ 21)
    }
}