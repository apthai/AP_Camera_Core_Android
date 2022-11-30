package com.apthai.apcameraxcore.android.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.apthai.apcameraxcore.android.R

class SimpleImageViewPagerAdapter(private val imageUriList: ArrayList<String>) :
    RecyclerView.Adapter<SimpleImageViewPagerViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SimpleImageViewPagerViewHolder {
        return SimpleImageViewPagerViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_image_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SimpleImageViewPagerViewHolder, position: Int) {
        holder.initView(this.imageUriList[position])
    }

    override fun getItemCount(): Int = this.imageUriList.size
}
