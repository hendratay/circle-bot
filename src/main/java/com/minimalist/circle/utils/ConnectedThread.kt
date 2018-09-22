package com.minimalist.circle.utils

import android.bluetooth.BluetoothSocket
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object ConnectedThread : Thread() {
    private var mmInStream: InputStream? = null
    private var mmOutStream: OutputStream? = null
    var message: String? = null

    fun setup(socket: BluetoothSocket) {
        try {
            mmInStream = socket.inputStream
            mmOutStream = socket.outputStream
        } catch (e: IOException) {
        }
    }

    fun write(bytes: ByteArray) {
        try {
            mmOutStream?.write(bytes)
        } catch (e: IOException) {
        }
    }

}
