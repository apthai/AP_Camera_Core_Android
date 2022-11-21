package com.apthai.apcameraxcore.galahad.previewer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.apthai.apcameraxcore.common.model.ApPhoto
import com.apthai.apcameraxcore.galahad.R

@Suppress("unused")
class ApPagerPhotoViewAdapter(private val context: Context) : RecyclerView.Adapter<ApPagerPhotoViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var apPhotoList: MutableList<ApPhoto> = ArrayList()
    private var listener: OnPhotoViewEditorEventListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApPagerPhotoViewHolder {
        val view = inflater.inflate(R.layout.item_ap_previewer_pager_view, parent, false)
        return ApPagerPhotoViewHolder(context, view).listen { _, _ ->
//            val currentPhoto = apPhotoList[position]
            // TODO with itemClick
        }
    }

    override fun onBindViewHolder(holder: ApPagerPhotoViewHolder, position: Int) {
        val apPhoto = apPhotoList[position]
        holder.initView(apPhoto)
    }

    private fun <T : RecyclerView.ViewHolder> T.listen(
        event: (position: Int, type: Int) -> Unit
    ): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }

    override fun getItemCount(): Int = apPhotoList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(apPhotoList: MutableList<ApPhoto>) {
        this.apPhotoList = apPhotoList
        try {
            notifyDataSetChanged()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    fun setOnPhotoViewEditorEventListener(listener: OnPhotoViewEditorEventListener) {
        this.listener = listener
    }

    interface OnPhotoViewEditorEventListener {

        fun onPhotoViewEditorItemClick(apPhoto: ApPhoto, view: View)
    }
}
