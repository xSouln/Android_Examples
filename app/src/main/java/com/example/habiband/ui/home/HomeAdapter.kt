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

        val textViewName = view?.findViewById(R.id.text_view_device_name) as TextView
        textViewName.text = "name :" + bluetoothDevice.name

        val textViewMacAddress = view.findViewById(R.id.text_view_device_mac_address) as TextView
        textViewMacAddress.text = "mac :" + bluetoothDevice.address

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