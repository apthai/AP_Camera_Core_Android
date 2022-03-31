package com.apthai.apcameraxcore.galahad.previewer.adapter

import com.apthai.apcameraxcore.common.model.ApPhoto

interface ApPhotoViewNavigator {

    fun initView(apPhoto : ApPhoto, position : Int)
}