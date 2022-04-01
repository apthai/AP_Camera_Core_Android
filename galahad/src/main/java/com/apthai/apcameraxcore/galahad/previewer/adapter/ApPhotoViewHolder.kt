package com.apthai.apcameraxcore.galahad.previewer.adapter

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.apthai.apcameraxcore.common.model.ApPhoto
import com.apthai.apcameraxcore.galahad.databinding.ItemGalahadPhotoViewBinding
import com.bumptech.glide.Glide

class ApPhotoViewHolder(private val context: Context, itemView: View) : RecyclerView.ViewHolder(itemView), ApPhotoViewNavigator {

    private val binding get() = ItemGalahadPhotoViewBinding.bind(itemView)

    override fun initView(apPhoto: ApPhoto, position: Int) {
        Glide.with(context).load(apPhoto.uriPath).into(binding.apGalahadItemImageView)
    }
}
