package com.apthai.apcameraxcore.galahad.previewer

import com.apthai.apcameraxcore.common.ApCameraBaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

class ApMultiplePreviewViewModel : ApCameraBaseViewModel<ApMultiplePreviewNavigator>() {

    private val job : Job by lazy { SupervisorJob() }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun tag(): String = ApMultiplePreviewViewModel::class.java.simpleName
}