package com.rpi.streaming.streamingplayer

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_music_source_listing.*

class MusicSourceListingActivity : AppCompatActivity() {

    private var ipAddress: String? = null
    private var deviceNameFromIntent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_source_listing)
        ipAddress = intent.getStringExtra("ipAddress")
        deviceNameFromIntent = intent.getStringExtra("deviceName")
        spotifySourceButton.setOnClickListener {
            loadSpotifySourceActivity()
        }
        tuneinSourceButton.setOnClickListener {
            loadTuneInSourceActivity()
        }
        localSourceButton.setOnClickListener {
            loadLocalSourceActivity()
        }
    }

    private fun loadSpotifySourceActivity() {
        val intent = Intent(this, TuneInMainCategoryListingActivity::class.java)
        intent.putExtra("ipAddress", ipAddress)
        intent.putExtra("deviceName", deviceNameFromIntent)
        startActivity(intent)
    }

    private fun loadTuneInSourceActivity() {
        val intent = Intent(this, TuneInMainCategoryListingActivity::class.java)
        intent.putExtra("ipAddress", ipAddress)
        intent.putExtra("deviceName", deviceNameFromIntent)
        startActivity(intent)
    }

    private fun loadLocalSourceActivity() {
        val intent = Intent(this, LocalMusicServerListingActivity::class.java)
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
                val intent = Intent(this, PlaylistActivity::class.java)
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
}