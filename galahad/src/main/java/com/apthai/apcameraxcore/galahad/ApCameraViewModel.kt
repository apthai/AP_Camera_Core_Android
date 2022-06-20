package com.apthai.apcameraxcore.galahad

import android.graphics.Bitmap
import com.apthai.apcameraxcore.common.ApCameraBaseViewModel
import com.apthai.apcameraxcore.common.utils.ImageUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

class ApCameraViewModel : ApCameraBaseViewModel<ApCameraNavigator>() {

    private val job: Job by lazy { SupervisorJob() }
    private val navigator: ApCameraNavigator? by lazy { getNavigator()?.get() }

    override val coroutineContext: CoroutineContext get() = job + Dispatchers.IO

    override fun tag(): String = ApCameraViewModel::class.java.simpleName

    fun saveImageToLocal(rootDir: String, fileName: String, imageBitmap: Bitmap) {
        launch {
            navigator?.loadDing(true)
            try {
                ImageUtil.saveImageBitmap(rootDir, fileName, imageBitmap)
                delay(500)
                navigator?.loadDing(false)
                navigator?.saveImageToLocalSuccess("$rootDir/$fileName")
            } catch (e: Exception) {
                e.printStackTrace()
                navigator?.loadDing(false)
            }
        }
    }
}
