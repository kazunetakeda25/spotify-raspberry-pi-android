package com.rpi.streaming.streamingplayer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_connected_devices_listing.*
import java.util.*

class ConnectedDevicesListingActivity : AppCompatActivity() {
    private val connectedDevicesList = ArrayList<ConnectedDevices>()
    private var mAdapter: ConnectedDevicesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connected_devices_listing)
        progressBar.visibility = View.INVISIBLE
        mAdapter = ConnectedDevicesAdapter(connectedDevicesList)
        connectedDevicesRecyclerView!!.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        connectedDevicesRecyclerView!!.layoutManager = mLayoutManager
        connectedDevicesRecyclerView!!.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        connectedDevicesRecyclerView!!.itemAnimator = DefaultItemAnimator()
        connectedDevicesRecyclerView!!.adapter = mAdapter
        connectedDevicesRecyclerView!!.addOnItemTouchListener(
            RecyclerTouchListener(
                applicationContext,
                connectedDevicesRecyclerView!!,
                object : RecyclerTouchListener.ClickListener {
                    override fun onLongClick(view: View?, position: Int) : Boolean {
                        return true
                    }
                    override fun onClick(view: View, position: Int) {
                        loadActivePlaylist(position)
                    }
                })
        )
        listConnectedDevices()
//        scanDevices()
//        scanButton.setOnClickListener {
//            scanDevices()
//        }
    }

    private fun loadActivePlaylist(index: Int) {
        val intent = Intent(this, PlaylistActivity::class.java)
        Log.d(this.localClassName, connectedDevicesList[index].ipAddress)
        intent.putExtra("ipAddress", connectedDevicesList[index].ipAddress)
        intent.putExtra("deviceName", connectedDevicesList[index].deviceName)
        startActivity(intent)
    }

//    private fun scanDevices() {
//        Thread(Runnable {
////            progressBar.visibility = View.VISIBLE
//            this@ConnectedDevicesListingActivity.run {
//                try {
//                    val context = this.applicationContext
//
//                    if (context == null) {
//                    } else {
//                        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//                        val activeNetwork = cm.activeNetworkInfo
//                        val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
//                        val connectionInfo = wm.connectionInfo
//                        val ipAddress = connectionInfo.ipAddress
//                        val byteArrayIpAddress = BigInteger.valueOf(ipAddress.toLong()).toByteArray()
//                        var i = 0
//                        var j = byteArrayIpAddress.size - 1
//                        var tmp: Byte
//                        while (j > i) {
//                            tmp = byteArrayIpAddress[j]
//                            byteArrayIpAddress[j] = byteArrayIpAddress[i]
//                            byteArrayIpAddress[i] = tmp
//                            j--
//                            i++
//                        }
//                        val inetAddress = InetAddress.getByAddress(byteArrayIpAddress)
//                        val ipString = inetAddress.hostAddress
//                        Log.d("activeNetwork", activeNetwork.toString())
//                        Log.d("ipAddress", ipAddress.toString())
//                        Log.d("ipString", ipString.toString())
//                        val prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1)
//                        Log.d("prefix", prefix)
//                        for (i in 0..254) {
//                            val testIp = prefix + i.toString()
//                            val address = InetAddress.getByName(testIp)
//                            val reachable = address.isReachable(1000)
//                            val hostName = address.canonicalHostName
//                            if (reachable)
//                                Log.i(
//                                    "Host",
//                                    "Host: " + hostName.toString() + "(" + testIp + ") is reachable!"
//                                )
//                        }
////                        progressBar.visibility = View.INVISIBLE
//                    }
//                } catch (t: Throwable) {
//                    Log.e("Exception", "Well that's not good.", t)
////                    progressBar.visibility = View.INVISIBLE
//                }
//            }
//        }).start()
//    }

    private fun listConnectedDevices() {
        val connectedDevice = ConnectedDevices("192.168.8.122", "orangepizero", getString(R.string.deviceStatusConnected))
        connectedDevicesList.add(connectedDevice)

        mAdapter!!.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val addSourceItem: MenuItem = menu.findItem(R.id.action_add_source)
        addSourceItem.isVisible = false
        val playlistItem: MenuItem = menu.findItem(R.id.action_playlist)
        playlistItem.isVisible = false
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
                true
            }
            R.id.action_add_device -> {
                val intent = Intent(this, ConnectDeviceActivity::class.java)
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
//
//    fun listenSocket() {
//        var server = ServerSocket()
//        try {
//            server = ServerSocket(4444)
//        } catch (e: IOException) {
//            println("Could not listen on port 4444")
//            System.exit(-1)
//        }
//
//        while (true) {
//            val w: ClientWorker
//            try {
//                w = ClientWorker(server.accept(), )
//                val t = Thread(w)
//                t.start()
//            } catch (e: IOException) {
//                println("Accept failed: 4444")
//                System.exit(-1)
//            }
//
//        }
//    }
}