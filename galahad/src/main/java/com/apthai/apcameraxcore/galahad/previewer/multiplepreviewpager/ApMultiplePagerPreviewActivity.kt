package com.apthai.apcameraxcore.galahad.previewer.multiplepreviewpager

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.apthai.apcameraxcore.common.ApCameraBaseActivity

class ApMultiplePagerPreviewActivity : ApCameraBaseActivity<ApMultiplePagerPreviewViewModel>(),
    ApMultiplePagerPreviewNavigator {

    private lateinit var viewModel: ApMultiplePagerPreviewViewModel

    override fun tag(): String = ApMultiplePagerPreviewActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.viewModel = ViewModelProvider.NewInstanceFactory().create(
            ApMultiplePagerPreviewViewModel::class.java
        )
        this.viewModel.setNavigator(this)
    }

    override fun setUpView() {

    }

    override fun initial() {

    }
}