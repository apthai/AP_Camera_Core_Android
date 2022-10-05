package com.apthai.apcameraxcore.galahad.previewer.adapter.apmultiplepager

import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.apthai.apcameraxcore.common.model.ApImageUriAdapter
import com.apthai.apcameraxcore.galahad.databinding.ItemApPreviewerPagerViewBinding
import com.bumptech.glide.Glide

class ApMultiplePagerPreviewViewHolder(private val mItemView: View) :
    RecyclerView.ViewHolder(mItemView) {
    private val itemViewBinding = ItemApPreviewerPagerViewBinding.bind(itemView)

    fun initView(imageUri: ApImageUriAdapter) {
        itemViewBinding.itemApCameraPagerPreviewerView.let {
            Glide.with(this.mItemView.context).load(Uri.parse(imageUri.uriStr)).into(it)
        }
    }
}