package com.rpi.streaming.streamingplayer

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.gson.Gson
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbFile
import kotlinx.android.synthetic.main.activity_local_music_listing.*
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.*

class LocalMusicListingActivity : AppCompatActivity() {

    private val localMusicItemsList = ArrayList<LocalMusic>()
    private val playlistItems = ArrayList<LocalMusic>()
    private var localMusicAdapter: LocalMusicAdapter? = null
    private var serverInfo = LocalMusicServer()
    private var contains = false
    private var searchQuery = ""
    private var category = ""
    private var ipAddress: String? = null
    private var deviceName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_music_listing)
        ipAddress = intent.getStringExtra("ipAddress")
        deviceName = intent.getStringExtra("deviceName")
        warningText.text = "Scanning the directory..."
        warningText.visibility = View.VISIBLE
        if (intent.extras.containsKey("query"))
            searchQuery = intent.getStringExtra("query")
        if (intent.extras.containsKey("category"))
            category = intent.getStringExtra("category")
        serverInfo = intent.getSerializableExtra("serverInfo") as LocalMusicServer
        localMusicAdapter = LocalMusicAdapter(localMusicItemsList, ipAddress, deviceName, window)
        localMusicRecyclerView!!.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        localMusicRecyclerView!!.layoutManager = mLayoutManager
        localMusicRecyclerView!!.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        localMusicRecyclerView!!.itemAnimator = DefaultItemAnimator()
        localMusicRecyclerView!!.adapter = localMusicAdapter
        localMusicRecyclerView!!.addOnItemTouchListener(RecyclerTouchListener(applicationContext, localMusicRecyclerView!!, object : RecyclerTouchListener.ClickListener {
            override fun onLongClick(view: View?, position: Int) : Boolean {
                return true
            }
            override fun onClick(view: View, position: Int) {
            }
        }))
        showLocalMusicItemsList()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val addSourceItem: MenuItem = menu.findItem(R.id.action_add_source)
        addSourceItem.isVisible = false
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
        return when {
            item.itemId == R.id.action_main -> {
                val intent = Intent(this, ConnectedDevicesListingActivity::class.java)
                startActivity(intent)
                true
            }
            item.itemId == R.id.action_playlist -> {
                val intent = Intent(this, LocalPlaylistActivity::class.java)
                intent.putExtra("ipAddress", ipAddress)
                intent.putExtra("deviceName", deviceName)
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

    private fun showLocalMusicItemsList() {
        val client = OkHttpClient()
        val urlBuilder = HttpUrl.parse("http://$ipAddress:3000/local/playlist")!!.newBuilder()
        val url = urlBuilder.build().toString()
        val request = Request.Builder()
            .url(url)
            .build()
        Thread(Runnable {
            var result = listOf("")
            var files = listOf<SmbFile>()
            if (category != "") {
                val homeUrl =
                    "smb://" + serverInfo.hostName + '/' + serverInfo.sharedRootDirectory + category + '/'
                val streamUrl =
                    "smb://" + serverInfo.username + ':' + serverInfo.password + '@' + serverInfo.hostName + '/' + serverInfo.sharedRootDirectory + category + "/"
                this@LocalMusicListingActivity.run {
                    try {
                        val authentication = NtlmPasswordAuthentication(null, serverInfo.username, serverInfo.password)
                        val home = SmbFile(homeUrl, authentication)
                        files = home.listFiles().toList()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
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
                    } catch (e: IOException) {
                    }
                }
                this@LocalMusicListingActivity.runOnUiThread {
                    for (file in files) {
                        result.forEach {
                            val gson = Gson()
                            val item = gson.fromJson(it, LocalMusic::class.java)
                            if (item != null)
                                playlistItems.add(item)
                        }
                        contains = false
                        if (playlistItems.count() > 0) {
                            playlistItems.forEach {
                                if (it.categoryDirectory + it.fileName == streamUrl + file.name) contains = true
                            }
                        }
                        if (file.isFile) {
                            val localMusicItem = LocalMusic(contains, streamUrl, file.name)
                            localMusicItemsList.add(localMusicItem)
                            localMusicAdapter!!.notifyDataSetChanged()
                        }
                    }
                    if (localMusicItemsList.count() == 0) {
                        warningText.text = "The directory is empty."
                        warningText.visibility = View.VISIBLE
                    } else {
                        warningText.visibility = View.INVISIBLE
                    }
                }
            }
            if (searchQuery != "") {
                val categorylists = listOf("Albums", "Artists", "Playlist", "Songs")
                for (it in categorylists) {
                    val homeUrl =
                        "smb://" + serverInfo.hostName + '/' + serverInfo.sharedRootDirectory + it + '/'
                    val streamUrl =
                        "smb://" + serverInfo.username + ':' + serverInfo.password + '@' + serverInfo.hostName + '/' + serverInfo.sharedRootDirectory + it + "/"
                    this@LocalMusicListingActivity.run {
                        try {
                            val authentication = NtlmPasswordAuthentication(null, serverInfo.username, serverInfo.password)
                            val home = SmbFile(homeUrl, authentication)
                            files = home.listFiles().toList()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
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
                        } catch (e: IOException) {
                        }
                    }
                    this@LocalMusicListingActivity.runOnUiThread {
                        for (file in files) {
                            result.forEach {
                                val gson = Gson()
                                val item = gson.fromJson(it, LocalMusic::class.java)
                                if (item != null)
                                    playlistItems.add(item)
                            }
                            contains = false
                            if (playlistItems.count() > 0) {
                                playlistItems.forEach {
                                    if (it.categoryDirectory + it.fileName == streamUrl + file.name) contains = true
                                }
                            }
                            if (file.isFile && file.name.contains(searchQuery)) {
                                val localMusicItem = LocalMusic(contains, streamUrl, file.name)
                                localMusicItemsList.add(localMusicItem)
                                localMusicAdapter!!.notifyDataSetChanged()
                            }
                        }
                        if (localMusicItemsList.count() == 0) {
                            warningText.text = "No results."
                            warningText.visibility = View.VISIBLE
                        } else {
                            warningText.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        }).start()
    }
}