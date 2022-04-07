package com.apthai.apcameraxcore.galahad.editor.fragment.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.apthai.apcameraxcore.galahad.R

class EmojiListAdapter(
    private val context: Context,
    private val emojiStrList: MutableList<String>
) : RecyclerView.Adapter<EmojiViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var listener: OnEmojiItemEventListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        val view = inflater.inflate(R.layout.item_ap_editor_emoji_view, parent, false)
        return EmojiViewHolder(context, view).listen { position, _ ->
            val emojiStr = emojiStrList[position]
            listener?.onEmojiClick(emojiStr)
        }
    }

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        val emojiStr = emojiStrList[position]
        holder.initView(emojiStr)
    }

    override fun getItemCount(): Int = emojiStrList.size

    private fun <T : RecyclerView.ViewHolder> T.listen(
        event: (position: Int, type: Int) -> Unit
    ): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }

    fun setOnEmojiItemEventListener(listener: OnEmojiItemEventListener) {
        this.listener = listener
    }

    interface OnEmojiItemEventListener {

        fun onEmojiClick(emojiStr: String?)
    }
}