package com.apthai.apcameraxcore.galahad.tools

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class ApCameraToolMainActivityResultContract : ActivityResultContract<String, String>() {
    override fun createIntent(context: Context, input: String): Intent {
        return ApCameraToolMainActivity.getIntent(context, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String {
        return if (resultCode == Activity.RESULT_OK) intent?.extras?.getString(
            ApCameraToolMainActivity.KEY_ITEM_IMAGE_FILE_PATH
        ) ?: "" else ""
    }
}