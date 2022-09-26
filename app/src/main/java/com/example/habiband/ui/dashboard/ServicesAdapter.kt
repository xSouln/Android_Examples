package com.example.habiband.ui.dashboard

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.lifecycle.LiveData
import com.example.habiband.R

@SuppressLint("MissingPermission")
class ServicesAdapter(context: Context,
                      private val gatt: BluetoothGatt?
                      ): BaseAdapter() {
    private var inflater: LayoutInflater

    init {
        inflater = LayoutInflater.from(context)
    }

    override fun getItem(position: Int): Any {
        return gatt?.services!![position].uuid.toString()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return gatt?.services!!.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View? = convertView

        if (view == null){
            view = this.inflater.inflate(R.layout.list_item_bluetooth_service, parent, false)
        }

        val textViewName = view?.findViewById(R.id.text_view_service_name) as TextView
        textViewName.text = "service uuid :" + gatt?.services!![position].uuid.toString()
        /*
        val textViewMacAddress = view.findViewById(R.id.text_view_device_mac_address) as TextView
        textViewMacAddress.text = "mac :" + bluetoothDevice.address
        */
        return view;
    }
}