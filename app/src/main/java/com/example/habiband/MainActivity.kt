package com.example.habiband

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.habiband.bluetooth.Connection
import com.example.habiband.bluetooth.Scanner
import com.example.habiband.bootloader.Firmware
import com.example.habiband.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Scanner.start(this)
        Connection.notificationControlRun()

        val intent = intent
        var action = intent.action

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.clipData != null)
        {
            val item = intent.clipData!!.getItemAt(0)
            val uri = item.uri

            Firmware.open(this, uri)
/*
            val reader = BufferedReader(InputStreamReader(contentResolver.openInputStream(uri)))
            Firmware.file.clear()
            while (true){
                val value = reader.read()
                if (value == -1)
                {
                    break;
                }
                OpenedFirmware.hexFile.add(value.toUByte())
            }
            */
        }

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications,
                R.id.navigation_bootloader
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        Scanner.startDiscoverDevices()
    }

    override fun onDestroy() {
        super.onDestroy()
        Scanner.dispose()
        Connection.notificationControlStop()
    }
}