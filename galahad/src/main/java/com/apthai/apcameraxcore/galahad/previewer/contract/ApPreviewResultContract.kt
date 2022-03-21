package com.apthai.apcameraxcore.galahad.previewer.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import com.apthai.apcameraxcore.galahad.previewer.ApPreviewActivity

class ApPreviewResultContract : ActivityResultContract<String, String?>() {

    companion object{

        const val AP_PREVIEW_CONTRACT_URI_PAYLOAD_CONST = "ap_preview_contract_uri_payload_cls"
    }

    override fun createIntent(context: Context, input: String): Intent = Intent(context, ApPreviewActivity::class.java).apply {
        putExtra(AP_PREVIEW_CONTRACT_URI_PAYLOAD_CONST, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        if (resultCode != Activity.RESULT_OK){
            return null
        }
        return intent?.getStringExtra(AP_PREVIEW_CONTRACT_URI_PAYLOAD_CONST)
    }
}