package com.rpi.streaming.streamingplayer

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_tunein_now_playing.*
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object: TypeToken<T>() {}.type)!!

class TuneInNowPlayingActivity : AppCompatActivity() {
    companion object {
        private
        var curPos: Int = 0
        private
        var tuneInPlayList: java.util.ArrayList<TuneInPlaylist> = java.util.ArrayList()
    }
    private var ipAddress: String? = null
    private var deviceNameFromIntent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tunein_now_playing)
        curPos = intent.getStringExtra("position").toInt()
        tuneInPlayList = Gson().fromJson(intent.getStringExtra("playlist"))
        ipAddress = intent.getStringExtra("ipAddress")
        deviceNameFromIntent = intent.getStringExtra("deviceName")
        deviceName.text = "$deviceNameFromIntent/$ipAddress"
        playSeekBar.isEnabled = false
        musicLength.text = "LIVE"
        Glide.with(this).load(tuneInPlayList[curPos].image).into(musicImage)
        fileName.text = tuneInPlayList[curPos].title
        musicSubTitle.text = tuneInPlayList[curPos].subTitle
        Log.d("STATUS", tuneInPlayList[curPos].status.toString())
        if (tuneInPlayList[curPos].status == 0) {
            playButton.visibility = View.VISIBLE
            stopButton.visibility = View.INVISIBLE
        }
        else if (tuneInPlayList[curPos].status == 1) {
            playButton.visibility = View.INVISIBLE
            stopButton.visibility = View.VISIBLE
        }
        var position = curPos
        stopButton.setOnClickListener(null)
        stopButton.setOnClickListener {
            val client1 = OkHttpClient()
            val request1 = Request.Builder()
                .url(tuneInPlayList[position].href)
                .build()
            Thread(Runnable {
                try {
                    val call1 = client1.newCall(request1)
                    val response1 = call1.execute()
                    if (response1.isSuccessful) {
                        val client = OkHttpClient()
                        val urlBuilder = HttpUrl.parse("http://$ipAddress:3000/tunein/pause")!!.newBuilder()
                        urlBuilder.addQueryParameter("url", tuneInPlayList[position].url)
                        val url = urlBuilder.build().toString()
                        val request = Request.Builder()
                            .url(url)
                            .build()
                        Thread(Runnable {
                            var responseStatus = false
                            this@TuneInNowPlayingActivity.run {
                                try {
                                    val call = client.newCall(request)
                                    val response = call.execute()
                                    if (response.isSuccessful) {
                                        responseStatus = true
                                    }
                                } catch (e: IOException) {
                                }
                            }
                            this@TuneInNowPlayingActivity.runOnUiThread {
                                if (responseStatus) {
                                    playButton.visibility = View.VISIBLE
                                    stopButton.visibility = View.INVISIBLE
                                }
                            }
                        }).start()
                    }
                } catch (e: IOException) {
                }
            }).start()
            val client2 = OkHttpClient()
            val urlBuilder2 = HttpUrl.parse("http://$ipAddress:3000/rmcp")!!.newBuilder()
            val url2 = urlBuilder2.build().toString()
            val request2 = Request.Builder()
                .url(url2)
                .build()
            Thread(Runnable {
                try {
                    val call2 = client2.newCall(request2)
                    val response2 = call2.execute()
                    if (response2.isSuccessful) {
                        tuneInPlayList[position].status = 0
                    }
                } catch (e: IOException) {
                }
            }).start()
        }
        playButton.setOnClickListener(null)
        playButton.setOnClickListener {
            val client1 = OkHttpClient()
            val request1 = Request.Builder()
                .url(tuneInPlayList[position].href)
                .build()
            Thread(Runnable {
                try {
                    Log.d("TuneInNowPlaying+pos", position.toString())
                    Log.d("TuneInNowPlaying+list", tuneInPlayList[position].toString())
                    val call1 = client1.newCall(request1)
                    val response1 = call1.execute()
                    if (response1.isSuccessful) {
                        val client = OkHttpClient()
                        val urlBuilder = HttpUrl.parse("http://$ipAddress:3000/tunein/play")!!.newBuilder()
                        urlBuilder.addQueryParameter("title", tuneInPlayList[position].title)
                        urlBuilder.addQueryParameter("href", tuneInPlayList[position].href)
                        urlBuilder.addQueryParameter("url", tuneInPlayList[position].url)
                        val url = urlBuilder.build().toString()
                        val request = Request.Builder()
                            .url(url)
                            .build()
                        Thread(Runnable {
                            var responseStatus = false
                            this@TuneInNowPlayingActivity.run {
                                try {
                                    val call = client.newCall(request)
                                    val response = call.execute()
                                    if (response.isSuccessful) {
                                        responseStatus = true
                                    }
                                } catch (e: IOException) {
                                }
                            }
                            this@TuneInNowPlayingActivity.runOnUiThread {
                                if (responseStatus) {
                                    playButton.visibility = View.INVISIBLE
                                    stopButton.visibility = View.VISIBLE
                                }
                            }
                        }).start()
                    }
                } catch (e: IOException) {
                }
            }).start()
        }
        previousButton.setOnClickListener {
            position--
            if (position < 0)
                position = tuneInPlayList.count() - 1
            nowPlayingItemDisplay(position)
        }
        nextButton.setOnClickListener {
            position++
            if (position >= tuneInPlayList.count())
                position = 0
            nowPlayingItemDisplay(position)
        }
        playlistButton.setOnClickListener {
            lanuchTuneInPlaylistActivity()
        }
    }

    private fun nowPlayingItemDisplay(pos: Int) {
        Glide.with(this).load(tuneInPlayList[pos].image).into(musicImage)
        fileName.text = tuneInPlayList[pos].title
        musicSubTitle.text = tuneInPlayList[pos].subTitle
        val client1 = OkHttpClient()
        val request1 = Request.Builder()
            .url(tuneInPlayList[pos].href)
            .build()
        Thread(Runnable {
            try {
                val call1 = client1.newCall(request1)
                val response1 = call1.execute()
                if (response1.isSuccessful) {
                    val streamUrl = response1.body()!!.string()
                    val client = OkHttpClient()
                    val urlBuilder = HttpUrl.parse("http://$ipAddress:3000/tunein/play")!!.newBuilder()
                    urlBuilder.addQueryParameter("title", tuneInPlayList[pos].title)
                    urlBuilder.addQueryParameter("href", tuneInPlayList[pos].href)
                    urlBuilder.addQueryParameter("url", streamUrl)
                    val url = urlBuilder.build().toString()
                    val request = Request.Builder()
                        .url(url)
                        .build()
                    Thread(Runnable {
                        var responseStatus = false
                        this@TuneInNowPlayingActivity.run {
                            try {
                                val call = client.newCall(request)
                                val response = call.execute()
                                if (response.isSuccessful) {
                                    responseStatus = true
                                }
                            } catch (e: IOException) {
                            }
                        }
                        this@TuneInNowPlayingActivity.runOnUiThread {
                            if (responseStatus) {
                                playButton.visibility = View.INVISIBLE
                                stopButton.visibility = View.VISIBLE
                            }
                        }
                    }).start()
                }
            } catch (e: IOException) {
            }
        }).start()
    }

    private fun lanuchTuneInPlaylistActivity() {
        val intent = Intent(this, TuneInPlaylistActivity::class.java)
        intent.putExtra("ipAddress", ipAddress)
        intent.putExtra("deviceName", deviceNameFromIntent)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val addSourceItem: MenuItem = menu.findItem(R.id.action_add_source)
        addSourceItem.isVisible = false
        val addDeviceItem: MenuItem = menu.findItem(R.id.action_add_device)
        addDeviceItem.isVisible = false
        val playlistItem: MenuItem = menu.findItem(R.id.action_playlist)
        playlistItem.isVisible = false
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when {
            item.itemId == R.id.action_main -> {
                val intent = Intent(this, ConnectedDevicesListingActivity::class.java)
                startActivity(intent)
                true
            }
            item.itemId == R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}