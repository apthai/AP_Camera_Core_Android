package com.apthai.apcameraxcore.galahad.tools

import com.apthai.apcameraxcore.common.ApCameraBaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

class ApCameraToolMainViewModel : ApCameraBaseViewModel<ApCameraToolMainNavigator>() {

    private val job: Job by lazy { SupervisorJob() }
    override val coroutineContext: CoroutineContext get() = job + Dispatchers.IO

    override fun tag(): String = ApCameraToolMainViewModel::class.java.simpleName
}
