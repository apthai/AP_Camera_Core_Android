package com.apthai.apcameraxcore.galahad.previewer.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.apthai.apcameraxcore.common.model.ApPhoto
import com.apthai.apcameraxcore.galahad.previewer.ApTransitionPreviewActivity

class ApTransitionPreviewResultContract(private val fromScreenTag: String) : ActivityResultContract<ApPhoto, String?>() {

    companion object {
        const val AP_TRANSITION_PREVIEW_PAYLOAD = "ap_transition_preview_payload"
    }

    override fun createIntent(context: Context, input: ApPhoto): Intent = ApTransitionPreviewActivity.getInstance(context, input, fromScreenTag)

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        if (resultCode != Activity.RESULT_OK) {
            return null
        }
        return intent?.getStringExtra(AP_TRANSITION_PREVIEW_PAYLOAD)
    }
}
