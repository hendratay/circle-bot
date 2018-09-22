package com.minimalist.circle.adapter

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.item_bluetooth.view.*
import com.minimalist.circle.R

class BluetoothAdapter(private val mContext: Context,
                       bluetooth: ArrayList<BluetoothDevice>,
                       private val mNames: ArrayList<String>,
                       private val mImages: ArrayList<Drawable>):
        ArrayAdapter<BluetoothDevice>(mContext, R.layout.item_bluetooth, bluetooth) {

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val layoutView = LayoutInflater.from(context).inflate(R.layout.item_bluetooth, parent, false)
        layoutView.txt_bluetooth_name.text = mNames[position]
        layoutView.ic_bluetooth_image.setImageDrawable(mImages[position])
        return layoutView
    }

}

