package com.apthai.apcameraxcore.galahad.previewer

import com.apthai.apcameraxcore.common.ApCameraBaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

class ApTransitionPreviewViewModel : ApCameraBaseViewModel<ApTransitionPreviewNavigator>() {

    private val job: Job by lazy { SupervisorJob() }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun tag(): String = ApTransitionPreviewViewModel::class.java.simpleName
}
