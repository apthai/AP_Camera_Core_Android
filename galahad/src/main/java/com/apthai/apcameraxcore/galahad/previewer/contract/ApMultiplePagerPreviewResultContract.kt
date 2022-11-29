package com.apthai.apcameraxcore.galahad.previewer.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.apthai.apcameraxcore.galahad.previewer.multiplepreviewpager.ApMultiplePagerPreviewActivity

class ApMultiplePagerPreviewResultContract :
    ActivityResultContract<ArrayList<String>, ArrayList<String>>() {
    override fun createIntent(context: Context, input: ArrayList<String>): Intent {
        return ApMultiplePagerPreviewActivity.getIntent(context, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): ArrayList<String> {
        return if (resultCode != Activity.RESULT_OK) arrayListOf() else intent?.extras?.getStringArrayList(
            ApMultiplePagerPreviewActivity.CONST_IMAGE_LIST_FOR_RESULT_NAME
        ) ?: arrayListOf()
    }
}
