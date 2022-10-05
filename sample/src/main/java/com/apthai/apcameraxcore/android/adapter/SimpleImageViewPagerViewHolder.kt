package com.apthai.apcameraxcore.android.adapter

import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.apthai.apcameraxcore.android.databinding.ItemImageViewBinding
import com.bumptech.glide.Glide

class SimpleImageViewPagerViewHolder(private val mItemView: View) :
    RecyclerView.ViewHolder(mItemView) {
    private val binding = ItemImageViewBinding.bind(itemView)

    fun initView(uriStr: String) {
        Glide.with(this.mItemView.context).load(Uri.parse(uriStr))
            .fitCenter()
            .into(this.binding.itemImageViewImg)
    }
}