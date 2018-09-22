package com.minimalist.circle.utils

import android.content.Context
import android.os.CountDownTimer
import com.minimalist.circle.activity.MainActivity
import java.util.concurrent.TimeUnit

class AutoCountDown(private val context: Context, millisInFuture: Long, countDownInterval: Long): CountDownTimer(millisInFuture, countDownInterval) {

    companion object {
        var time = ""
    }

    override fun onTick(millisUntilFinished: Long) {
        val hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % TimeUnit.MINUTES.toSeconds(1))
        time = hms
        (context as MainActivity).updatingTime()
    }

    override fun onFinish() {
        ConnectedThread.write("0".toByteArray())
        (context as MainActivity).enableButton()
    }

}
