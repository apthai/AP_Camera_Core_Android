package com.apthai.apcameraxcore.galahad.editor.fragment.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.apthai.apcameraxcore.galahad.R

class StickerListAdapter(
    private val context: Context,
    private val stickerUrlList: MutableList<String>
) : RecyclerView.Adapter<StickerViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var listener: OnStickerItemEventListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerViewHolder {
        val view = inflater.inflate(R.layout.item_ap_editor_sticker_view, parent, false)
        return StickerViewHolder(context, view).listen { position, _ ->
            val stickerUrl = stickerUrlList[position]
            listener?.onStickerClick(stickerUrl)
        }
    }

    override fun onBindViewHolder(holder: StickerViewHolder, position: Int) {
        val stickerUrl = stickerUrlList[position]
        holder.initView(stickerUrl)
    }

    override fun getItemCount(): Int = stickerUrlList.size

    private fun <T : RecyclerView.ViewHolder> T.listen(
        event: (position: Int, type: Int) -> Unit
    ): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }

    fun setOnStickerItemEventListener(listener: OnStickerItemEventListener){
        this.listener = listener
    }

    interface OnStickerItemEventListener {

        fun onStickerClick(stickerUrl: String)
    }
}