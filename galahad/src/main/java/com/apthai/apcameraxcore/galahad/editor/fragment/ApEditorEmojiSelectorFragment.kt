package com.apthai.apcameraxcore.galahad.editor.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.apthai.apcameraxcore.galahad.R
import com.apthai.apcameraxcore.galahad.databinding.FragmentApEditorStickerEmojiDialogBinding
import com.apthai.apcameraxcore.galahad.editor.fragment.adapter.EmojiListAdapter
import com.apthai.apcameraxcore.galahad.util.EmojiConst
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ApEditorEmojiSelectorFragment :
    BottomSheetDialogFragment(),
    EmojiListAdapter.OnEmojiItemEventListener {

    private val mBottomSheetBehaviorCallback: BottomSheetCallback = object : BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    private var fragmentApEditorStickerBinding: FragmentApEditorStickerEmojiDialogBinding? = null
    private val binding get() = fragmentApEditorStickerBinding

    private var emojiListAdapter: EmojiListAdapter? = null
    private var listener: OnEmojiSelectedListener? = null

    @Suppress("DEPRECATION")
    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView =
            View.inflate(context, R.layout.fragment_ap_editor_sticker_emoji_dialog, null)
        fragmentApEditorStickerBinding = FragmentApEditorStickerEmojiDialogBinding.bind(contentView)
        binding?.root?.let { ctView ->
            dialog.setContentView(ctView)
            val params = (ctView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior
            if (behavior != null && behavior is BottomSheetBehavior<*>) {
                behavior.setBottomSheetCallback(mBottomSheetBehaviorCallback)
            }
            (ctView.parent as View).setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.transparent
                )
            )
            val gridLayoutManager = GridLayoutManager(activity, 5)
            binding?.apEditorEmojiStickerRecyclerView?.layoutManager = gridLayoutManager
            emojiListAdapter = EmojiListAdapter(requireActivity(), emojisList)
            binding?.apEditorEmojiStickerRecyclerView?.adapter = emojiListAdapter
            binding?.apEditorEmojiStickerRecyclerView?.setHasFixedSize(true)
            binding?.apEditorEmojiStickerRecyclerView?.setItemViewCacheSize(emojisList.size)
            emojiListAdapter?.setOnEmojiItemEventListener(this)
        }
    }

    override fun onEmojiClick(emojiStr: String?) {
        listener?.onEmojiSelected(emojiStr)
        dismiss()
    }

    fun setOnEmojiSelectedListener(listener: OnEmojiSelectedListener) {
        this.listener = listener
    }

    interface OnEmojiSelectedListener {

        fun onEmojiSelected(emojiStr: String?)
    }

    companion object {
        private var emojisList = getEmojis()

        /**
         * Provide the list of emoji in form of unicode string
         * @return list of emoji unicode
         */
        private fun getEmojis(): ArrayList<String> {
            val convertedEmojiList = ArrayList<String>()
            EmojiConst.EMOJI_LIST.toMutableList().let { emojiUnicodeList ->
                for (emojiUnicode in emojiUnicodeList) {
                    val emoji = convertEmoji(emojiUnicode)
                    if (emoji.isNotEmpty()) {
                        convertedEmojiList.add(emoji)
                    }
                }
            }
            return convertedEmojiList
        }

        private fun convertEmoji(emoji: String): String {
            return try {
                val convertEmojiToInt = emoji.substring(2).toInt(16)
                String(Character.toChars(convertEmojiToInt))
            } catch (e: NumberFormatException) {
                ""
            }
        }
    }
}
