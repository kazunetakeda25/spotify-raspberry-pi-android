package com.rpi.streaming.streamingplayer

import android.content.Context
import android.media.AudioManager
import android.database.ContentObserver
import android.os.Handler
import android.util.Log
import com.rpi.streaming.streamingplayer.R.id.ipAddress
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException


class AudioSettingsContentObserver(internal var context: Context, handler: Handler, var ipAddress: String?) : ContentObserver(handler) {
    private var previousVolume: Int = 0

    init {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)

    }

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        val ip = "$ipAddress:3000"
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolumePercentage = 100 * currentVolume / maxVolume
        val client = OkHttpClient()
        val urlBuilder = HttpUrl.parse("http://$ip/volume")!!.newBuilder()
        urlBuilder.addQueryParameter("volume", currentVolumePercentage.toString())
        val url = urlBuilder.build().toString()
        val request = Request.Builder()
            .url(url)
            .build()
        Thread(Runnable {
            try {
                val call = client.newCall(request)
                val response = call.execute()
                if (response.isSuccessful) {
                    return@Runnable
                }
            } catch (e: IOException) {
            }
        }).start()
    }
}