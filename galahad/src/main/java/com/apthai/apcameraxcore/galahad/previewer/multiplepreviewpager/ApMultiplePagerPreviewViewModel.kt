package com.apthai.apcameraxcore.galahad.previewer.multiplepreviewpager

import android.net.Uri
import com.apthai.apcameraxcore.common.ApCameraBaseViewModel
import com.apthai.apcameraxcore.common.model.ApImageUriAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import java.io.File
import kotlin.coroutines.CoroutineContext

class ApMultiplePagerPreviewViewModel : ApCameraBaseViewModel<ApMultiplePagerPreviewNavigator>() {

    private val job: Job by lazy { SupervisorJob() }
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job

    override fun tag(): String = ApMultiplePagerPreviewViewModel::class.java.simpleName

    fun removeFileFromUri(uriStr: String) {
        val uri = Uri.parse(uriStr)
        uri.path?.let { uriPath ->
            val file = File(uriPath)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    fun imageAdapterToImageListStr(imageUriAdapter: MutableList<ApImageUriAdapter>): ArrayList<String> {
        val imageList: ArrayList<String> = arrayListOf()
        imageUriAdapter.forEach {
            imageList.add(it.uriStr)
        }
        return imageList
    }
}