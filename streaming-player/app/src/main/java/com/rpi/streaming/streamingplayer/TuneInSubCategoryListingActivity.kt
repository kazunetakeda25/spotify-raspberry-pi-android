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
import kotlinx.android.synthetic.main.activity_tunein_sub_category_listing.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import org.jsoup.select.Elements
import java.io.IOException
import java.util.ArrayList

class TuneInSubCategoryListingActivity : AppCompatActivity() {

    private val tuneInSubCategoryList = ArrayList<TuneInSubCategory>()
    private var subCategoryAdapter: TuneInSubCategoryAdapter? = null
    private var ipAddress: String? = null
    private var deviceNameFromIntent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tunein_sub_category_listing)
        ipAddress = intent.getStringExtra("ipAddress")
        deviceNameFromIntent = intent.getStringExtra("deviceName")
        infoText.text = "Prepareing the data..."
        infoText.visibility = View.VISIBLE
        subCategoryAdapter = TuneInSubCategoryAdapter(tuneInSubCategoryList)
        recycler_view!!.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        recycler_view!!.layoutManager = mLayoutManager
        recycler_view!!.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        recycler_view!!.itemAnimator = DefaultItemAnimator()
        recycler_view!!.adapter = subCategoryAdapter
        recycler_view!!.addOnItemTouchListener(RecyclerTouchListener(applicationContext, recycler_view!!, object : RecyclerTouchListener.ClickListener {
            override fun onLongClick(view: View?, position: Int) : Boolean {
                return true
            }
            override fun onClick(view: View, position: Int) {
                launchTuneInCategoryItemActivity(position)
            }
        }))
        showTuneInSubCategoryList()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val addSourceItem: MenuItem = menu.findItem(R.id.action_add_source)
        addSourceItem.isVisible = false
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

    private fun launchTuneInCategoryItemActivity(index: Int) {
        val intent = Intent(this, TuneInMusicListingActivity::class.java)
        intent.putExtra("href", tuneInSubCategoryList[index].href)
        startActivity(intent)
    }

    private fun showTuneInSubCategoryList() {
        Thread(Runnable {
            var guideItemLinks = Elements()
            this@TuneInSubCategoryListingActivity.run {
                try {
                    var doc = Document("")
                    when {
                        intent.getStringExtra("category") == "LocalRadio" -> doc = Jsoup.connect("http://opml.radiotime.com/Browse.ashx?c=local").get()
                        intent.getStringExtra("category") == "Music" -> doc = Jsoup.connect("http://opml.radiotime.com/Browse.ashx?c=music").get()
                        intent.getStringExtra("category") == "News" -> doc = Jsoup.connect("http://opml.radiotime.com/Browse.ashx?c=talk").get()
                        intent.getStringExtra("category") == "Sport" -> doc = Jsoup.connect("http://opml.radiotime.com/Browse.ashx?c=sports").get()
                        intent.getStringExtra("category") == "Podcasts" -> doc = Jsoup.connect("http://opml.radiotime.com/Browse.ashx?c=podcast").get()
                    }
                    doc = Jsoup.parse(doc.toString(), "", Parser.xmlParser())
                    guideItemLinks = doc.select("outline")
                } catch (e: IOException) {

                }
            }
            this@TuneInSubCategoryListingActivity.runOnUiThread {
                for (guideItemLink in guideItemLinks) {
                    if (guideItemLink.attr("type") == "link") {
                        val tunInSubCategoryItem =
                            TuneInSubCategory(guideItemLink.attr("text"), guideItemLink.attr("URL"))
                        tuneInSubCategoryList.add(tunInSubCategoryItem)
                        subCategoryAdapter!!.notifyDataSetChanged()
                    }
                }
                if (tuneInSubCategoryList.count() == 0) {
                    infoText.text = "No results."
                    infoText.visibility = View.VISIBLE
                } else {
                    infoText.visibility = View.INVISIBLE
                }
            }
        }).start()
    }
}