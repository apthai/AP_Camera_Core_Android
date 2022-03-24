package com.apthai.apcameraxcore.galahad.previewer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.apthai.apcameraxcore.galahad.R

class ApImageListAdapter(private val context : Context) : RecyclerView.Adapter<ApImageViewHolder>() {

    private val inflater : LayoutInflater = LayoutInflater.from(context)
    private var imagePathList : MutableList<String> = ArrayList()
    private var listener : OnImageSelectedEventListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApImageViewHolder {
        val view = inflater.inflate(R.layout.item_galahad_image_view, parent, false)
        return ApImageViewHolder(context, view).listen { position, _ ->
            val imagePath = imagePathList[position]
            listener?.onImageSelected(imagePath, position)
        }
    }

    fun setImageSelectedEventListener(listener: OnImageSelectedEventListener){
        this.listener = listener
    }

    override fun onBindViewHolder(holder: ApImageViewHolder, position: Int) {
        val imagePath = imagePathList[position]
        holder.initView(imagePath)
    }

    override fun getItemCount(): Int = imagePathList.size

    fun updateData(imagePaths : MutableList<String>){
        this.imagePathList = imagePaths
    }

    private fun <T : RecyclerView.ViewHolder> T.listen(
        event: (position: Int, type: Int) -> Unit
    ): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }

    interface OnImageSelectedEventListener{

        fun onImageSelected(imagePath : String, position: Int)
    }
}