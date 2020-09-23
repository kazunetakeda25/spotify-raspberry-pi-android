package com.rpi.streaming.streamingplayer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_local_playlist.*
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.*

class LocalPlaylistActivity : AppCompatActivity() {

    private val localPlaylistItems = ArrayList<LocalPlaylist>()
    private var playlistAdapter: LocalPlaylistAdapter? = null
    private var isAdded: Boolean = false
    private var ipAddress: String? = null
    private var deviceName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_playlist)
        ipAddress = intent.getStringExtra("ipAddress")
        deviceName = intent.getStringExtra("deviceName")
        val mSettingsContentObserver = AudioSettingsContentObserver(this, Handler(), ipAddress)
        applicationContext.contentResolver.registerContentObserver(
            android.provider.Settings.System.CONTENT_URI,
            true,
            mSettingsContentObserver
        )
        noItemsText.visibility = View.INVISIBLE
        playlistAdapter = LocalPlaylistAdapter(localPlaylistItems, ipAddress, deviceName)
        localPlaylistRecyclerView!!.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        localPlaylistRecyclerView!!.layoutManager = mLayoutManager
        localPlaylistRecyclerView!!.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        localPlaylistRecyclerView!!.itemAnimator = DefaultItemAnimator()
        localPlaylistRecyclerView!!.adapter = playlistAdapter
        localPlaylistRecyclerView!!.addOnItemTouchListener(RecyclerTouchListener(applicationContext, localPlaylistRecyclerView!!, object : RecyclerTouchListener.ClickListener {
            override fun onLongClick(view: View?, position: Int) : Boolean {
                return true
            }
            override fun onClick(view: View, position: Int) {

            }
        }))
        showLocalPlaylist()
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
                val intent = Intent(this, LocalMusicServerListingActivity::class.java)
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

    public override fun onResume() {
        super.onResume()
        if (localPlaylistItems.count() == 0)
            noItemsText.visibility = View.VISIBLE
        else
            noItemsText.visibility = View.INVISIBLE
        if (isAdded) {
            val client = OkHttpClient()
            val urlBuilder1 = HttpUrl.parse("http://$ipAddress:3000/currentlyPlaying")!!.newBuilder()
            val url1 = urlBuilder1.build().toString()
            val request1 = Request.Builder()
                .url(url1)
                .build()
            Thread(Runnable {
                var result1 = String()
                this@LocalPlaylistActivity.run {
                    try {
                        val call1 = client.newCall(request1)
                        val response1 = call1.execute()
                        when {
                            response1.isSuccessful -> {
                                val responseData = response1.body()!!.string()
                                val stringBuilder = StringBuilder(responseData)
                                result1 = stringBuilder.toString()
                            }
                            else -> {
                            }
                        }
                    } catch (e: IOException) {
                    }
                }

                this@LocalPlaylistActivity.runOnUiThread {
                    val gson1 = Gson()
                    val currentlyPlayingItem = gson1.fromJson(result1, LocalPlaylist::class.java)
                    localPlaylistItems.forEach {
                        if (currentlyPlayingItem != null && it.categoryDirectory + it.fileName == currentlyPlayingItem.categoryDirectory + it.fileName)
                            it.status = 0
                        else
                            it.status = 1
                        playlistAdapter!!.notifyDataSetChanged()
                    }
                    if (localPlaylistItems.count() == 0)
                        noItemsText.visibility = View.VISIBLE
                    else
                        noItemsText.visibility = View.INVISIBLE
                }
            }).start()
        }
    }

    private fun showLocalPlaylist() {
        val client = OkHttpClient()
        val urlBuilder = HttpUrl.parse("http://$ipAddress:3000/local/playlist")!!.newBuilder()
        val url = urlBuilder.build().toString()
        val request = Request.Builder()
            .url(url)
            .build()
        val urlBuilder1 = HttpUrl.parse("http://$ipAddress:3000/currentlyPlaying")!!.newBuilder()
        val url1 = urlBuilder1.build().toString()
        val request1 = Request.Builder()
            .url(url1)
            .build()
        Thread(Runnable {
            var result = listOf("")
            var result1 = String()
            this@LocalPlaylistActivity.run {
                try {
                    val call = client.newCall(request)
                    val response = call.execute()
                    when {
                        response.isSuccessful -> {
                            val responseData = response.body()!!.string()
                            val stringBuilder = StringBuilder(responseData)
                            result = stringBuilder.split("```").map { it.trim() }
                        }
                        else -> {
                        }
                    }
                    val call1 = client.newCall(request1)
                    val response1 = call1.execute()
                    when {
                        response1.isSuccessful -> {
                            val responseData = response1.body()!!.string()
                            val stringBuilder = StringBuilder(responseData)
                            result1 = stringBuilder.toString()
                        }
                        else -> {
                        }
                    }
                } catch (e: IOException) {
                }
            }
            this@LocalPlaylistActivity.runOnUiThread {
                val gson1 = Gson()
                val currentlyPlayingItem = gson1.fromJson(result1, LocalPlaylist::class.java)
                result.forEach {
                    val gson = Gson()
                    val localPlaylistItem = gson.fromJson(it, LocalPlaylist::class.java)
                    if (localPlaylistItem != null) {
                        if (currentlyPlayingItem != null && localPlaylistItem.categoryDirectory + localPlaylistItem.fileName == currentlyPlayingItem.categoryDirectory + currentlyPlayingItem.fileName)
                            localPlaylistItem.status = 1
                        else
                            localPlaylistItem.status = 0
                        localPlaylistItems.add(localPlaylistItem)
                        playlistAdapter!!.notifyDataSetChanged()
                    }
                }
                isAdded = true
                if (localPlaylistItems.count() == 0)
                    noItemsText.visibility = View.VISIBLE
                else
                    noItemsText.visibility = View.INVISIBLE
            }
        }).start()
    }
}