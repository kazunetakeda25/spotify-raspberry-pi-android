package com.rpi.streaming.streamingplayer

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_local_music_category_listing.*
import java.io.Serializable

class LocalMusicCategoryListingActivity : AppCompatActivity() {

    private var serverInfo = LocalMusicServer()
    private var ipAddress: String? = null
    private var deviceNameFromIntent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_music_category_listing)
        ipAddress = intent.getStringExtra("ipAddress")
        deviceNameFromIntent = intent.getStringExtra("deviceName")
        serverInfo = intent.getSerializableExtra("serverInfo") as LocalMusicServer
        searchButton.setOnClickListener {
            if (searchInputField.text.toString() == "") {
                searchInputField.error = "Please input search query."
                return@setOnClickListener
            }
            launchLocalMusicListingActivity("Search")
        }
        artistsButton.setOnClickListener {
            launchLocalMusicListingActivity("Artists")
        }
        albumsButton.setOnClickListener {
            launchLocalMusicListingActivity("Albums")
        }
        songsButton.setOnClickListener {
            launchLocalMusicListingActivity("Songs")
        }
        playlistButton.setOnClickListener {
            launchLocalMusicListingActivity("Playlist")
        }
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
                intent.putExtra("deviceName", deviceNameFromIntent)
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

    private fun launchLocalMusicListingActivity(category: String) {
        val intent: Intent
        if (category == "Search") {
            intent = Intent(this, LocalMusicListingActivity::class.java)
            intent.putExtra("query", searchInputField.text.toString())
        } else {
            intent = Intent(this, LocalMusicListingActivity::class.java)
            intent.putExtra("category", category)
        }
        intent.putExtra("ipAddress", ipAddress)
        intent.putExtra("deviceName", deviceNameFromIntent)
        intent.putExtra("serverInfo", serverInfo as Serializable)
        startActivity(intent)
    }
}