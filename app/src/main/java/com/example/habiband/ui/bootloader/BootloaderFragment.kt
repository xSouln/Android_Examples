package com.example.habiband.ui.bootloader

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.habiband.BLEServices
import com.example.habiband.bluetooth.Connection
import com.example.habiband.bluetooth.Scanner
import com.example.habiband.bluetooth.interfaces.IRequestConnect
import com.example.habiband.bootloader.Firmware
import com.example.habiband.databinding.FragmentBootloaderBinding

class BootloaderFragment : Fragment(), IRequestConnect
{

    private var _binding: FragmentBootloaderBinding? = null
    private val binding get() = _binding!!
    private var connection: Connection? = null


    @SuppressLint("SetTextI18n", "MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        val bootloaderViewModel = ViewModelProvider(this).get(BootloaderViewModel::class.java)

        _binding = FragmentBootloaderBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //adapter = ServicesAdapter(requireContext(), Control.connection?.gatt!!)
        val device = Scanner.selectedDevice
        //bootloaderViewModel.notificationUpdateStateEnable()
        Connection.setEventListener(bootloaderViewModel)

        bootloaderViewModel.bindModel(this)

        if (Connection.device != Scanner.selectedDevice && Connection.state == Connection.State.Connected)
        {
            Connection.close()
        }

        Connection.open(device, requireContext())

        //Connection.addNotificationRequest(connection?.gatt, BLEServices.Bootloader.Characteristics.PORT_TX, true)
        //val device = Control.connection?.gatt?.device

        bootloaderViewModel.observeBootloaderStatus.observe(viewLifecycleOwner)
        {
            binding.textBootloaderOperation.text = "Operation: " + bootloaderViewModel.bootloaderStatus.operation.toString()
            //binding.textBootloaderOperationInProgress.text = "OperationInProgress: " + bootloaderViewModel.bootloaderStatus.operationInProgress.toString()
            binding.textBootloaderInBoot.text = "InBootSection: " + bootloaderViewModel.bootloaderStatus.inBootSection.toString()
            binding.textBootloaderOperationResult.text = "OperationResult: " + bootloaderViewModel.bootloaderStatus.operationResult.toString()
            //binding.textBootloaderSessionKeyConfirmed.text = "SessionKeyConfirmed: " + bootloaderViewModel.bootloaderStatus.sessionKeyConfirmed.toString()
            binding.textBootloaderFirmwareLoadingInit.text = "FirmwareLoadingInit: " + bootloaderViewModel.bootloaderStatus.firmwareLoadingInit.toString()
        }

        bootloaderViewModel.observeFirmwareLoadingStatus.observe(viewLifecycleOwner)
        {
            binding.textBootloaderLoadedImageSize.text = "LoadedImageSize: " + bootloaderViewModel.firmwareLoadingStatus.loadedImageSize
            binding.textBootloaderLoadedImageCrc.text = "LoaderImageCRC: 0x" + bootloaderViewModel.firmwareLoadingStatus.loadedImageCrc.toString(16)
        }

        bootloaderViewModel.observeSessionKey.observe(viewLifecycleOwner)
        {
            binding.textBootloaderSessionKey.text = "SessionKey: 0x" + bootloaderViewModel.sessionKey.toString(16)
        }

        Firmware.observeSize.observe(viewLifecycleOwner)
        {
            binding.textBootloaderImageSize.text = "ImageSize: " + Firmware.size
        }

        Firmware.observePath.observe(viewLifecycleOwner)
        {
            binding.textBootloaderFirmwarePath.text = "Path: " + Firmware.path
        }

        Firmware.observeCRC.observe(viewLifecycleOwner)
        {
            binding.textBootloaderImageCrc.text = "ImageCRC: 0x" + Firmware.CRC.toString(16)
        }

        binding.buttonStartApp.setOnClickListener {
            bootloaderViewModel.startApp()
        }

        binding.buttonStartBoot.setOnClickListener {
            bootloaderViewModel.startBoot()
        }

        binding.buttonStartLoadFirmware.setOnClickListener {
            bootloaderViewModel.startUpdate()
        }

        return root
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
        Connection.addNotificationRequest(BLEServices.Bootloader.Characteristics.PORT_TX, false)
        Connection.addNotificationRequest(BLEServices.Bootloader.Characteristics.STATUS, false)
    }

    override fun Connect()
    {
        Connection.open( Scanner.selectedDevice, requireContext())
    }
}