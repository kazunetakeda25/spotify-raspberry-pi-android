package com.rpi.streaming.streamingplayer

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_tunein_main_category_listing.*

class TuneInMainCategoryListingActivity : AppCompatActivity() {

    private var ipAddress: String? = null
    private var deviceNameFromIntent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tunein_main_category_listing)

        btnSearch.setOnClickListener {
            if (searchInput.text.toString() == "") {
                searchInput.error = "Please input search query"
                return@setOnClickListener
            }
            loadTuneInSubCategoryActivity("Search")
        }
        btnLocalRadio.setOnClickListener {
            loadTuneInSubCategoryActivity("LocalRadio")
        }
        btnMusic.setOnClickListener {
            loadTuneInSubCategoryActivity("Music")
        }
        btnNews.setOnClickListener {
            loadTuneInSubCategoryActivity("News")
        }
        btnSport.setOnClickListener {
            loadTuneInSubCategoryActivity("Sport")
        }
        btnPodcasts.setOnClickListener {
            loadTuneInSubCategoryActivity("Podcasts")
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
                val intent = Intent(this, TuneInPlaylistActivity::class.java)
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

    private fun loadTuneInSubCategoryActivity(category: String) {
        val intent: Intent
        if (category == "Search") {
            intent = Intent(this, TuneInMusicListingActivity::class.java)
            intent.putExtra("href", "http://opml.radiotime.com/Search.ashx?query=${searchInput.text}")
        } else {
            intent = Intent(this, TuneInSubCategoryListingActivity::class.java)
            intent.putExtra("category", category)
        }
        intent.putExtra("ipAddress", ipAddress)
        intent.putExtra("deviceName", deviceNameFromIntent)
        startActivity(intent)
    }
}