package com.apthai.apcameraxcore.galahad.editor.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.apthai.apcameraxcore.galahad.editor.ApEditorActivity

class ApEditorResultContract :
    ActivityResultContract<String, String?>() {

    override fun createIntent(context: Context, input: String): Intent =
        ApEditorActivity.getInstance(context, input)

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        if (resultCode != Activity.RESULT_OK) {
            return null
        }
        return intent?.getStringExtra(ApEditorActivity.AP_EDITOR_PHOTO_PAYLOAD)
    }
}
