package com.minimalist.circle.activity

import android.app.*
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import kotlinx.android.synthetic.main.activity_main.*
import com.minimalist.circle.utils.ConnectThread
import com.minimalist.circle.utils.ConnectedThread
import com.minimalist.circle.utils.AutoCountDown
import com.minimalist.circle.R
import com.minimalist.circle.fragment.AutoDialogFragment
import com.minimalist.circle.fragment.BluetoothDialogFragment
import java.util.UUID

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_ENABLE_BT = 1
        val BLUETOOTH_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")!!
    }

    private lateinit var mBluetoothAdapter: BluetoothAdapter
    lateinit var connectDialog: ProgressDialog
    lateinit var timer: AutoCountDown

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar_main)
        setupButtonClick()
        if(!ConnectThread.isAlive) bluetoothButtonEnabledOnly()
    }

    override fun onStart() {
        super.onStart()
        checkBluetooth()
    }

    override fun onDestroy() {
        super.onDestroy()
        ConnectThread.cancel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) finish()
        }
    }

    private fun setupButtonClick() {
        up.setOnTouchListener{ _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                ConnectedThread.write("u".toByteArray())
            } else if (event.action == MotionEvent.ACTION_UP) {
                ConnectedThread.write("s".toByteArray())
            }
            false
        }

        down.setOnTouchListener{ _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                ConnectedThread.write("d".toByteArray())
            } else if (event.action == MotionEvent.ACTION_UP) {
                ConnectedThread.write("s".toByteArray())
            }
            false
        }

        left.setOnTouchListener{ _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                ConnectedThread.write("l".toByteArray())
            } else if (event.action == MotionEvent.ACTION_UP) {
                ConnectedThread.write("s".toByteArray())
            }
            false
        }

        right.setOnTouchListener{ _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                ConnectedThread.write("r".toByteArray())
            } else if (event.action == MotionEvent.ACTION_UP) {
                ConnectedThread.write("s".toByteArray())
            }
            false
        }

        btn_bluetooth.setOnClickListener { openBluetoothDialog() }

        switch_vacuum.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) ConnectedThread.write("v".toByteArray())
            else ConnectedThread.write("nv".toByteArray())
        }

        btn_auto_off.setOnClickListener {
            ConnectedThread.write("0".toByteArray())
            timer.cancel()
            enableButton()
        }

        btn_auto_time.setOnClickListener { openAutoDialog() }
    }

    private fun checkBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!mBluetoothAdapter.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
        }
    }

    private fun bluetoothButtonEnabledOnly() {
        up.isEnabled = false
        down.isEnabled = false
        left.isEnabled = false
        right.isEnabled = false
        btn_bluetooth.isEnabled = true
        switch_vacuum.isEnabled = false
        btn_auto_off.isEnabled = false
        btn_auto_time.isEnabled = false
    }

    private fun openAutoDialog() {
        val autoFragment = AutoDialogFragment()
        autoFragment.show(supportFragmentManager, "automate")
    }

    fun openBluetoothDialog() {
        val bluetoothFragment = BluetoothDialogFragment()
        bluetoothFragment.show(supportFragmentManager, "bluetooth")
    }

    fun checkConnection() {
        if (ConnectThread.mmSocket!!.isConnected) {
            val frag = supportFragmentManager.findFragmentByTag("bluetooth")
            (frag as BluetoothDialogFragment).dismiss()
            runOnUiThread {
                connectDialog.setMessage("Connection Successfull")
                Handler().postDelayed({ connectDialog.dismiss() }, 2000)
                label_connected_bluetooth.text = "Connected To: ${ConnectThread.device}"
            }
            setupButtonClick(); enableButton()
            ConnectedThread.setup(ConnectThread.mmSocket!!)
            ConnectedThread.start()
            ConnectedThread.write("0".toByteArray())
        } else {
            runOnUiThread {
                connectDialog.setMessage("Connection Failed")
                Handler().postDelayed({ connectDialog.dismiss() }, 2000)
            }
        }
    }

    fun enableButton() {
        up.isEnabled = true
        down.isEnabled = true
        left.isEnabled = true
        right.isEnabled = true
        btn_bluetooth.isEnabled = true
        switch_vacuum.isEnabled = true
        btn_auto_off.isEnabled = false
        label_auto_time.text = resources.getString(R.string.label_auto_time)
        btn_auto_time.isEnabled = true
    }

    fun disableButton() {
        up.isEnabled = false
        down.isEnabled = false
        left.isEnabled = false
        right.isEnabled = false
        btn_bluetooth.isEnabled = false
        switch_vacuum.isEnabled = false
        btn_auto_off.isEnabled = true
        btn_auto_time.isEnabled = false
    }

    fun updatingTime() {
        label_auto_time.text = AutoCountDown.time
    }

}
