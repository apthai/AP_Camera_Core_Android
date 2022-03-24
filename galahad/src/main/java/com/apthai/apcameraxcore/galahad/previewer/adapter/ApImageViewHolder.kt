package com.apthai.apcameraxcore.galahad.previewer.adapter

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.apthai.apcameraxcore.galahad.databinding.ItemGalahadImageViewBinding
import com.bumptech.glide.Glide

class ApImageViewHolder(private val context : Context, itemView : View): RecyclerView.ViewHolder(itemView), ApImageViewHolderNavigator {

    private val binding = ItemGalahadImageViewBinding.bind(itemView)

    override fun initView(imagePath: String) {
        if (imagePath.isEmpty()){
            return
        }
        binding.itemGalahadImageView.let { imageView ->
            Glide.with(context).load(imagePath).into(imageView)
        }
    }
}