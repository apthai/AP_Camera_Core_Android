package com.apthai.apcameraxcore.galahad.previewer.adapter

import com.apthai.apcameraxcore.galahad.model.ApPhoto

interface ApPhotoViewNavigator {

    fun initView(apPhoto : ApPhoto, position : Int)
}