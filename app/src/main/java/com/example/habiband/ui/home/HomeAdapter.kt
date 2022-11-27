package com.example.habiband.ui.home

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.lifecycle.LiveData
import com.example.habiband.R
import com.example.habiband.bluetooth.Scanner

class HomeAdapter (
    context: Context,
    private val bluetoothDevicesList: LiveData<ArrayList<BluetoothDevice>>
) : BaseAdapter() {
    private var inflater: LayoutInflater

    init {
        inflater = LayoutInflater.from(context)
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        /*
        val rowView = inflater.inflate(R.layout.list_item_bluetooth_device, parent, false)
        val bluetoothDevice = bluetoothDevicesList.value!![position]

        rowView.
*/

        val bluetoothDevice = bluetoothDevicesList.value!![position]
        var view: View? = convertView

        if (view == null){
            view = this.inflater.inflate(R.layout.list_item_bluetooth_device, parent, false)
        }

        val name = view?.findViewById(R.id.text_view_device_name) as TextView
        name.text = "name: " + bluetoothDevice.name

        val mac = view.findViewById(R.id.text_view_device_mac) as TextView
        mac.text = "mac: " + bluetoothDevice.address

        val type = view.findViewById(R.id.text_view_device_type) as TextView
        type.text = "type: " + Scanner.getType(bluetoothDevice)

        return view
    }

    override fun getItem(position: Int): Any {
        return bluetoothDevicesList.value!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return bluetoothDevicesList.value!!.size
    }
}