package com.example.habiband.ui.home

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.habiband.R
import com.example.habiband.bluetooth.Connection
import com.example.habiband.bluetooth.Control
import com.example.habiband.databinding.FragmentHomeBinding
import com.example.habiband.ui.dashboard.DashboardFragment

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var bluetoothDeviceAdapter: HomeAdapter? = null

    private var count : Int = 0

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome

        Control.setEventListener(homeViewModel)

        bluetoothDeviceAdapter = HomeAdapter(requireContext(), homeViewModel.getDevices())

        binding.bluetoothDevicesListView.adapter = bluetoothDeviceAdapter

        homeViewModel.getDevices().observe(viewLifecycleOwner, Observer {
            bluetoothDeviceAdapter?.notifyDataSetChanged()
        })

        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        binding.buttonFindDevices.setOnClickListener {
            count++
            Control.resetDiscoverDevices()
        }

        binding.buttonCloseConnections.setOnClickListener{
            for (gatt in Control.connectedDevices){
                gatt.close()
            }
            Control.connectedDevices.clear()
        }

        binding.bluetoothDevicesListView.setOnItemClickListener { parent, view, position, id ->
            val element = bluetoothDeviceAdapter?.getItem(position) as BluetoothDevice
            Control.selectedDevice = element
            //Control.connection?.open(element)
            /*
            val bundle = Bundle()
            val fragment = DashboardFragment()
            bundle.putBinder("qwerty", )
            val fragmentTransaction = childFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.navigation_home, DashboardFragment())
            fragmentTransaction.commit()
            */
        }

        return root
    }

    override fun onDestroy() {
        super.onDestroy()
        Control.setEventListener(null)
    }
}