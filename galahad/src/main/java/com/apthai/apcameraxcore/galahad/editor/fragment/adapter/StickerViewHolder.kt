package com.apthai.apcameraxcore.galahad.editor.fragment.adapter

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.apthai.apcameraxcore.galahad.databinding.ItemApEditorStickerViewBinding
import com.bumptech.glide.Glide

class StickerViewHolder(private val context: Context, itemView: View) :
    RecyclerView.ViewHolder(itemView), StickerListAdapterNavigator {

    private val binding = ItemApEditorStickerViewBinding.bind(itemView)

    override fun initView(stickerUrl: String) {
        Glide.with(context)
            .asBitmap()
            .load(stickerUrl)
            .into(binding.apEditorItemStickerImageView)
    }
}
