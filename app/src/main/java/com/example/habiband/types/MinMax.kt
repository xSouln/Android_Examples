package com.example.habiband.types

class MinMax() {
    var min = 0.0f
    var max = 0.0f

    fun set(value: MinMax){
        min = value.min
        max = value.max
    }

    fun set(min: Float, max: Float){
        this.min = min
        this.max = max
    }

    fun compare(min: Float, max: Float){
        if (this.min > min) this.min = min
        if (this.max < max) this.max = max
    }
}