package com.apthai.apcameraxcore.galahad.previewer.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.apthai.apcameraxcore.galahad.previewer.ApTransitionPreviewActivity
import com.apthai.apcameraxcore.galahad.util.ApCameraConst

class ApJustPreviewResultContract(private val fromScreenTag: String) :
    ActivityResultContract<String, String?>() {

    override fun createIntent(context: Context, input: String): Intent =
        ApTransitionPreviewActivity.getInstance(context, input, fromScreenTag)

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        if (resultCode != Activity.RESULT_OK) {
            return null
        }
        return intent?.getStringExtra(ApCameraConst.ApPreviewPayload.AP_PREVIEW_URI_STR_PAYLOAD)
    }
}
