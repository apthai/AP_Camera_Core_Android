package com.apthai.apcameraxcore.galahad.previewer.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.apthai.apcameraxcore.galahad.previewer.ApMultiplePreviewActivity

class ApMultiplePreviewResultContract : ActivityResultContract<String, String?>() {

    override fun createIntent(context: Context, input: String): Intent = ApMultiplePreviewActivity.getInstance(context, input)

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        if (resultCode != Activity.RESULT_OK){
            return ""
        }
        return intent?.getStringExtra(ApMultiplePreviewActivity.AP_MULTIPLE_PREVIEW_PAYLOAD)
    }
}