package com.example.habiband.types

class ECG_Value(var value: Int) {
    fun getVoltage(): Float{
        return k * getADC()
    }

    fun getADC(): Int{
        return if (value >= negativeLevel) { value - maxLevel } else { value }
    }

    companion object{
        private const val negativeLevel = 8388608 // 2 ^ 23
        private const val maxLevel = 16777216 // 2 ^ 24
        private const val k = 1.2f / 2097152 // 1.2 / (2 ^ 21)
    }
}