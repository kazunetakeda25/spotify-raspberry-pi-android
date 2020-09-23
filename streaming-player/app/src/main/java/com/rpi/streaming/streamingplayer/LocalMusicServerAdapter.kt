package com.rpi.streaming.streamingplayer

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class LocalMusicServerAdapter(private val localMusicServerItemsList: List<LocalMusicServer>) : RecyclerView.Adapter<LocalMusicServerAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var hostName: TextView = view.findViewById<View>(R.id.hostName) as TextView
        var sharedRootDirectory: TextView = view.findViewById<View>(R.id.sharedRootDirectory) as TextView
        var username: TextView = view.findViewById<View>(R.id.username) as TextView
        var password: TextView = view.findViewById<View>(R.id.password) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_local_music_server_item_row, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val list = localMusicServerItemsList[position]
        holder.hostName.text = list.hostName
        holder.sharedRootDirectory.text = list.sharedRootDirectory!!.removeSuffix("/")
        holder.username.text = list.username
        holder.password.text = "******"
    }

    override fun getItemCount(): Int {
        return localMusicServerItemsList.size
    }
}