package com.minimalist.circle.utils

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.minimalist.circle.activity.MainActivity
import java.io.IOException

object ConnectThread : Thread() {
    var mmSocket: BluetoothSocket? = null
    var device = ""

    fun setup(device: BluetoothDevice) {
        try {
            this.device = device.name
            mmSocket = device.createInsecureRfcommSocketToServiceRecord(MainActivity.BLUETOOTH_UUID)
        } catch (e: IOException) {
        }
    }

    override fun run() {
        try {
            mmSocket?.connect()
        } catch (connectException: IOException) {
            try {
                mmSocket?.close()
            } catch (closeException: IOException) {
            }
        }
    }

    fun cancel() {
        try {
            mmSocket?.close()
        } catch (e: IOException) {
        }
    }

}
