package com.apthai.apcameraxcore.galahad.previewer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.apthai.apcameraxcore.galahad.R
import com.apthai.apcameraxcore.galahad.model.ApPhoto

class ApPhotoViewListAdapter(private val context : Context) : RecyclerView.Adapter<ApPhotoViewHolder>(){

    private var apPhotoList : MutableList<ApPhoto> = ArrayList()
    private val inflater = LayoutInflater.from(context)
    private var listener : OnPhotoViewItemEventListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApPhotoViewHolder {
        val view = inflater.inflate(R.layout.item_galahad_photo_view, parent, false)
        return ApPhotoViewHolder(context, view).listen { position, _ ->
            val apPhoto = apPhotoList[position]
            listener?.onPhotoClick(view, apPhoto, position)
        }
    }

    override fun onBindViewHolder(holder: ApPhotoViewHolder, position: Int) {
        val apPhoto = apPhotoList[position]
        holder.initView(apPhoto, position)
    }

    override fun getItemCount(): Int = apPhotoList.size

    private fun <T : RecyclerView.ViewHolder> T.listen(
        event: (position: Int, type: Int) -> Unit
    ): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(apPhotos : MutableList<ApPhoto>){
        this.apPhotoList = apPhotos
        notifyDataSetChanged()
    }

    fun setOnPhotoViewItemEventListener(listener: OnPhotoViewItemEventListener){
        this.listener = listener
    }

    interface OnPhotoViewItemEventListener{

        fun onPhotoClick(view : View, apPhoto : ApPhoto, position: Int)
    }
}