package com.minimalist.circle.fragment

import android.annotation.TargetApi
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import kotlinx.android.synthetic.main.dialog_bluetooth.*
import com.minimalist.circle.utils.ConnectThread
import com.minimalist.circle.activity.MainActivity
import com.minimalist.circle.R

class BluetoothDialogFragment : DialogFragment() {

    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private lateinit var pairedDevices: Set<BluetoothDevice>
    private var mBluetoothDevice: ArrayList<BluetoothDevice> = arrayListOf()
    private var mBluetoothName: ArrayList<String> = arrayListOf()
    private var mBluetoothImage: ArrayList<Drawable> = arrayListOf()

    private val mReceiver = object : BroadcastReceiver() {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        override fun onReceive(p0: Context?, p1: Intent?) {
            when(p1?.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> progress_bar_dialog_bluetooth.visibility = View.VISIBLE
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> progress_bar_dialog_bluetooth.visibility = View.GONE
                BluetoothDevice.ACTION_FOUND -> {
                    val device = p1.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (device.bondState != BluetoothDevice.BOND_BONDED) {
                        mBluetoothDevice.add(device)
                        mBluetoothName.add(device.name)
                        mBluetoothImage.add(getDrawableByMajorClass(device.bluetoothClass.majorDeviceClass))
                        list_view_unpaired.adapter = com.minimalist.circle.adapter.BluetoothAdapter(requireContext(), mBluetoothDevice, mBluetoothName, mBluetoothImage)
                        list_view_unpaired.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                            mBluetoothAdapter.cancelDiscovery()
                            val device = list_view_unpaired.getItemAtPosition(position) as BluetoothDevice
                            try {
                                // failed to use this method
                                // val method = device.javaClass.getMethod("createBond", null as Class<*>?)
                                // method.invoke(device, null as Any?)
                                device.createBond()
                            } catch (e: Exception) {
                                Toast.makeText(activity, "$e", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device = p1.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    when(device.bondState) {
                        BluetoothDevice.BOND_BONDING -> progress_bar_dialog_bluetooth.visibility = View.VISIBLE
                        BluetoothDevice.BOND_BONDED -> {
                            progress_bar_dialog_bluetooth.visibility = View.GONE
                            val frag = activity?.supportFragmentManager?.findFragmentByTag("bluetooth") as DialogFragment
                            frag.dismiss()
                            (activity as MainActivity).openBluetoothDialog()
                        }
                        BluetoothDevice.BOND_NONE -> progress_bar_dialog_bluetooth.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_bluetooth, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupBluetoothReceiver()
        pairedDevicesList()
        btn_scan.setOnClickListener { mBluetoothAdapter.startDiscovery() }
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).unregisterReceiver(mReceiver)
    }

    private fun setupBluetoothReceiver() {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        }
        (activity as MainActivity).registerReceiver(mReceiver, filter)
    }

    private fun pairedDevicesList() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        pairedDevices = mBluetoothAdapter.bondedDevices
        if (pairedDevices.isNotEmpty()) {
            for (device in pairedDevices) {
                mBluetoothDevice.add(device)
                mBluetoothName.add(device.name)
                mBluetoothImage.add(getDrawableByMajorClass(device.bluetoothClass.majorDeviceClass))
            }
            val adapter = com.minimalist.circle.adapter.BluetoothAdapter(requireContext(), mBluetoothDevice, mBluetoothName, mBluetoothImage)
            list_view_paired.adapter = adapter
            list_view_paired.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                mBluetoothAdapter.cancelDiscovery()
                val device = list_view_paired.getItemAtPosition(position) as BluetoothDevice
                if (ConnectThread.isAlive) ConnectThread.cancel()
                ConnectThread.setup(device)
                ConnectThread.run()
                (activity as MainActivity).connectDialog = ProgressDialog(activity as MainActivity)
                (activity as MainActivity).connectDialog.setMessage("Start Connecting ...")
                (activity as MainActivity).connectDialog.show()
                (activity as MainActivity).checkConnection()
            }
        }
    }

    private fun getDrawableByMajorClass(major: Int): Drawable {
        return when (major) {
            BluetoothClass.Device.Major.COMPUTER -> ResourcesCompat.getDrawable(resources, R.drawable.computer_icon, null)!!
            BluetoothClass.Device.Major.PHONE -> ResourcesCompat.getDrawable(resources, R.drawable.phone_icon, null)!!
            else -> ResourcesCompat.getDrawable(resources, R.drawable.device_icon, null)!!
        }
    }

}
