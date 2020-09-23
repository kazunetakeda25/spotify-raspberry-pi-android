package com.rpi.streaming.streamingplayer

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class ConnectedDevicesAdapter(private val connectedDevicesList: List<ConnectedDevices>) : RecyclerView.Adapter<ConnectedDevicesAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var ipAddress: TextView = view.findViewById<View>(R.id.ipAddress) as TextView
        var deviceName: TextView = view.findViewById<View>(R.id.deviceName) as TextView
        var status: TextView = view.findViewById<View>(R.id.status) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_device_item_row, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val list = connectedDevicesList[position]
        holder.ipAddress.text = list.ipAddress
        holder.deviceName.text = list.deviceName
        holder.status.text = list.status
    }

    override fun getItemCount(): Int {
        return connectedDevicesList.size
    }
}