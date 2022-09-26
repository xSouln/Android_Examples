package com.example.habiband.ui.notifications

import android.bluetooth.BluetoothGattCharacteristic
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
import com.example.habiband.databinding.FragmentNotificationsBinding
import com.example.habiband.ui.dashboard.ExpandableServicesAdapter

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var connection: Connection? = null
    private var characteristic: BluetoothGattCharacteristic? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel = ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val device = Control.selectedDevice
        connection = Connection(requireContext())
        notificationsViewModel.setConnection(connection)
        connection?.registerReceiver(gattUpdateReceiver)
        connection?.open(device)

        notificationsViewModel.temperature.observe(viewLifecycleOwner) {
            binding.textNotifications.text = it.toString()
        }
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
                    characteristic = connection?.findCharacteristic("0000c101-8e22-4541-9d4c-21edae82ed19")
                    if(characteristic != null){
                        connection?.setNotification(characteristic)
                    }
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