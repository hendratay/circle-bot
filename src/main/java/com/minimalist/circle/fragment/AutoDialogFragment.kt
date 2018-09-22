package com.minimalist.circle.fragment

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import kotlinx.android.synthetic.main.dialog_automate.view.*
import com.minimalist.circle.utils.ConnectedThread
import com.minimalist.circle.utils.AutoCountDown
import com.minimalist.circle.R
import com.minimalist.circle.activity.MainActivity

class AutoDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstance: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = (activity as MainActivity).layoutInflater.inflate(R.layout.dialog_automate, null)
        builder.setView(view)
        view.btn_send.setOnClickListener {
            if (!view.input_time.text.isNullOrEmpty()) {
                ConnectedThread.write(view.input_time.text.toString().toByteArray())
                (activity as MainActivity).timer = AutoCountDown(requireContext(),view.input_time.text.toString().toLong() * 1000 * 60
                        , 1000)
                (activity as MainActivity).timer.start()
                (activity as MainActivity).disableButton()
                dialog.dismiss()
            }
        }
        return builder.create()
    }

}
