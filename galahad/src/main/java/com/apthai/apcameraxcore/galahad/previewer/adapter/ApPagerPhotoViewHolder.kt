package com.apthai.apcameraxcore.galahad.previewer.adapter

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.apthai.apcameraxcore.common.model.ApPhoto
import com.apthai.apcameraxcore.galahad.databinding.ItemApPreviewerPagerViewBinding
import com.bumptech.glide.Glide

class ApPagerPhotoViewHolder(private val context: Context, itemView: View) : RecyclerView.ViewHolder(itemView), ApPagerPhotoViewNavigator {

    private val itemViewBinding = ItemApPreviewerPagerViewBinding.bind(itemView)

    override fun initView(apPhoto: ApPhoto) {
        val photoUri = apPhoto.uriPath
        itemViewBinding.itemApCameraPagerPreviewerView.let {
            Glide.with(context).load(photoUri).into(it)
        }
    }
}
