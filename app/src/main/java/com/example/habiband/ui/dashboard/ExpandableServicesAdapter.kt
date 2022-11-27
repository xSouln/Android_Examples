package com.example.habiband.ui.dashboard

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.example.habiband.bluetooth.Connection
import com.example.habiband.databinding.FragmentDashboardBinding


class ExpandableServicesAdapter(context: Context,
                                private val connection: Connection?
): BaseExpandableListAdapter() {
    private var inflater: LayoutInflater

    init {
        inflater = LayoutInflater.from(context)
    }

    override fun getGroupCount(): Int {
        if (Connection.connectedGatt == null
            || Connection.connectedGatt!!.services == null) {
            return 0
        }
        return Connection.connectedGatt!!.services.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        if (Connection.connectedGatt == null
            || Connection.connectedGatt!!.services == null
            || Connection.connectedGatt!!.services[groupPosition].characteristics == null) {
            return 0
        }
        return Connection.connectedGatt!!.services[groupPosition].characteristics.size
    }

    override fun getGroup(groupPosition: Int): Any {
        if (Connection.connectedGatt == null
            || Connection.connectedGatt!!.services == null) {
            return 0
        }
        return Connection.connectedGatt!!.services[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        if (Connection.connectedGatt == null
            || Connection.connectedGatt!!.services == null
            || Connection.connectedGatt!!.services[groupPosition].characteristics == null) {
            return 0
        }
        return Connection.connectedGatt!!.services[groupPosition].characteristics[childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    @SuppressLint("SetTextI18n")
    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var view: View? = convertView

        if (view == null) {
            view = this.inflater.inflate(com.example.habiband.R.layout.list_item_child_bluetoth_service, parent, false)
        }

        val uuid = view?.findViewById(com.example.habiband.R.id.text_view_child_service_uuid) as TextView
        uuid.text = "uuid: " + Connection.connectedGatt?.services!![groupPosition].uuid.toString()

        val type = view.findViewById(com.example.habiband.R.id.text_view_child_service_type) as TextView
        type.text = "type: " + Connection.connectedGatt!!.services!![groupPosition].type.toString()

        val includedServices = view.findViewById(com.example.habiband.R.id.text_view_child_included_services) as TextView
        includedServices.text = "included services: " + Connection.connectedGatt!!.services!![groupPosition].includedServices.size

        val includedCharacteristics = view.findViewById(com.example.habiband.R.id.text_view_child_included_characteristics) as TextView
        includedCharacteristics.text = "included characteristics: " + Connection.connectedGatt!!.services!![groupPosition].characteristics.size

        return view;
    }

    @SuppressLint("SetTextI18n")
    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var view: View? = convertView

        if (view == null) {
            view = this.inflater.inflate(com.example.habiband.R.layout.list_item_bloetooth_characteristic, parent, false)
        }

        val uuid = view?.findViewById(com.example.habiband.R.id.text_view_child_characteristic_uuid) as TextView
        uuid.text = "uuid :" + Connection.connectedGatt?.services!![groupPosition].characteristics[childPosition].uuid.toString()

        val writeType = view.findViewById(com.example.habiband.R.id.text_view_child_characteristic_write_type) as TextView
        writeType.text = "write type :" + Connection.connectedGatt!!.services!![groupPosition].characteristics[childPosition].writeType

        val descriptorsCount = view.findViewById(com.example.habiband.R.id.text_view_child_descriptors_count) as TextView
        descriptorsCount.text = "descriptors count :" + Connection.connectedGatt!!.services!![groupPosition].characteristics[childPosition].descriptors.size.toString()

        val descriptorsProperties = view.findViewById(com.example.habiband.R.id.text_view_child_characteristic_properties) as TextView
        descriptorsProperties.text = "properties :" + Connection.connectedGatt!!.services!![groupPosition].characteristics[childPosition].properties.toString()

        val descriptorsPermissions = view.findViewById(com.example.habiband.R.id.text_view_child_characteristic_permissions) as TextView
        descriptorsPermissions.text = "permissions :" + Connection.connectedGatt!!.services!![groupPosition].characteristics[childPosition].permissions

        val value = view.findViewById(com.example.habiband.R.id.text_view_child_characteristic_value) as TextView
        value.text = "value :" + Connection.connectedGatt!!.services!![groupPosition].characteristics[childPosition].value.toString()

        return view;
    }
}