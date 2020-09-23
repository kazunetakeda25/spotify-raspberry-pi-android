package com.rpi.streaming.streamingplayer

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.CheckBox
import android.widget.TextView
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class LocalMusicAdapter(private val localMusicItemsList: List<LocalMusic>, private val ipAddress: String?, private val deviceName: String?, private val myWindow: Window)
    : RecyclerView.Adapter<LocalMusicAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var checkBox: CheckBox = view.findViewById<View>(R.id.checkBox) as CheckBox
        var fileName: TextView = view.findViewById<View>(R.id.fileName) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_local_music_item_row, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = localMusicItemsList[position]
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = item.bSelected!!
        holder.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            localMusicItemsList[position].bSelected = isChecked
            myWindow.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            val client = OkHttpClient()
            if (isChecked) {
                val urlBuilder = HttpUrl.parse("http://$ipAddress:3000/local/add")!!.newBuilder()
                urlBuilder.addQueryParameter("categoryDirectory", item.categoryDirectory)
                urlBuilder.addQueryParameter("fileName", item.fileName)
                val url = urlBuilder.build().toString()
                val request = Request.Builder()
                    .url(url)
                    .build()
                Thread(Runnable {
                    try {
                        val call = client.newCall(request)
                        val response = call.execute()
                        if (response.isSuccessful) {
                            myWindow.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        }
                    } catch (e: IOException) {
                        myWindow.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    }
                }).start()
            } else {
                val urlBuilder = HttpUrl.parse("http://$ipAddress:3000/local/remove")!!.newBuilder()
                urlBuilder.addQueryParameter("categoryDirectory", item.categoryDirectory)
                urlBuilder.addQueryParameter("fileName", item.fileName)
                val url = urlBuilder.build().toString()
                val request = Request.Builder()
                    .url(url)
                    .build()
                Thread(Runnable {
                    try {
                        val call = client.newCall(request)
                        val response = call.execute()
                        if (response.isSuccessful) {
                            myWindow.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        }
                    } catch (e: IOException) {
                        myWindow.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    }
                }).start()
            }
        }
        holder.fileName.text = item.fileName
    }

    override fun getItemCount(): Int {
        return localMusicItemsList.size
    }
}