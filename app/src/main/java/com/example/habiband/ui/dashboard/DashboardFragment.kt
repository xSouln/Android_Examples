package com.example.habiband.ui.dashboard

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.habiband.bluetooth.Connection
import com.example.habiband.bluetooth.Scanner
import com.example.habiband.bluetooth.interfaces.IConnectionEventListener
import com.example.habiband.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment(), IConnectionEventListener {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private var adapter: ExpandableServicesAdapter? = null
    private var connection: Connection? = null

    @SuppressLint("SetTextI18n", "MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        val dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        Connection.setEventListener(this)
        Connection.registerReceiver(requireContext(), gattUpdateReceiver)

        //adapter = ServicesAdapter(requireContext(), Control.connection?.gatt!!)
        val device = Scanner.selectedDevice

        if (Connection.device != Scanner.selectedDevice && Connection.state == Connection.State.Connected)
        {
            Connection.close()
        }

        Connection.open(device, requireContext())

        //binding.listViewBluetoothServices.adapter = adapter
        binding.textViewDashboardDeviceName.text = "name: " + device?.name
        binding.textViewDashboardDeviceMac.text = "mac: " + device?.address
        binding.textViewDashboardDeviceType.text = "type: " + Scanner.getType(device)
        binding.textViewDashboardDeviceClass.text = "class: " + device?.bluetoothClass
        binding.textViewDashboardDeviceState.text = "state: " + device?.bondState

        return root
    }

    private val gattUpdateReceiver = object : BroadcastReceiver()
    {
        override fun onReceive(context: Context, intent: Intent)
        {
            when (intent.action) {
                Scanner.ACTION_GATT_CONNECTED -> {

                }
                Scanner.ACTION_GATT_DISCONNECTED -> {

                }
                Scanner.ACTION_GATT_SERVICES_DISCOVERED -> {
                    adapter = ExpandableServicesAdapter(requireContext(), connection)
                    _binding?.expandableListServices?.setAdapter(adapter)
                }
                Scanner.ACTION_DATA_AVAILABLE -> {
                    //Toast.makeText(context, "ACTION_DATA_AVAILABLE", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Connection.unregisterReceiver(requireContext(), gattUpdateReceiver)
    }

    override fun servicesDiscovered(connection: Connection)
    {
        Connection.broadcastUpdate(requireContext(), Scanner.ACTION_GATT_SERVICES_DISCOVERED)
    }

    override fun connectionStateChanged(connection: Connection)
    {

    }

    override fun characteristicChanged(connection: Connection, characteristic: BluetoothGattCharacteristic?)
    {

    }

    override fun characteristicWrite(
        connection: Connection,
        characteristic: BluetoothGattCharacteristic?
    )
    {

    }
}