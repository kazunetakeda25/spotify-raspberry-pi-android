package com.rpi.streaming.streamingplayer

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class TuneInSubCategoryAdapter(private val tuneInSubCategoryList: List<TuneInSubCategory>) : RecyclerView.Adapter<TuneInSubCategoryAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById<View>(R.id.fileName) as TextView
        var href: String? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_tunein_sub_category_item_row, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = tuneInSubCategoryList[position]
        holder.title.text = item.title
        holder.href = item.href
    }

    override fun getItemCount(): Int {
        return tuneInSubCategoryList.size
    }
}