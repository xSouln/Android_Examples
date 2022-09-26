package com.example.habiband.ui.dashboard

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.habiband.bluetooth.Connection
import com.example.habiband.bluetooth.Control
import com.example.habiband.bluetooth.interfaces.IConnectionEventListener
import com.example.habiband.databinding.FragmentDashboardBinding
import com.example.habiband.ui.home.HomeAdapter

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private var adapter: ExpandableServicesAdapter? = null
    private var connection: Connection? = null

    @SuppressLint("SetTextI18n", "MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //adapter = ServicesAdapter(requireContext(), Control.connection?.gatt!!)
        val device = Control.selectedDevice
        connection = Connection(requireContext())
        //connection?.setEventListener(this)
        connection?.registerReceiver(gattUpdateReceiver)
        connection?.open(device)
        //val device = Control.connection?.gatt?.device

        //binding.listViewBluetoothServices.adapter = adapter
        binding.textViewDashboardDeviceName.text = "name: " + device?.name
        binding.textViewDashboardDeviceMac.text = "mac: " + device?.address
        binding.textViewDashboardDeviceType.text = "type: " + device?.type
        binding.textViewDashboardDeviceClass.text = "class: " + device?.bluetoothClass
        binding.textViewDashboardDeviceState.text = "state: " + device?.bondState

        //adapter = ExpandableServicesAdapter(requireContext(), connection!!)
        //binding.expandableListServices.setAdapter(adapter)
        /*
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        */

        return root
    }

    private val gattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Control.ACTION_GATT_CONNECTED -> {

                }
                Control.ACTION_GATT_DISCONNECTED -> {

                }
                Control.ACTION_GATT_SERVICES_DISCOVERED -> {
                    adapter = ExpandableServicesAdapter(requireContext(), connection)
                    _binding?.expandableListServices?.setAdapter(adapter)
                }
                Control.ACTION_DATA_AVAILABLE -> {
                    //Toast.makeText(context, "ACTION_DATA_AVAILABLE", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        connection?.unregisterReceiver(gattUpdateReceiver)
    }
}