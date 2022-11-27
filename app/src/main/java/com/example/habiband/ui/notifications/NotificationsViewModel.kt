package com.example.habiband.ui.notifications

import android.bluetooth.BluetoothGattCharacteristic
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.habiband.BLEServices
import com.example.habiband.bluetooth.Connection
import com.example.habiband.bluetooth.interfaces.IConnectionEventListener
import com.example.habiband.types.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

class NotificationsViewModel : ViewModel(), IConnectionEventListener {

    private val _temperature = MutableLiveData<Float>()
    var temperature: LiveData<Float> = _temperature

    val gyroscopeBuffer: GyroscopeBuffer = GyroscopeBuffer(0x3ff)
    val accelerometerBuffer: AccelerometerBuffer = AccelerometerBuffer(0x3ff)
    val ecgBuffer: ECG_Buffer = ECG_Buffer(0x3ff)
    val ppgBuffer: PPG_Buffer = PPG_Buffer(0x3ff)

    private val _gyroscopePoints = MutableLiveData<GyroscopeBuffer>().apply { value = gyroscopeBuffer }
    private val _accelerometerPoints = MutableLiveData<AccelerometerBuffer>().apply { value = accelerometerBuffer }
    private val _ecgPoints = MutableLiveData<ECG_Buffer>().apply { value = ecgBuffer }
    private val _ppgPoints = MutableLiveData<PPG_Buffer>().apply { value = ppgBuffer }

    var gyroscopePoints: LiveData<GyroscopeBuffer> = _gyroscopePoints
    var accelerometerPoints: LiveData<AccelerometerBuffer> = _accelerometerPoints
    var ecgPoints: LiveData<ECG_Buffer> = _ecgPoints
    var ppgPoints: LiveData<PPG_Buffer> = _ppgPoints

    private var connection: Connection? = null

    fun setConnection(connection: Connection?){
        this.connection = connection
        connection?.setEventListener(this)
    }

    fun clear(){

    }

    override fun servicesDiscovered(connection: Connection) {

    }

    override fun connectionStateChanged(connection: Connection) {

    }

    private fun receiveTemperature(data: ByteArray){
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer()
        //buffer.order(ByteOrder.LITTLE_ENDIAN)
        if (buffer.capacity() > 0){
            _temperature.postValue(buffer[0])
        }
    }

    private fun receiveGyroscopePoints(data: ByteArray){
        if (data.isNotEmpty()){
            val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
            val pointsCount = buffer.capacity() / 3
            if (pointsCount > 0){
                var j = 0
                for (i: Int in 0 until buffer.capacity() / 3){
                    gyroscopeBuffer.put(buffer[j], buffer[j + 1], buffer[j + 2])
                    j += 3
                }
                _gyroscopePoints.postValue(gyroscopeBuffer)
            }
        }
    }

    private fun receiveAccelerometerPoints(data: ByteArray){
        if (data.isNotEmpty()){
            val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
            val pointsCount = buffer.capacity() / 3
            if (pointsCount > 0){
                var j = 0
                for (i: Int in 0 until buffer.capacity() / 3){
                    accelerometerBuffer.put(buffer[j], buffer[j + 1], buffer[j + 2])
                    j += 3
                }
                _accelerometerPoints.postValue(accelerometerBuffer)
            }
        }
    }

    private fun receiveECGPoints(data: ByteArray){
        if (data.isNotEmpty()){
            val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer()
            val pointsCount = buffer.capacity()
            if (pointsCount > 0){
                for (i: Int in 0 until buffer.capacity()){
                    ecgBuffer.put(buffer[i])
                }
                _ecgPoints.postValue(ecgBuffer)
            }
        }
    }

    private fun receivePPGPoints(data: ByteArray){
        if (data.isNotEmpty()){
            val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer()
            val pointsCount = buffer.capacity() / 4
            if (pointsCount > 0){
                var j = 0
                for (i: Int in 0 until buffer.capacity() / 4){
                    ppgBuffer.put(buffer[j], buffer[j + 1], buffer[j + 2], buffer[j + 3])
                    j += 4
                }
                _ppgPoints.postValue(ppgBuffer)
            }
        }
    }

    override fun characteristicChanged(
        connection: Connection,
        characteristic: BluetoothGattCharacteristic?
    ) {
        if (characteristic != null){
            when(characteristic.uuid.toString()){
                BLEServices.Temperature.Characteristics.VALUE -> receiveTemperature(characteristic.value)
                BLEServices.Gyroscope.Characteristics.POINTS -> receiveGyroscopePoints(characteristic.value)
                BLEServices.Accelerometer.Characteristics.POINTS -> receiveAccelerometerPoints(characteristic.value)
                BLEServices.ECG.Characteristics.ECG_POINTS -> receiveECGPoints(characteristic.value)
                BLEServices.ECG.Characteristics.PPG_POINTS -> receivePPGPoints(characteristic.value)
            }
        }
    }

    override fun characteristicWrite(
        connection: Connection,
        characteristic: BluetoothGattCharacteristic?
    ) {

    }
}