package com.apthai.apcameraxcore.galahad.previewer.multiplepreviewpager

import com.apthai.apcameraxcore.common.ApCameraBaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

class ApMultiplePagerPreviewViewModel : ApCameraBaseViewModel<ApMultiplePagerPreviewNavigator>() {

    private val job: Job by lazy { SupervisorJob() }
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job

    override fun tag(): String = ApMultiplePagerPreviewViewModel::class.java.simpleName
}