package com.apthai.apcameraxcore.galahad.previewer.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.apthai.apcameraxcore.galahad.previewer.ApPagerPreviewActivity
import com.apthai.apcameraxcore.galahad.util.ApCameraUtil

class ApPagerPreviewResultContract : ActivityResultContract<String, String?>() {

    override fun createIntent(context: Context, input: String): Intent = ApPagerPreviewActivity.getInstance(context, fromScreenTag = input)

    override fun parseResult(resultCode: Int, intent: Intent?): String? = if (resultCode != Activity.RESULT_OK) null else intent?.getStringExtra(ApCameraUtil.Generic.AP_CAMERA_DEFAULT_FROM_SCREEN_TAG)
}