package com.apthai.apcameraxcore.galahad.tools

import android.content.Context
import android.content.Intent
import com.apthai.apcameraxcore.common.ApCameraBaseActivity

class ApCameraToolMainActivity : ApCameraBaseActivity<ApCameraToolMainViewModel>(),
    ApCameraToolMainNavigator {

    companion object {
        private const val KEY_ITEM_IMAGE_FILE_PATH: String = "image_file_path"
        fun getIntent(context: Context, imageFilePath: String): Intent =
            Intent(context, ApCameraToolMainActivity::class.java).apply {
                putExtra(KEY_ITEM_IMAGE_FILE_PATH, imageFilePath)
            }
    }

    override fun tag(): String = ApCameraToolMainActivity::class.java.simpleName

    override fun setUpView() {

    }

    override fun initial() {

    }
}