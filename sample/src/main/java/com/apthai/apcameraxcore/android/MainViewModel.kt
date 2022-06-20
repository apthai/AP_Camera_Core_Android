package com.apthai.apcameraxcore.android

import com.apthai.apcameraxcore.common.ApCameraBaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

class MainViewModel : ApCameraBaseViewModel<MainNavigator>() {

    private val job: Job by lazy { SupervisorJob() }
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun tag(): String = MainViewModel::class.java.simpleName
}
