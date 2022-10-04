package com.apthai.apcameraxcore.galahad.previewer.multiplepreviewpager

import androidx.viewpager2.widget.ViewPager2
import com.apthai.apcameraxcore.common.model.ApImageUriAdapter

interface ApMultiplePagerPreviewNavigator {
    fun getIntentImageUriList(): ArrayList<String>
    fun setUpViewPagerAdapter(imageUriAdapterList: ArrayList<ApImageUriAdapter>)
    fun onViewPagerPageChangeCallback(): ViewPager2.OnPageChangeCallback
    fun imageUriListToImageUriAdapter(imageUriList: ArrayList<String>): ArrayList<ApImageUriAdapter>
    fun onClickMenuItemChecked()
    fun onClickMenuConfirm()
    fun checkUpdateCheckedItemCheckedView()
    fun setCountSelectedImage(amountSelected: Int)
    fun getItemImageSelectedList(): ArrayList<ApImageUriAdapter>
    fun updateNewImage(uriStr: String, currentPosition: Int)
    fun showSnackBar(message: String)
}