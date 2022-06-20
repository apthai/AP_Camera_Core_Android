package com.apthai.apcameraxcore.galahad.editor.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.apthai.apcameraxcore.galahad.R
import com.apthai.apcameraxcore.galahad.databinding.FragmentApEditorStickerEmojiDialogBinding
import com.apthai.apcameraxcore.galahad.editor.fragment.adapter.StickerListAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ApEditorStickerSelectorFragment :
    BottomSheetDialogFragment(),
    StickerListAdapter.OnStickerItemEventListener {

    private val mBottomSheetBehaviorCallback: BottomSheetCallback = object : BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    private var fragmentApEditorStickerEmojiDialogBinding: FragmentApEditorStickerEmojiDialogBinding? =
        null
    private val binding get() = fragmentApEditorStickerEmojiDialogBinding
    private var stickerListAdapter: StickerListAdapter? = null
    private var listener: OnStickerSelectedListener? = null

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView =
            View.inflate(context, R.layout.fragment_ap_editor_sticker_emoji_dialog, null)
        fragmentApEditorStickerEmojiDialogBinding =
            FragmentApEditorStickerEmojiDialogBinding.bind(contentView)
        binding?.root?.let { ctView ->
            dialog.setContentView(ctView)
            val params = (ctView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior
            if (behavior != null && behavior is BottomSheetBehavior<*>) {
                behavior.setBottomSheetCallback(mBottomSheetBehaviorCallback)
            }
            (ctView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))
            val gridLayoutManager = GridLayoutManager(activity, 3)
            binding?.apEditorEmojiStickerRecyclerView?.layoutManager = gridLayoutManager
            stickerListAdapter = StickerListAdapter(requireActivity(), stickerPathList)
            binding?.apEditorEmojiStickerRecyclerView?.adapter = stickerListAdapter
            binding?.apEditorEmojiStickerRecyclerView?.setHasFixedSize(true)
            binding?.apEditorEmojiStickerRecyclerView?.setItemViewCacheSize(stickerPathList.size)
            stickerListAdapter?.setOnStickerItemEventListener(this)
        }
    }

    override fun onStickerClick(stickerUrl: String) {
        Glide.with(requireContext())
            .asBitmap()
            .load(stickerUrl)
            .into(object : CustomTarget<Bitmap?>(256, 256) {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    listener?.onStickerSelected(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
        dismiss()
    }

    fun setOnStickerSelectedListener(listener: OnStickerSelectedListener) {
        this.listener = listener
    }

    interface OnStickerSelectedListener {

        fun onStickerSelected(bitmap: Bitmap)
    }

    companion object {
        // Image Urls from flaticon(https://www.flaticon.com/stickers-pack/food-289)
        private val stickerPathList = arrayListOf<String>(
            "https://cdn-icons-png.flaticon.com/256/4392/4392452.png",
            "https://cdn-icons-png.flaticon.com/256/4392/4392455.png",
            "https://cdn-icons-png.flaticon.com/256/4392/4392459.png",
            "https://cdn-icons-png.flaticon.com/256/4392/4392462.png",
            "https://cdn-icons-png.flaticon.com/256/4392/4392465.png",
            "https://cdn-icons-png.flaticon.com/256/4392/4392467.png",
            "https://cdn-icons-png.flaticon.com/256/4392/4392469.png",
            "https://cdn-icons-png.flaticon.com/256/4392/4392471.png",
            "https://cdn-icons-png.flaticon.com/256/4392/4392522.png",
        )
    }
}
