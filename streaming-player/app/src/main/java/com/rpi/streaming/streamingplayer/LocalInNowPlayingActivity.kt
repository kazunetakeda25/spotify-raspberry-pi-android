package com.rpi.streaming.streamingplayer

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_local_now_playing.*
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.*

class LocalNowPlayingActivity : AppCompatActivity() {
    companion object {
        private
        var curPos: Int = 0
        private
        var localPlaylist: ArrayList<LocalPlaylist> = ArrayList()
    }
    private var handler = Handler()
    private var musicLengthInSecs = 0
    private var seekPositionInSecs = 0
    private var ipAddress: String? = null
    private var deviceNameFromIntent: String? = null

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_now_playing)
        curPos = intent.getStringExtra("position").toInt()
        localPlaylist = Gson().fromJson(intent.getStringExtra("playlist"))
        ipAddress = intent.getStringExtra("ipAddress")
        deviceNameFromIntent = intent.getStringExtra("deviceName")
        deviceName.text = "$deviceNameFromIntent/$ipAddress:3000"
        seekPos.text = "00:00:00"
        musicLength.text = "00:00:00"
        playSeekBar.progress = 0
        playSeekBar.max = 0
        fileName.text = localPlaylist[curPos].fileName
        val client = OkHttpClient()
        val urlBuilder0 = HttpUrl.parse("http://$ipAddress:3000/local/length")!!.newBuilder()
        urlBuilder0.addQueryParameter("categoryDirectory", localPlaylist[curPos].categoryDirectory)
        urlBuilder0.addQueryParameter("fileName", localPlaylist[curPos].fileName)
        val url0 = urlBuilder0.build().toString()
        val request0 = Request.Builder()
            .url(url0)
            .build()
        Thread(Runnable {
            var length = ""
            this@LocalNowPlayingActivity.run {
                try {
                    val call0 = client.newCall(request0)
                    val response0 = call0.execute()
                    if (response0.isSuccessful) {
                        length = response0.body()!!.string()
                    }
                } catch (e: IOException) {
                }
            }
            this@LocalNowPlayingActivity.runOnUiThread {
                if (length != "") {
                    musicLengthInSecs = length.toFloat().toInt()
                    val hours = musicLengthInSecs / 3600
                    val minutes = (musicLengthInSecs % 3600) / 60
                    val seconds = musicLengthInSecs % 60
                    musicLength.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    playSeekBar.max = musicLengthInSecs
                    if (localPlaylist[curPos].status == 1) {
                        seekPositionInSecs = 0
                        playSeekBar.progress = seekPositionInSecs
                        handler.removeCallbacksAndMessages(null)
                        handler.postDelayed(object : Runnable {
                            override fun run() {
                                seekPositionInSecs++
                                if (seekPositionInSecs > musicLengthInSecs) {
                                    handler.removeCallbacksAndMessages(null)
                                    curPos++
                                    if (curPos >= localPlaylist.count())
                                        curPos = 0
                                    nowPlayingItemDisplay(curPos)
                                    return
                                }
                                playSeekBar.progress = seekPositionInSecs
                                val hh = seekPositionInSecs / 3600
                                val mm = (seekPositionInSecs % 3600) / 60
                                val ss = seekPositionInSecs % 60
                                seekPos.text = String.format("%02d:%02d:%02d", hh, mm, ss)
                                handler.postDelayed(this, 1000)
                            }
                        }, 1000)
                    }
                }
            }
        }).start()
        if (localPlaylist[curPos].status == 0) {
            playButton.visibility = View.VISIBLE
            stopButton.visibility = View.INVISIBLE
        }
        else if (localPlaylist[curPos].status == 1) {
            playButton.visibility = View.INVISIBLE
            stopButton.visibility = View.VISIBLE
        }
        var position = curPos
        stopButton.setOnClickListener(null)
        stopButton.setOnClickListener {
            val urlBuilder = HttpUrl.parse("http://$ipAddress:3000/local/pause")!!.newBuilder()
            urlBuilder.addQueryParameter("categoryDirectory", localPlaylist[position].categoryDirectory)
            urlBuilder.addQueryParameter("fileName", localPlaylist[position].fileName)
            val url = urlBuilder.build().toString()
            val request = Request.Builder()
                .url(url)
                .build()
            Thread(Runnable {
                var responseStatus = false
                this@LocalNowPlayingActivity.run {
                    try {
                        val call = client.newCall(request)
                        val response = call.execute()
                        if (response.isSuccessful) {
                            responseStatus = true
                        }
                    } catch (e: IOException) {
                    }
                }
                this@LocalNowPlayingActivity.runOnUiThread {
                    if (responseStatus) {
                        playButton.visibility = View.VISIBLE
                        stopButton.visibility = View.INVISIBLE
                        handler.removeCallbacksAndMessages(null)
                    }
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
                        localPlaylist[position].status = 0
                    }
                } catch (e: IOException) {
                }
            }).start()
        }
        playButton.setOnClickListener(null)
        playButton.setOnClickListener {
            val urlBuilder2 = HttpUrl.parse("http://$ipAddress:3000/local/length")!!.newBuilder()
            urlBuilder2.addQueryParameter("categoryDirectory", localPlaylist[position].categoryDirectory)
            urlBuilder2.addQueryParameter("fileName", localPlaylist[position].fileName)
            val url2 = urlBuilder2.build().toString()
            val request2 = Request.Builder()
                .url(url2)
                .build()
            Thread(Runnable {
                var length = ""
                this@LocalNowPlayingActivity.run {
                    try {
                        val call2 = client.newCall(request2)
                        val response2 = call2.execute()
                        if (response2.isSuccessful) {
                            length = response2.body()!!.string()
                        }
                    } catch (e: IOException) {
                    }
                }
                this@LocalNowPlayingActivity.runOnUiThread {
                    if (length != "") {
                        musicLengthInSecs = length.toFloat().toInt()
                        val hours = musicLengthInSecs / 3600
                        val minutes = (musicLengthInSecs % 3600) / 60
                        val seconds = musicLengthInSecs % 60
                        musicLength.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                        playSeekBar.max = musicLengthInSecs
                        seekPositionInSecs = 0
                        playSeekBar.progress = seekPositionInSecs
                        handler.removeCallbacksAndMessages(null)
                        handler.postDelayed(object : Runnable {
                            override fun run() {
                                seekPositionInSecs++
                                if (seekPositionInSecs > musicLengthInSecs) {
                                    handler.removeCallbacksAndMessages(null)
                                    curPos++
                                    if (curPos >= localPlaylist.count())
                                        curPos = 0
                                    nowPlayingItemDisplay(curPos)
                                    return
                                }
                                playSeekBar.progress = seekPositionInSecs
                                val hh = seekPositionInSecs / 3600
                                val mm = (seekPositionInSecs % 3600) / 60
                                val ss = seekPositionInSecs % 60
                                seekPos.text = String.format("%02d:%02d:%02d", hh, mm, ss)
                                handler.postDelayed(this, 1000)
                            }
                        }, 1000)
                    }
                }
            }).start()
            val urlBuilder = HttpUrl.parse("http://$ipAddress:3000/local/play")!!.newBuilder()
            urlBuilder.addQueryParameter("categoryDirectory", localPlaylist[position].categoryDirectory)
            urlBuilder.addQueryParameter("fileName", localPlaylist[position].fileName)
            val url = urlBuilder.build().toString()
            val request = Request.Builder()
                .url(url)
                .build()
            Thread(Runnable {
                var responseStatus = false
                this@LocalNowPlayingActivity.run {
                    try {
                        val call = client.newCall(request)
                        val response = call.execute()
                        if (response.isSuccessful) {
                            responseStatus = true
                        }
                    } catch (e: IOException) {
                    }
                }
                this@LocalNowPlayingActivity.runOnUiThread {
                    if (responseStatus) {
                        playButton.visibility = View.INVISIBLE
                        stopButton.visibility = View.VISIBLE
                    }
                }
            }).start()
        }
        previousButton.setOnClickListener {
            position--
            if (position < 0)
                position = localPlaylist.count() - 1
            nowPlayingItemDisplay(position)
        }
        nextButton.setOnClickListener {
            position++
            if (position >= localPlaylist.count())
                position = 0
            nowPlayingItemDisplay(position)
        }
        playSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                seekPositionInSecs = progress
                val hours = progress / 3600
                val minutes = (progress % 3600) / 60
                val seconds = progress % 60
                seekPos.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }
        })
        playlistButton.setOnClickListener {
            launchLocalPlaylistActivity()
        }
    }

    private fun nowPlayingItemDisplay(pos: Int) {
        fileName.text = localPlaylist[pos].fileName
        val client = OkHttpClient()
        val urlBuilder0 = HttpUrl.parse("http://$ipAddress:3000/local/length")!!.newBuilder()
        urlBuilder0.addQueryParameter("categoryDirectory", localPlaylist[pos].categoryDirectory)
        urlBuilder0.addQueryParameter("fileName", localPlaylist[pos].fileName)
        val url0 = urlBuilder0.build().toString()
        val request0 = Request.Builder()
            .url(url0)
            .build()
        Thread(Runnable {
            var length = ""
            this@LocalNowPlayingActivity.run {
                try {
                    val call0 = client.newCall(request0)
                    val response0 = call0.execute()
                    if (response0.isSuccessful) {
                        length = response0.body()!!.string()
                    }
                } catch (e: IOException) {
                }
            }
            this@LocalNowPlayingActivity.runOnUiThread {
                if (length != "") {
                    musicLengthInSecs = length.toFloat().toInt()
                    val hours = musicLengthInSecs / 3600
                    val minutes = (musicLengthInSecs % 3600) / 60
                    val seconds = musicLengthInSecs % 60
                    musicLength.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    playSeekBar.max = musicLengthInSecs
                    seekPositionInSecs = 0
                    playSeekBar.progress = seekPositionInSecs
                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed(object : Runnable {
                        override fun run() {
                            seekPositionInSecs ++
                            if (seekPositionInSecs > musicLengthInSecs) {
                                handler.removeCallbacksAndMessages(null)
                                curPos++
                                if (curPos >= localPlaylist.count())
                                    curPos = 0
                                nowPlayingItemDisplay(curPos)
                                return
                            }
                            playSeekBar.progress = seekPositionInSecs
                            val hh = seekPositionInSecs / 3600
                            val mm = (seekPositionInSecs % 3600) / 60
                            val ss = seekPositionInSecs % 60
                            seekPos.text = String.format("%02d:%02d:%02d", hh, mm, ss)
                            handler.postDelayed(this, 1000)
                        }
                    }, 1000)
                }
            }
        }).start()
        val urlBuilder = HttpUrl.parse("http://$ipAddress:3000/local/play")!!.newBuilder()
        urlBuilder.addQueryParameter("categoryDirectory", localPlaylist[pos].categoryDirectory)
        urlBuilder.addQueryParameter("fileName", localPlaylist[pos].fileName)
        val url = urlBuilder.build().toString()
        val request = Request.Builder()
            .url(url)
            .build()
        Thread(Runnable {
            var responseStatus = false
            this@LocalNowPlayingActivity.run {
                try {
                    val call = client.newCall(request)
                    val response = call.execute()
                    if (response.isSuccessful) {
                        responseStatus = true
                    }
                } catch (e: IOException) {
                }
            }
            this@LocalNowPlayingActivity.runOnUiThread {
                if (responseStatus) {
                    playButton.visibility = View.INVISIBLE
                    stopButton.visibility = View.VISIBLE
                }
            }
        }).start()
    }

    private fun launchLocalPlaylistActivity() {
        val intent = Intent(this, LocalPlaylistActivity::class.java)
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