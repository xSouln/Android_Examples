package com.example.habiband

object BLEServices {
    object Temperature {
        const val UUID = "00002c00-8e22-4541-9d4c-21edae82ed19"
        object Characteristics{
            const val VALUE = "00002c01-8e22-4541-9d4c-21edae82ed19"
        }
    }

    object Accelerometer {
        const val UUID = "00003c00-8e22-4541-9d4c-21edae82ed19"

        object Characteristics {
            const val POINTS = "00003c01-8e22-4541-9d4c-21edae82ed19"
        }
    }

    object Gyroscope {
        const val UUID = "00004c00-8e22-4541-9d4c-21edae82ed19"

        object Characteristics {
            const val POINTS = "00004c01-8e22-4541-9d4c-21edae82ed19"
        }
    }

    object ECG {
        const val UUID = "00005c00-8e22-4541-9d4c-21edae82ed19"

        object Characteristics {
            const val ECG_POINTS = "00005c01-8e22-4541-9d4c-21edae82ed19"
            const val PPG_POINTS = "00005c02-8e22-4541-9d4c-21edae82ed19"
        }
    }

    object Bootloader {
        const val UUID = "0000b000-8e22-4541-9d4c-21edae82ed19"

        object Characteristics {
            const val PORT_RX = "0000b001-8e22-4541-9d4c-21edae82ed19"
            const val PORT_TX = "0000b002-8e22-4541-9d4c-21edae82ed19"
            const val STATUS = "0000b003-8e22-4541-9d4c-21edae82ed19"
        }
    }
}