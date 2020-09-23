package com.rpi.streaming.streamingplayer

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_tunein_music_listing.*
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import org.jsoup.select.Elements
import java.io.IOException
import java.util.ArrayList

class TuneInMusicListingActivity : AppCompatActivity() {

    private val tuneInSubCategoryItemList = ArrayList<TuneInMusic>()
    private val playlistItems = ArrayList<TuneInPlaylist>()
    private var recyclerView: RecyclerView? = null
    private var subCategoryItemAdapter: TuneInMusicAdapter? = null
    private var contains = false
    private var ipAddress: String? = null
    private var deviceName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tunein_music_listing)
        ipAddress = intent.getStringExtra("ipAddress")
        deviceName = intent.getStringExtra("deviceName")
        infoText.text = "Preparing the data..."
        infoText.visibility = View.VISIBLE
        recyclerView = recycler_view as RecyclerView
        subCategoryItemAdapter = TuneInMusicAdapter(tuneInSubCategoryItemList, ipAddress, deviceName)
        recyclerView!!.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        recyclerView!!.layoutManager = mLayoutManager
        recyclerView!!.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        recyclerView!!.adapter = subCategoryItemAdapter
        recyclerView!!.addOnItemTouchListener(RecyclerTouchListener(applicationContext, recyclerView!!, object : RecyclerTouchListener.ClickListener {
            override fun onLongClick(view: View?, position: Int) : Boolean {
                return true
            }
            override fun onClick(view: View, position: Int) {
            }
        }))
        showTuneInSubCategoryList()
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

    private fun showTuneInSubCategoryList() {
        val client = OkHttpClient()
        val urlBuilder = HttpUrl.parse("http://$ipAddress:3000/tunein/playlist")!!.newBuilder()
        val url = urlBuilder.build().toString()
        val request = Request.Builder()
            .url(url)
            .build()
        Thread(Runnable {
            var result = listOf("")
            var guideItemLinks = Elements()
            if (intent.getStringExtra("href") == null)
                return@Runnable

            this@TuneInMusicListingActivity.run {
                try {
                    var doc : Document = Jsoup.connect(intent.getStringExtra("href")).get()
                    doc = Jsoup.parse(doc.toString(), "", Parser.xmlParser())
                    guideItemLinks = doc.select("outline")
                } catch (e: IOException) {
                }
                if (ipAddress != null) {
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
            }
            this@TuneInMusicListingActivity.runOnUiThread {
                for (guideItemLink in guideItemLinks) {
                    if (guideItemLink.attr("type") == "audio") {
                        if (ipAddress != null) {
                            result.forEach {
                                val gson = Gson()
                                val tuneInPlaylistItem = gson.fromJson(it, TuneInPlaylist::class.java)
                                if (tuneInPlaylistItem != null)
                                    playlistItems.add(tuneInPlaylistItem)
                            }
                        }
                        contains = false
                        if (playlistItems.count() > 0) {
                            playlistItems.forEach {
                                if (it.href == guideItemLink.attr("URL")) contains = true
                            }
                        }
                        val tunInSubCategoryItem = TuneInMusic(
                            contains,
                            guideItemLink.attr("image"),
                            guideItemLink.attr("text"),
                            guideItemLink.attr("playing_image"),
                            guideItemLink.attr("subtext"),
                            guideItemLink.attr("URL")
                        )
                        tuneInSubCategoryItemList.add(tunInSubCategoryItem)
                        subCategoryItemAdapter!!.notifyDataSetChanged()
                    }
                }
                if (tuneInSubCategoryItemList.count() == 0) {
                    infoText.text = "No results."
                    infoText.visibility = View.VISIBLE
                } else {
                    infoText.visibility = View.INVISIBLE
                }
            }
        }).start()
    }
}