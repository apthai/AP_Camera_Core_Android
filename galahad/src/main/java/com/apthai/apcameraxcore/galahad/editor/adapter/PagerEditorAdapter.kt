package com.apthai.apcameraxcore.galahad.editor.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.apthai.apcameraxcore.common.model.ApPhoto
import com.apthai.apcameraxcore.galahad.editor.fragment.ApEditorFragment

class PagerEditorAdapter(fragmentManager : FragmentManager, lifecycle: Lifecycle, private val apPhotoList : MutableList<ApPhoto>) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = apPhotoList.size

    override fun createFragment(position: Int): Fragment = ApEditorFragment.getInstance(apPhoto = apPhotoList[position])
}