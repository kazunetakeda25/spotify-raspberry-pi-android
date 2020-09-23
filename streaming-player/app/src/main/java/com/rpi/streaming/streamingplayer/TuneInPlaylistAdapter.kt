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
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class TuneInPlaylistAdapter(private val tuneInPlaylistItems: List<TuneInPlaylist>, private val ipAddress: String?, private val deviceName: String?)
    : RecyclerView.Adapter<TuneInPlaylistAdapter.MyViewHolder>() {

    private var lastPlayButton: ImageButton? = null
    private var lastStopButton: ImageButton? = null
    private val lastPos = 0
    private var autoStart = true

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var playButton: ImageButton = view.findViewById<View>(R.id.playButton) as ImageButton
        var stopButton: ImageButton = view.findViewById<View>(R.id.stopButton) as ImageButton
        var image: ImageView = view.findViewById<View>(R.id.musicImage) as ImageView
        var title: TextView = view.findViewById<View>(R.id.fileName) as TextView
        var subImage: ImageView = view.findViewById<View>(R.id.subImage) as ImageView
        var subTitle: TextView = view.findViewById<View>(R.id.subTitle) as TextView
        var href: String? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_tunein_playlist_item_row, parent, false)

        return MyViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (tuneInPlaylistItems.count() > 0) {
            val item = tuneInPlaylistItems[position]
            holder.itemView.setOnClickListener(null)
            holder.itemView.setOnClickListener {
                launchNowPlayingActivity(holder.itemView.context, position)
            }
            holder.playButton.setOnClickListener(null)
            holder.stopButton.setOnClickListener(null)
            val client1 = OkHttpClient()
            val request1 = Request.Builder()
                .url(item.href)
                .build()
            Thread(Runnable {
                try {
                    val call1 = client1.newCall(request1)
                    val response1 = call1.execute()
                    if (response1.isSuccessful) {
                        tuneInPlaylistItems[position].url = response1.body()!!.string()
                    }
                } catch (e: Exception) {
                }
            }).start()
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
            for (i in 0 until tuneInPlaylistItems.count()) {
                if (tuneInPlaylistItems[i].status == 1) {
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
                            tuneInPlaylistItems[lastPos].status = 0
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
                        tuneInPlaylistItems[lastPos].status = 0
                    }
                    tuneInPlaylistItems[position].status = 1
                    holder.playButton.visibility = View.INVISIBLE
                    holder.stopButton.visibility = View.VISIBLE
                } else {
                    if (lastPlayButton != null)
                    {
                        lastPlayButton!!.visibility = View.INVISIBLE
                        lastStopButton!!.visibility = View.VISIBLE
                        tuneInPlaylistItems[lastPos].status = 1
                    }
                    tuneInPlaylistItems[position].status = 0
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
                    tuneInPlaylistItems[lastPos].status = 0
                }
                tuneInPlaylistItems[position].status = 0
                holder.stopButton.visibility = View.INVISIBLE
                holder.playButton.visibility = View.VISIBLE
                lastPlayButton = holder.playButton
                lastStopButton = holder.stopButton
                val urlBuilder = HttpUrl.parse("http://$ipAddress:3000/tunein/pause")!!.newBuilder()
                urlBuilder.addQueryParameter("url", tuneInPlaylistItems[position].url)
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
                            for (i in 0 until tuneInPlaylistItems.count()) {
                                if (tuneInPlaylistItems[i].status == 1) {
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
                                            tuneInPlaylistItems[lastPos].status = 0
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
            Glide.with(holder.itemView.context).load(item.image).into(holder.image)
            holder.title.text = item.title
            Glide.with(holder.itemView.context).load(item.subImage).into(holder.subImage)
            holder.subTitle.text = item.subTitle
            holder.href = item.href
        }
    }

    private fun launchNowPlayingActivity(context: Context, position: Int) {
        val intent = Intent(context, TuneInNowPlayingActivity::class.java)
        intent.putExtra("ipAddress", ipAddress)
        intent.putExtra("deviceName", deviceName)
        intent.putExtra("position", position.toString())
        intent.putExtra("playlist", Gson().toJson(tuneInPlaylistItems))
        context.startActivity(intent)
    }

    private fun play(item: TuneInPlaylist) {
        val client1 = OkHttpClient()
        val request1 = Request.Builder()
            .url(item.href)
            .build()
        Thread(Runnable {
            try {
                val call1 = client1.newCall(request1)
                val response1 = call1.execute()
                if (response1.isSuccessful) {
                    item.url = response1.body()!!.string()
                    val client = OkHttpClient()
                    val urlBuilder = HttpUrl.parse("http://$ipAddress:3000/tunein/play")!!.newBuilder()
                    urlBuilder.addQueryParameter("title", item.title)
                    urlBuilder.addQueryParameter("href", item.href)
                    urlBuilder.addQueryParameter("url", item.url)
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
                }
            } catch (e: IOException) {
            }
        }).start()
    }

    override fun getItemCount(): Int {
        return tuneInPlaylistItems.size
    }
}