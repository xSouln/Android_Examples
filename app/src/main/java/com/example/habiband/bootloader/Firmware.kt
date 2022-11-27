package com.example.habiband.bootloader

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.*

object Firmware
{
    var file = ArrayList<Byte>()

    private val _path = MutableLiveData<String>().apply { value = "" }
    var observePath: LiveData<String> = _path
    var path: String = ""

    private val _crc = MutableLiveData<Int>().apply { value = -1 }
    var observeCRC: LiveData<Int> = _crc
    var CRC = -1

    private val _size = MutableLiveData<Int>().apply { value = -1 }
    var observeSize: LiveData<Int> = _size

    fun open(context: Context, uri: Uri?)
    {
        if (uri != null)
        {
            val _file = ArrayList<Byte>()

            //val reader = BufferedReader(InputStreamReader(context.contentResolver.openInputStream(uri)))
            path = uri.path.toString()
            _path.postValue(path)

            var inputStream: FileInputStream? = null
            try
            {
                inputStream = FileInputStream(context.contentResolver.openFileDescriptor(uri, "r")?.fileDescriptor)
            }
            catch (e: Exception)
            {
                val result = e.toString()
            }

            val stream = DataInputStream(inputStream)
            clear()
            while (true)
            {
                //val value = reader.read()
                try
                {
                    val j = stream.readUnsignedByte()
                    this.file.add(j.toByte())
                }
                catch (e: Exception)
                {
                    break
                }
            }

            _size.postValue(size)

            CRC = crc
            _crc.postValue(CRC)
        }
    }

    fun clear()
    {
        file.clear()
    }

    fun startLoad()
    {

    }

    val size: Int
        get()
        {
            return file.size
        }

    val crc: Int
        get()
        {
            var result = 0

            for (i: Int in 0 until size)
            {
                result += file[i].toInt().and(0xff)
            }

            return result
        }
}
