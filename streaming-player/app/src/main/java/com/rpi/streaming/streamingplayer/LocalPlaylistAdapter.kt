package com.rpi.streaming.streamingplayer

import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.google.gson.Gson
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class LocalPlaylistAdapter(private val localPlaylistItems: List<LocalPlaylist>, private val ipAddress: String?, private val deviceName: String?)
    : RecyclerView.Adapter<LocalPlaylistAdapter.MyViewHolder>() {
    private var lastPlayButton: ImageButton? = null
    private var lastStopButton: ImageButton? = null
    private val lastPos = 0
    private var autoStart = true

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var playButton: ImageButton = view.findViewById<View>(R.id.playButton) as ImageButton
        var stopButton: ImageButton = view.findViewById<View>(R.id.stopButton) as ImageButton
        var title: TextView = view.findViewById<View>(R.id.fileName) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_local_playlist_item_row, parent, false)

        return MyViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (localPlaylistItems.count() > 0) {
            val item = localPlaylistItems[position]
            holder.itemView.setOnClickListener(null)
            holder.itemView.setOnClickListener {
                launchNowPlayingActivity(holder.itemView.context, position)
            }
            holder.playButton.setOnClickListener(null)
            holder.stopButton.setOnClickListener(null)
            when {
                item.status == 0 -> {
                    holder.playButton.visibility = View.VISIBLE
                    holder.stopButton.visibility = View.INVISIBLE
                }
                item.status == 1 -> {
                    holder.playButton.visibility = View.INVISIBLE
                    holder.stopButton.visibility = View.VISIBLE
                    lastPlayButton = holder.playButton
                    lastStopButton = holder.stopButton
//                    play(item)
                }
            }
            var isMusicPlaying = false
            for (i in 0 until localPlaylistItems.count()) {
                if (localPlaylistItems[i].status == 1) {
                    isMusicPlaying = true
                }
            }
            val client = OkHttpClient()
            if (!isMusicPlaying) {
                val urlBuilder = HttpUrl.parse("http://$ipAddress:3000/rmcp")!!.newBuilder()
                val url = urlBuilder.build().toString()
                val request = Request.Builder()
                    .url(url)
                    .build()
                Thread(Runnable {
                    try {
                        val call = client.newCall(request)
                        val response = call.execute()
                        if (response.isSuccessful) {
                            localPlaylistItems[lastPos].status = 0
                        }
                    } catch (e: IOException) {
                    }
                }).start()
            }
            holder.playButton.setOnClickListener {
                if (holder.playButton.visibility == View.VISIBLE) {
                    if (lastPlayButton != null)
                    {
                        lastPlayButton!!.visibility = View.VISIBLE
                        lastStopButton!!.visibility = View.INVISIBLE
                        localPlaylistItems[lastPos].status = 0
                    }
                    localPlaylistItems[position].status = 1
                    holder.playButton.visibility = View.INVISIBLE
                    holder.stopButton.visibility = View.VISIBLE
                } else {
                    if (lastPlayButton != null)
                    {
                        lastPlayButton!!.visibility = View.INVISIBLE
                        lastStopButton!!.visibility = View.VISIBLE
                        localPlaylistItems[lastPos].status = 1
                    }
                    localPlaylistItems[position].status = 0
                    holder.playButton.visibility = View.VISIBLE
                    holder.stopButton.visibility = View.INVISIBLE
                }
                lastPlayButton = holder.playButton
                lastStopButton = holder.stopButton
                play(item)
                autoStart = true
            }
            holder.stopButton.setOnClickListener {
                if (lastStopButton != null)
                {
                    lastStopButton!!.visibility = View.INVISIBLE
                    lastPlayButton!!.visibility = View.VISIBLE
                    localPlaylistItems[lastPos].status = 0
                }
                localPlaylistItems[position].status = 0
                holder.stopButton.visibility = View.INVISIBLE
                holder.playButton.visibility = View.VISIBLE
                lastPlayButton = holder.playButton
                lastStopButton = holder.stopButton
                val urlBuilder = HttpUrl.parse("http://$ipAddress/local/pause")!!.newBuilder()
                urlBuilder.addQueryParameter("categoryDirectory", localPlaylistItems[position].categoryDirectory)
                urlBuilder.addQueryParameter("fileName", localPlaylistItems[position].fileName)
                val url = urlBuilder.build().toString()
                val request = Request.Builder()
                    .url(url)
                    .build()
                Thread(Runnable {
                    try {
                        val call = client.newCall(request)
                        val response = call.execute()
                        if (response.isSuccessful) {
                            isMusicPlaying = false
                            for (i in 0 until localPlaylistItems.count()) {
                                if (localPlaylistItems[i].status == 1) {
                                    isMusicPlaying = true
                                }
                            }
                            if (!isMusicPlaying) {
                                val urlBuilder3 = HttpUrl.parse("http://$ipAddress:3000/rmcp")!!.newBuilder()
                                val url3 = urlBuilder3.build().toString()
                                val request3 = Request.Builder()
                                    .url(url3)
                                    .build()
                                Thread(Runnable {
                                    try {
                                        val call3 = client.newCall(request3)
                                        val response3 = call3.execute()
                                        if (response3.isSuccessful) {
                                            localPlaylistItems[lastPos].status = 0
                                        }
                                    } catch (e: IOException) {
                                    }
                                }).start()
                            }
                        }
                    } catch (e: IOException) {
                    }
                }).start()
            }
            holder.title.text = item.fileName
        }
    }

    private fun launchNowPlayingActivity(context: Context, position: Int) {
        val intent = Intent(context, LocalNowPlayingActivity::class.java)
        intent.putExtra("ipAddress", ipAddress)
        intent.putExtra("deviceName", deviceName)
        intent.putExtra("position", position.toString())
        intent.putExtra("playlist", Gson().toJson(localPlaylistItems))
        context.startActivity(intent)
    }

    private fun play(item: LocalPlaylist) {
        Thread(Runnable {
            val client = OkHttpClient()
            val urlBuilder = HttpUrl.parse("http://$ipAddress/local/play")!!.newBuilder()
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
                    }
                } catch (e: IOException) {
                }
            }).start()
        }).start()
    }

    override fun getItemCount(): Int {
        return localPlaylistItems.size
    }
}