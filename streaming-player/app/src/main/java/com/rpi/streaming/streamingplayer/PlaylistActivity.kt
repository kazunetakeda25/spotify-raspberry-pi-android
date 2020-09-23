package com.rpi.streaming.streamingplayer

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_playlist.*
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class PlaylistActivity : AppCompatActivity() {

    private var ipAddress: String? = null
    private var deviceName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)
        ipAddress = intent.getStringExtra("ipAddress")
        deviceName = intent.getStringExtra("deviceName")
        Log.d(this.localClassName, ipAddress)
        spotifySourceButton.setOnClickListener {
            loadPlaylistActivity(SpotifyPlaylistActivity::class.java)
        }
        tuneinPlaylistButton.setOnClickListener {
            loadPlaylistActivity(TuneInPlaylistActivity::class.java)
        }
        localPlaylistButton.setOnClickListener {
            loadPlaylistActivity(LocalPlaylistActivity::class.java)
        }
    }

    override fun onResume() {
        Log.d("+onResume", "sdfsdfsdfd")
        Log.d("+onResume", ipAddress)
        Thread(Runnable {
            val client = OkHttpClient()
            val urlBuilder = HttpUrl.parse("http://$ipAddress:3000/tunein/count")!!.newBuilder()
            val url = urlBuilder.build().toString()
            val request = Request.Builder()
                .url(url)
                .build()
            var tuneinPlaylistCount = "TuneIn Playlist (0 songs)"
            this@PlaylistActivity.run {
                try {
                    val call = client.newCall(request)
                    val response = call.execute()
                    if (response.isSuccessful) {
                        tuneinPlaylistCount = "TuneIn Playlist (${response.body()!!.string()} songs)"
                    }
                } catch (e: IOException) {
                }
            }
            this@PlaylistActivity.runOnUiThread {
                tuneinPlaylistButton.text = tuneinPlaylistCount
            }
        }).start()
        Thread(Runnable {
            val client = OkHttpClient()
            val urlBuilder = HttpUrl.parse("http://$ipAddress:3000/local/count")!!.newBuilder()
            val url = urlBuilder.build().toString()
            val request = Request.Builder()
                .url(url)
                .build()
            var localPlaylistCount = "Local Network Storage Playlist (0 songs)"
            this@PlaylistActivity.run {
                try {
                    val call = client.newCall(request)
                    val response = call.execute()
                    if (response.isSuccessful) {
                        localPlaylistCount = "Local Network Storage Playlist (${response.body()!!.string()} songs)"
                    }
                } catch (e: IOException) {
                }
            }
            this@PlaylistActivity.runOnUiThread {
                localPlaylistButton.text = localPlaylistCount
            }
        }).start()
        super.onResume()
    }

    private fun loadPlaylistActivity(activity: Class<*>) {
        val intent = Intent(this, activity)
        intent.putExtra("ipAddress", ipAddress)
        intent.putExtra("deviceName", deviceName)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val playlistItem: MenuItem = menu.findItem(R.id.action_playlist)
        playlistItem.isVisible = false
        val addDeviceItem: MenuItem = menu.findItem(R.id.action_add_device)
        addDeviceItem.isVisible = false
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        return when (id) {
            R.id.action_main -> {
                val intent = Intent(this, ConnectedDevicesListingActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_add_source -> {
                val intent = Intent(this, MusicSourceListingActivity::class.java)
                intent.putExtra("ipAddress", ipAddress)
                intent.putExtra("deviceName", deviceName)
                startActivity(intent)
                true
            }
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}