package com.rpi.streaming.streamingplayer

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.OkHttpClient
import java.io.IOException

class TuneInMusicAdapter(private val tuneInSubCategoryItemList: List<TuneInMusic>, private val ipAddress: String?, private val deviceName: String?)
    : RecyclerView.Adapter<TuneInMusicAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var checkBox = view.findViewById<View>(R.id.checkBox) as CheckBox
        var image: ImageView = view.findViewById<View>(R.id.musicImage) as ImageView
        var title: TextView = view.findViewById<View>(R.id.fileName) as TextView
        var subImage: ImageView = view.findViewById<View>(R.id.subImage) as ImageView
        var subTitle: TextView = view.findViewById<View>(R.id.subTitle) as TextView
        var href: String? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_tunein_music_item_row, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = tuneInSubCategoryItemList[position]
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = item.bSelected!!
        holder.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            tuneInSubCategoryItemList[position].bSelected = isChecked
            val client = OkHttpClient()
            if (isChecked) {
                val urlBuilder = HttpUrl.parse("http://$ipAddress/tunein/add")!!.newBuilder()
                urlBuilder.addQueryParameter("image", item.image)
                urlBuilder.addQueryParameter("title", item.title)
                urlBuilder.addQueryParameter("subImage", item.subImage)
                urlBuilder.addQueryParameter("subTitle", item.subTitle)
                urlBuilder.addQueryParameter("href", item.href)
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
            } else {
                val urlBuilder = HttpUrl.parse("http://$ipAddress/tunein/remove")!!.newBuilder()
                urlBuilder.addQueryParameter("href", item.href)
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
                        Log.d("Exception", e.toString())
                    }
                }).start()
            }
        }
        Glide.with(holder.itemView.context).load(item.image).into(holder.image)
        holder.title.text = item.title
        Glide.with(holder.itemView.context).load(item.subImage).into(holder.subImage)
        holder.subTitle.text = item.subTitle
        holder.href = item.href
    }

    override fun getItemCount(): Int {
        return tuneInSubCategoryItemList.size
    }
}