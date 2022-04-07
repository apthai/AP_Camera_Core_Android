package com.apthai.apcameraxcore.galahad.editor.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.apthai.apcameraxcore.galahad.R
import com.apthai.apcameraxcore.galahad.databinding.FragmentApEditorStickerEmojiDialogBinding
import com.apthai.apcameraxcore.galahad.editor.PhotoApp.Companion.photoApp
import com.apthai.apcameraxcore.galahad.editor.fragment.adapter.EmojiListAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ApEditorEmojiSelectorFragment : BottomSheetDialogFragment(),
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
            (ctView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))
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
        private var emojisList = getEmojis(photoApp)

        /**
         * Provide the list of emoji in form of unicode string
         *
         * @param context context
         * @return list of emoji unicode
         */
        fun getEmojis(context: Context?): ArrayList<String> {
            val convertedEmojiList = ArrayList<String>()
            val emojiList = context!!.resources.getStringArray(R.array.photo_editor_emoji)
            for (emojiUnicode in emojiList) {
                convertedEmojiList.add(convertEmoji(emojiUnicode))
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
