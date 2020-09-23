package com.rpi.streaming.streamingplayer

import android.content.Context
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
import kotlinx.android.synthetic.main.activity_local_music_server_listing.*
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.*

class LocalMusicServerListingActivity : AppCompatActivity() {

    private val localMusicServerItemsList = ArrayList<LocalMusicServer>()
    private var musicServerAdapter: LocalMusicServerAdapter? = null
    private var ipAddress: String? = null
    private var deviceNameFromIntent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_music_server_listing)
        ipAddress = intent.getStringExtra("ipAddress")
        deviceNameFromIntent = intent.getStringExtra("deviceName")
        noDevicesText.visibility = View.INVISIBLE
        musicServerAdapter = LocalMusicServerAdapter(localMusicServerItemsList)
        localMusicServerRecycleView!!.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        localMusicServerRecycleView!!.layoutManager = mLayoutManager
        localMusicServerRecycleView!!.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        localMusicServerRecycleView!!.itemAnimator = DefaultItemAnimator()
        localMusicServerRecycleView!!.adapter = musicServerAdapter
        localMusicServerRecycleView!!.addOnItemTouchListener(RecyclerTouchListener(applicationContext, localMusicServerRecycleView!!, object : RecyclerTouchListener.ClickListener {
            override fun onLongClick(view: View?, position: Int) : Boolean {
                return true
            }
            override fun onClick(view: View, position: Int) {
                launchLocalMusicCategoryListingActivity(position)
            }
        }))
        showLocalMusicServerList()
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
            item.itemId == R.id.action_add_device -> {
                val intent = Intent(this, ConnectLocalSourceActivity::class.java)
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

    private fun launchLocalMusicCategoryListingActivity(position: Int) {
        val intent = Intent(this, LocalMusicCategoryListingActivity::class.java)
        intent.putExtra("ipAddress", ipAddress)
        intent.putExtra("deviceName", deviceNameFromIntent)
        intent.putExtra("serverInfo", localMusicServerItemsList[position])
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        localMusicServerItemsList.clear()
        showLocalMusicServerList()
    }

    private fun showLocalMusicServerList() {
        val localServerInfo = File(filesDir.absolutePath + "/localServerInfo")
        if (!localServerInfo.exists()) {
            try {
                val fOut = openFileOutput("localServerInfo", Context.MODE_PRIVATE)
                val osw = OutputStreamWriter(fOut)
                osw.write("")
                osw.flush()
                osw.close()
            } catch (e: IOException) {
            }
        }
        try {
            val fIn = openFileInput("localServerInfo")
            val isr = InputStreamReader(fIn)
            val content = isr.readText()
            if (content != "") {
                val obj = Gson().fromJson<List<LocalMusicServer>>(content)
                for (it in obj) {
                    val localMusicServerItem =
                        LocalMusicServer(it.username, it.password, it.hostName, it.sharedRootDirectory)
                    localMusicServerItemsList.add(localMusicServerItem)
                    musicServerAdapter!!.notifyDataSetChanged()
                }
            }
        } catch (e: IOException) {
        }
        if (localMusicServerItemsList.count() == 0)
            noDevicesText.visibility = View.VISIBLE
        else
            noDevicesText.visibility = View.INVISIBLE
    }
}