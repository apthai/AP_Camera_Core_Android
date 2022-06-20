package com.apthai.apcameraxcore.galahad.editor.fragment.adapter

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.apthai.apcameraxcore.galahad.databinding.ItemApEditorEmojiViewBinding

class EmojiViewHolder(private val context: Context, itemView: View) : RecyclerView.ViewHolder(itemView), EmojiListAdapterNavigator {

    private val binding = ItemApEditorEmojiViewBinding.bind(itemView)

    override fun initView(emojiStr: String) {
        binding.apEditorItemEmojiView.text = emojiStr
    }
}
