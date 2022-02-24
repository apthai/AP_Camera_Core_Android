package com.apthai.apcameraxcore.galahad.tools

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.apthai.apcameraxcore.common.ApCameraBaseActivity
import com.apthai.apcameraxcore.galahad.databinding.ActivityApCameraToolMainBinding
import com.apthai.apcameraxcore.galahad.tools.fragment.MainEditPictureToolsFragment

class ApCameraToolMainActivity : ApCameraBaseActivity<ApCameraToolMainViewModel>(),
    ApCameraToolMainNavigator {


    companion object {
        const val KEY_ITEM_IMAGE_FILE_PATH: String = "image_file_path"
        fun getIntent(context: Context, imageFilePath: String): Intent =
            Intent(context, ApCameraToolMainActivity::class.java).apply {
                putExtra(KEY_ITEM_IMAGE_FILE_PATH, imageFilePath)
            }
    }

    private lateinit var activityApCameraBinding: ActivityApCameraToolMainBinding
    private val binding get() = activityApCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityApCameraBinding = ActivityApCameraToolMainBinding.inflate(layoutInflater)
        setContentView(activityApCameraBinding.root)
        this.setUpView()

    }

    override fun tag(): String = ApCameraToolMainActivity::class.java.simpleName

    override fun setUpView() {
        supportFragmentManager.beginTransaction()
            .replace(
                binding.actApCameraToolFrmContainerView.id,
                MainEditPictureToolsFragment.getInstance(this.getPathFileFromIntent()),
                MainEditPictureToolsFragment.TAG_FRAGMENT_NAME
            )
            .setReorderingAllowed(true)
            .addToBackStack(MainEditPictureToolsFragment.TAG_FRAGMENT_NAME)
            .commit()
    }

    override fun initial() {

    }

    override fun getPathFileFromIntent(): String {
        return intent.getStringExtra(KEY_ITEM_IMAGE_FILE_PATH) ?: ""
    }
}