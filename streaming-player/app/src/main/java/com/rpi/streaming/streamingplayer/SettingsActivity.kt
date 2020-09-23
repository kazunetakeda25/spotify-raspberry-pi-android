package com.rpi.streaming.streamingplayer

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        playerSetupSettingButton.setOnClickListener {
            loadSettingsComponentActivity(SettingsPlayerSetupActivity::class.java)
        }
        spotifySettingButton.setOnClickListener {
            loadSettingsComponentActivity(SettingsSpotifyActivity::class.java)
        }
        tuneinSettingButton.setOnClickListener {
            loadSettingsComponentActivity(SettingsTuneInActivity::class.java)
        }
        localSettingButton.setOnClickListener {
            loadSettingsComponentActivity(SettingsLocalActivity::class.java)
        }
        viewPlayerSetup.setOnClickListener {
            loadSettingsComponentActivity(SettingsPlayerSetupActivity::class.java)
        }
        viewSpotify.setOnClickListener {
            loadSettingsComponentActivity(SettingsSpotifyActivity::class.java)
        }
        viewTuneIn.setOnClickListener {
            loadSettingsComponentActivity(SettingsTuneInActivity::class.java)
        }
        viewLNS.setOnClickListener {
            loadSettingsComponentActivity(SettingsLocalActivity::class.java)
        }
    }

    private fun loadSettingsComponentActivity(activity: Class<*>) {
        val intent = Intent(this, activity)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val addSourceItem: MenuItem = menu.findItem(R.id.action_add_source)
        addSourceItem.isVisible = false
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
            R.id.action_settings -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}