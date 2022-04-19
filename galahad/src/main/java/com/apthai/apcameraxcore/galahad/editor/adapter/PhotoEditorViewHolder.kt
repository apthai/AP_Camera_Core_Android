package com.apthai.apcameraxcore.galahad.editor.adapter

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.apthai.apcameraxcore.common.model.ApPhoto
import com.apthai.apcameraxcore.galahad.databinding.ItemApEditorPhotoViewBinding
import com.bumptech.glide.Glide

class PhotoEditorViewHolder(private val context : Context, itemView : View) : RecyclerView.ViewHolder(itemView), PhotoEditorNavigator {

    private val itemViewBinding = ItemApEditorPhotoViewBinding.bind(itemView)

    override fun initView(apPhoto: ApPhoto) {
        val photoUri = apPhoto.uriPath
        itemViewBinding.itemApCameraEditorPhotoView.let {
            Glide.with(context).load(photoUri).into(it.source)
        }
    }
}