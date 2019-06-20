package com.teaphy.wineuidemo.viewpager2

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.teaphy.wineuidemo.R

class BannerAdapter(val list: List<String> = mutableListOf()) : RecyclerView.Adapter<BannerAdapter.ViewHolder>(){

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context)
			.inflate(R.layout.item_banner, parent, false)
		return ViewHolder(view)
	}

	override fun getItemCount(): Int {
		return list.size
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {

		Log.e("teaphy", "imageUrl: ${list[position]}, position: $position")

		Glide.with(holder.imageView)
			.asBitmap()
			.load(list[position])
			.into(holder.imageView)

		holder.positionText.text = position.toString()
	}

	class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
		val imageView = itemView.findViewById<ImageView>(R.id.image_view)
		val positionText = itemView.findViewById<TextView>(R.id.position_text)
	}
}