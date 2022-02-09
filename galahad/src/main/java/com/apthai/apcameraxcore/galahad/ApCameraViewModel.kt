package com.apthai.apcameraxcore.galahad

import com.apthai.apcameraxcore.common.ApCameraBaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

class ApCameraViewModel : ApCameraBaseViewModel<ApCameraNavigator>() {

    private val job : Job by lazy { SupervisorJob() }
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    override fun tag(): String = ApCameraViewModel::class.java.simpleName
}