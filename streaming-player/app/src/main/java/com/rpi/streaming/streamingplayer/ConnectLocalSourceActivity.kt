package com.rpi.streaming.streamingplayer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.gson.Gson
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbFile
import kotlinx.android.synthetic.main.activity_connect_local_music_server.*
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.regex.Pattern

class ConnectLocalSourceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_local_music_server)
        val filters = arrayOfNulls<InputFilter>(1)
        filters[0] = InputFilter { source, start, end, dest, dstart, dend ->
            if (end > start) {
                val destTxt = dest.toString()
                val resultingTxt =
                    destTxt.substring(0, dstart) + source.subSequence(start, end) + destTxt.substring(dend)
                if (!resultingTxt.matches("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?".toRegex())) {
                    return@InputFilter ""
                } else {
                    val splits = resultingTxt.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    for (i in splits.indices) {
                        if (Integer.valueOf(splits[i]) > 255) {
                            return@InputFilter ""
                        }
                    }
                }
            }
            null
        }
        ipAddressInputField.filters = filters
        connectButton.setOnClickListener {
            if (ipAddressInputField.text.toString() == "") {
                ipAddressInputField.error = "IP Address is required!"
                return@setOnClickListener
            }
            val ipAddressPattern = Pattern.compile("((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                        + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                        + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                        + "|[1-9][0-9]|[0-9]))")
            val matcher = ipAddressPattern.matcher(ipAddressInputField.text.toString())
            if (!matcher.matches()) {
                ipAddressInputField.error = "IP Address is incorrect!"
                return@setOnClickListener
            }
            Thread(Runnable {
                val homeUrl = "smb://" + ipAddressInputField.text.toString() + "/LocalMusic/"
                var exists = false
                this@ConnectLocalSourceActivity.run {
                    try {
                        val authentication = NtlmPasswordAuthentication(null, usernameInputField.text.toString(), passwordInputField.text.toString())
                        val smbFile = SmbFile(homeUrl, authentication)
                        if (smbFile.exists()) {
                            exists = true
                        }
                    } catch (e: Exception) {
                    }
                }
                this@ConnectLocalSourceActivity.runOnUiThread {
                    if (!exists) {
                        connectButton.error = "Incorrect Local Music Server Credentials!"
                        passwordInputField.setText("")
                        return@runOnUiThread
                    }
                    val newLocalMusicServerItem = LocalMusicServer(usernameInputField.text.toString(), passwordInputField.text.toString(), ipAddressInputField.text.toString(), "LocalMusic/")
                    val addedLocalMusicServerItems = mutableListOf<LocalMusicServer>()
                    val localServerInfo = File(filesDir.absolutePath + "/localServerInfo")
                    if (!localServerInfo.exists()) {
                        try {
                            val fOut = openFileOutput("localServerInfo", Context.MODE_PRIVATE)
                            val osw = OutputStreamWriter(fOut)
                            osw.write("")
                            osw.flush()
                            osw.close()
                        } catch (e: IOException) {
                            Toast.makeText(this, "Error occured!", Toast.LENGTH_LONG).show()
                            finish()
                            return@runOnUiThread
                        }
                    }
                    try {
                        val fIn = openFileInput("localServerInfo")
                        val isr = InputStreamReader(fIn)
                        val content = isr.readText()
                        if (content != "") {
                            val obj = Gson().fromJson<MutableList<LocalMusicServer>>(content)
                            for (it in obj) {
                                val localMusicServerItem =
                                    LocalMusicServer(it.username, it.password, it.hostName, it.sharedRootDirectory)
                                addedLocalMusicServerItems.add(localMusicServerItem)
                            }
                            for (it in addedLocalMusicServerItems) {
                                if (it.hostName == newLocalMusicServerItem.hostName) {
                                    Toast.makeText(this, "Already added!", Toast.LENGTH_LONG).show()
                                    finish()
                                    return@runOnUiThread
                                }
                            }
                        }
                    } catch (e: IOException) {
                        Toast.makeText(this, "Error occured!", Toast.LENGTH_LONG).show()
                        finish()
                        return@runOnUiThread
                    }
                    addedLocalMusicServerItems.add(newLocalMusicServerItem)
                    try {
                        val fOut = openFileOutput("localServerInfo", Context.MODE_PRIVATE)
                        val osw = OutputStreamWriter(fOut)
                        val content = Gson().toJson(addedLocalMusicServerItems)
                        osw.write(content)
                        osw.flush()
                        osw.close()
                    } catch (e: IOException) {
                        Toast.makeText(this, "Error occured!", Toast.LENGTH_LONG).show()
                        finish()
                        return@runOnUiThread
                    }
                    Toast.makeText(this, "A Local Music Server Added!", Toast.LENGTH_LONG).show()
                    finish()
                }
            }).start()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val addSourceItem: MenuItem = menu.findItem(R.id.action_add_source)
        addSourceItem.isVisible = false
        val addDeviceItem: MenuItem = menu.findItem(R.id.action_add_device)
        addDeviceItem.isVisible = false
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
        return when {
            item.itemId == R.id.action_main -> {
                val intent = Intent(this, ConnectedDevicesListingActivity::class.java)
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