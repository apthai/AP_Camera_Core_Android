package com.apthai.apcameraxcore.galahad.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.apthai.apcameraxcore.galahad.ApCameraActivity
import com.apthai.apcameraxcore.galahad.util.ApCameraConst

class ApCameraContract(private val fromScreenTag: String) :
    ActivityResultContract<Bundle, String>() {

    override fun createIntent(context: Context, input: Bundle): Intent =
        ApCameraActivity.getInstance(context, input, fromScreenTag)

    override fun parseResult(resultCode: Int, intent: Intent?): String =
        if (resultCode == Activity.RESULT_OK) intent?.getStringExtra(ApCameraConst.ApCameraPayload.AP_CAMERA_OUTPUT_URI_STRING)
            ?: "" else ""
}