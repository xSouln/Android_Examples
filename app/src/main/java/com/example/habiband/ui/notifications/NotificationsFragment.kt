package com.example.habiband.ui.notifications


import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.habiband.BLEServices
import com.example.habiband.R
import com.example.habiband.bluetooth.Connection
import com.example.habiband.bluetooth.Scanner
import com.example.habiband.databinding.FragmentNotificationsBinding
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.io.File


class NotificationsFragment : Fragment()
{

    private var _binding: FragmentNotificationsBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    //private lateinit var connection: Connection
    private lateinit var notificationsViewModel: NotificationsViewModel

    private enum class SelectedGraph
    {
        Gyroscope,
        Accelerometer,
        ECG,
        PPG
    }

    private var gyroscopeSeriesCoordinateX: LineGraphSeries<DataPoint> = LineGraphSeries()
    private var gyroscopeSeriesCoordinateY: LineGraphSeries<DataPoint> = LineGraphSeries()
    private var gyroscopeSeriesCoordinateZ: LineGraphSeries<DataPoint> = LineGraphSeries()

    private var accelerometerSeriesCoordinateX: LineGraphSeries<DataPoint> = LineGraphSeries()
    private var accelerometerSeriesCoordinateY: LineGraphSeries<DataPoint> = LineGraphSeries()
    private var accelerometerSeriesCoordinateZ: LineGraphSeries<DataPoint> = LineGraphSeries()

    private var ppgSeriesCoordinateRedAc: LineGraphSeries<DataPoint> = LineGraphSeries()
    private var ppgSeriesCoordinateIrAc: LineGraphSeries<DataPoint> = LineGraphSeries()
    private var ppgSeriesCoordinateRedDc: LineGraphSeries<DataPoint> = LineGraphSeries()
    private var ppgSeriesCoordinateIrDc: LineGraphSeries<DataPoint> = LineGraphSeries()

    private var ecgSeriesCoordinateX: LineGraphSeries<DataPoint> = LineGraphSeries()

    private var _xAxisECG: Int = 0
    private var _xAxisPPG: Int = 0
    private var _xAxisAccelerometer: Int = 0
    private var _xAxisGyroscope: Int = 0
    private var selectedGraph = SelectedGraph.Gyroscope

    @SuppressLint("MissingPermission", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        notificationsViewModel = ViewModelProvider(this).get(NotificationsViewModel::class.java)
        //connection = Connection(requireContext())
        //notificationsViewModel.setConnection(connection)
        Connection.setEventListener(notificationsViewModel)
        notificationsViewModel.clear()

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        _xAxisECG = 0
        _xAxisPPG = 0
        _xAxisAccelerometer = 0
        _xAxisGyroscope = 0

        binding.manometerGraphView.viewport.isYAxisBoundsManual = true
        binding.manometerGraphView.viewport.isXAxisBoundsManual = true
        binding.manometerGraphView.viewport.setMinX(0.0)
        binding.manometerGraphView.viewport.setMaxX(200.0)

        val limit = 0x3fff + 1;
        binding.manometerGraphView.viewport.setMinY((limit * -1).toDouble())
        binding.manometerGraphView.viewport.setMaxY(limit.toDouble())

        binding.manometerGraphView.addSeries(gyroscopeSeriesCoordinateX)
        binding.manometerGraphView.addSeries(gyroscopeSeriesCoordinateY)
        binding.manometerGraphView.addSeries(gyroscopeSeriesCoordinateZ)

        gyroscopeSeriesCoordinateX.color = ContextCompat.getColor(requireContext(), R.color.color_gyroscope_coordinate_x)
        gyroscopeSeriesCoordinateY.color = ContextCompat.getColor(requireContext(), R.color.color_gyroscope_coordinate_y)
        gyroscopeSeriesCoordinateZ.color = ContextCompat.getColor(requireContext(), R.color.color_gyroscope_coordinate_z)

        accelerometerSeriesCoordinateX.color = ContextCompat.getColor(requireContext(), R.color.color_accelerometer_coordinate_x)
        accelerometerSeriesCoordinateY.color = ContextCompat.getColor(requireContext(), R.color.color_accelerometer_coordinate_y)
        accelerometerSeriesCoordinateZ.color = ContextCompat.getColor(requireContext(), R.color.color_accelerometer_coordinate_z)

        ppgSeriesCoordinateRedAc.color = ContextCompat.getColor(requireContext(), R.color.color_ppg_red_ac)
        ppgSeriesCoordinateIrAc.color = ContextCompat.getColor(requireContext(), R.color.color_ppg_ir_ac)
        ppgSeriesCoordinateRedDc.color = ContextCompat.getColor(requireContext(), R.color.color_ppg_red_dc)
        ppgSeriesCoordinateIrDc.color = ContextCompat.getColor(requireContext(), R.color.color_ppg_ir_dc)

        ecgSeriesCoordinateX.color = ContextCompat.getColor(requireContext(), R.color.color_ekg)

        val adapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.notifications_graphs_selector,
            android.R.layout.simple_spinner_item
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerNotifications.adapter = adapter

        binding.spinnerNotifications.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener
            {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long
                ) {
                    when(pos){
                        SelectedGraph.Gyroscope.ordinal-> selectGyroscopeGraphs()
                        SelectedGraph.Accelerometer.ordinal -> selectAccelerometerGraphs()
                        SelectedGraph.ECG.ordinal -> selectECGGraphs()
                        SelectedGraph.PPG.ordinal -> selectPPGGraphs()
                    }
                }

                override fun onNothingSelected(arg0: AdapterView<*>?) {

                }
            }

        if (Connection.device != Scanner.selectedDevice && Connection.state == Connection.State.Connected)
        {
            Connection.close()
        }

        Connection.open(Scanner.selectedDevice, requireContext())

        if (Connection.state == Connection.State.Connected)
        {
            Connection.connectedGatt?.requestMtu(256)
            Connection.addNotificationRequest(BLEServices.Temperature.Characteristics.VALUE, true)
            Connection.addNotificationRequest(BLEServices.Gyroscope.Characteristics.POINTS, true)
            Connection.addNotificationRequest(BLEServices.Accelerometer.Characteristics.POINTS, true)
        }

        notificationsViewModel.temperature.observe(viewLifecycleOwner)
        {
            binding.textNotifications.text = "temperature: $it"
        }

        notificationsViewModel.gyroscopePoints.observe(viewLifecycleOwner)
        {
            while (notificationsViewModel.gyroscopeBuffer.totalIndex != notificationsViewModel.gyroscopeBuffer.handlerIndex)
            {
                val point = notificationsViewModel.gyroscopeBuffer.read()
                gyroscopeSeriesCoordinateX.appendData(DataPoint(_xAxisGyroscope.toDouble(), point.x.toDouble()),
                    true,
                    500)

                gyroscopeSeriesCoordinateY.appendData(DataPoint(_xAxisGyroscope.toDouble(), point.y.toDouble()),
                    true,
                    500)

                gyroscopeSeriesCoordinateZ.appendData(DataPoint(_xAxisGyroscope.toDouble(), point.z.toDouble()),
                    true,
                    500)

                _xAxisGyroscope++
            }
        }

        notificationsViewModel.accelerometerPoints.observe(viewLifecycleOwner)
        {
            while (notificationsViewModel.accelerometerBuffer.totalIndex != notificationsViewModel.accelerometerBuffer.handlerIndex)
            {
                val point = notificationsViewModel.accelerometerBuffer.read()

                accelerometerSeriesCoordinateX.appendData(DataPoint(_xAxisAccelerometer.toDouble(), point.x.toDouble()),
                    true,
                    500)

                accelerometerSeriesCoordinateY.appendData(DataPoint(_xAxisAccelerometer.toDouble(), point.y.toDouble()),
                    true,
                    500)

                accelerometerSeriesCoordinateZ.appendData(DataPoint(_xAxisAccelerometer.toDouble(), point.z.toDouble()),
                    true,
                    500)

                _xAxisAccelerometer++
            }
        }

        notificationsViewModel.ppgPoints.observe(viewLifecycleOwner)
        {
            while (notificationsViewModel.ppgBuffer.totalIndex != notificationsViewModel.ppgBuffer.handlerIndex)
            {
                val point = notificationsViewModel.ppgBuffer.read()
                val irAcPoint = point.getIrAcVoltage().toDouble()
                val redAcPoint = point.getRedAcVoltage().toDouble()
                val redDcPoint = point.getRedDcVoltage().toDouble()
                val irDcPoint = point.getIrDcVoltage().toDouble()

                if (selectedGraph == SelectedGraph.PPG){
                    val minMax = notificationsViewModel.ppgBuffer.getMinMaxVoltage(500)

                    binding.manometerGraphView.viewport.setMinY(minMax.min.toDouble())
                    binding.manometerGraphView.viewport.setMaxY(minMax.max.toDouble())
                }

                ppgSeriesCoordinateIrAc.appendData(DataPoint(_xAxisPPG.toDouble(), irAcPoint),
                    true,
                    500)

                ppgSeriesCoordinateRedAc.appendData(DataPoint(_xAxisPPG.toDouble(), redAcPoint),
                    true,
                    500)

                ppgSeriesCoordinateRedDc.appendData(DataPoint(_xAxisPPG.toDouble(), redDcPoint),
                    true,
                    500)

                ppgSeriesCoordinateIrDc.appendData(DataPoint(_xAxisPPG.toDouble(), irDcPoint),
                    true,
                    500)

                _xAxisPPG++
            }
        }

        notificationsViewModel.ecgPoints.observe(viewLifecycleOwner){
            while (notificationsViewModel.ecgBuffer.totalIndex != notificationsViewModel.ecgBuffer.handlerIndex)
            {
                val point = notificationsViewModel.ecgBuffer.read()
                val value = point.getVoltage().toDouble()

                if (selectedGraph == SelectedGraph.ECG){
                    //val minMax = notificationsViewModel.ppgBuffer.getMinMaxVoltage(500)

                    //binding.manometerGraphView.viewport.setMinY(minMax.min.toDouble())
                    //binding.manometerGraphView.viewport.setMaxY(minMax.max.toDouble())
                }

                ecgSeriesCoordinateX.appendData(DataPoint(_xAxisECG.toDouble(), value),
                    true,
                    500)

                _xAxisECG++
            }
        }

        return root
    }

    private fun selectGyroscopeGraphs(){
        selectedGraph = SelectedGraph.Gyroscope
        binding.manometerGraphView.series.clear()
        val limit = 0x3fff + 1;
        binding.manometerGraphView.viewport.setMinY((limit * -1).toDouble())
        binding.manometerGraphView.viewport.setMaxY(limit.toDouble())

        binding.manometerGraphView.addSeries(gyroscopeSeriesCoordinateX)
        binding.manometerGraphView.addSeries(gyroscopeSeriesCoordinateY)
        binding.manometerGraphView.addSeries(gyroscopeSeriesCoordinateZ)

        Connection.addNotificationRequest(BLEServices.Gyroscope.Characteristics.POINTS, true)
    }

    private fun selectAccelerometerGraphs(){
        selectedGraph = SelectedGraph.Accelerometer
        binding.manometerGraphView.series.clear()
        val limit = 0x3fff + 1;
        binding.manometerGraphView.viewport.setMinY((limit * -1).toDouble())
        binding.manometerGraphView.viewport.setMaxY(limit.toDouble())
        binding.manometerGraphView.addSeries(accelerometerSeriesCoordinateX)
        binding.manometerGraphView.addSeries(accelerometerSeriesCoordinateY)
        binding.manometerGraphView.addSeries(accelerometerSeriesCoordinateZ)

        Connection.addNotificationRequest(BLEServices.Accelerometer.Characteristics.POINTS, true)
    }

    private fun selectECGGraphs(){
        selectedGraph = SelectedGraph.ECG
        binding.manometerGraphView.series.clear()

        val limit = 1000.0;
        binding.manometerGraphView.viewport.setMinY(limit * -1)
        binding.manometerGraphView.viewport.setMaxY(limit)

        binding.manometerGraphView.addSeries(ecgSeriesCoordinateX)

        Connection.addNotificationRequest(BLEServices.ECG.Characteristics.ECG_POINTS, true)
    }

    private fun selectPPGGraphs(){
        selectedGraph = SelectedGraph.PPG
        binding.manometerGraphView.series.clear()
        val limit = 1000.0;
        binding.manometerGraphView.viewport.setMinY(limit * -1)
        binding.manometerGraphView.viewport.setMaxY(limit)

        binding.manometerGraphView.addSeries(ppgSeriesCoordinateIrAc)
        binding.manometerGraphView.addSeries(ppgSeriesCoordinateIrDc)
        binding.manometerGraphView.addSeries(ppgSeriesCoordinateRedAc)
        binding.manometerGraphView.addSeries(ppgSeriesCoordinateRedDc)

        Connection.addNotificationRequest(BLEServices.ECG.Characteristics.PPG_POINTS, true)
    }

    private val gattUpdateReceiver = object : BroadcastReceiver()
    {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent)
        {
            when (intent.action) {
                Scanner.ACTION_GATT_CONNECTED -> {
                    Connection.connectedGatt?.requestMtu(256)
                }
                Scanner.ACTION_GATT_DISCONNECTED -> {

                }
                Scanner.ACTION_GATT_SERVICES_DISCOVERED -> {
                    Connection.connectedGatt?.requestMtu(256)
                    Thread.sleep(100)
                    Connection.addNotificationRequest(BLEServices.Temperature.Characteristics.VALUE, true)
                    Connection.addNotificationRequest(BLEServices.Gyroscope.Characteristics.POINTS, true)
                    Connection.addNotificationRequest(BLEServices.Accelerometer.Characteristics.POINTS, true)
                }
                Scanner.ACTION_DATA_AVAILABLE -> {

                }
            }
        }
    }

    override fun onStart() {
        //connection.registerReceiver(gattUpdateReceiver)
        when(selectedGraph){
            SelectedGraph.Gyroscope-> selectGyroscopeGraphs()
            SelectedGraph.Accelerometer -> selectAccelerometerGraphs()
            SelectedGraph.ECG -> selectECGGraphs()
            SelectedGraph.PPG -> selectPPGGraphs()
        }
        Connection.addNotificationRequest(BLEServices.Temperature.Characteristics.VALUE, true)
        Connection.addNotificationRequest(BLEServices.Gyroscope.Characteristics.POINTS, true)
        Connection.addNotificationRequest(BLEServices.Accelerometer.Characteristics.POINTS, true)
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        //connection.unregisterReceiver(gattUpdateReceiver)
        Connection.addNotificationRequest(BLEServices.Temperature.Characteristics.VALUE, false)
        Connection.addNotificationRequest(BLEServices.Gyroscope.Characteristics.POINTS, false)
        Connection.addNotificationRequest(BLEServices.Accelerometer.Characteristics.POINTS, false)
        Connection.addNotificationRequest(BLEServices.ECG.Characteristics.ECG_POINTS, false)
        Connection.addNotificationRequest(BLEServices.ECG.Characteristics.PPG_POINTS, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}