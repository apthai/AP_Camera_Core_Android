package com.apthai.apcameraxcore.galahad.tools

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.apthai.apcameraxcore.common.ApCameraBaseActivity
import com.apthai.apcameraxcore.common.utils.ImageUtil
import com.apthai.apcameraxcore.galahad.databinding.ActivityApCameraToolMainBinding
import com.apthai.apcameraxcore.galahad.tools.fragment.MainEditPictureToolsFragment

class ApCameraToolMainActivity :
    ApCameraBaseActivity<ApCameraToolMainViewModel>(),
    ApCameraToolMainNavigator {

    companion object {
        const val KEY_ITEM_IMAGE_FILE_PATH: String = "image_file_path"
        private const val KEY_ITEM_IMAGE_IS_FLIP_LENS_FRONT: String = "is_flip_lens_front"
        fun getIntent(
            context: Context,
            imageFilePath: String,
            isFlipFromLensFront: Boolean = false
        ): Intent = Intent(context, ApCameraToolMainActivity::class.java).apply {
            putExtra(KEY_ITEM_IMAGE_FILE_PATH, imageFilePath)
            putExtra(KEY_ITEM_IMAGE_IS_FLIP_LENS_FRONT, isFlipFromLensFront)
        }
    }

    private lateinit var activityApCameraBinding: ActivityApCameraToolMainBinding
    private val binding get() = activityApCameraBinding
    private val sharedViewModel: ApCameraToolSharedViewModel by viewModels()

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
            .commit()

        sharedViewModel.customImageBitmap.observe(this) {
            it?.let {
            }
        }
    }

    override fun initial() {
        val pathFile = this.getPathFileFromIntent()
        if (pathFile.isNotEmpty()) {
            this.setConvertImagePathFileToBitmap(
                pathFile,
                this.getIsFlipFromLensFrontIntent()
            )
        }
    }

    override fun getPathFileFromIntent(): String {
        return intent.getStringExtra(KEY_ITEM_IMAGE_FILE_PATH) ?: ""
    }

    override fun getIsFlipFromLensFrontIntent(): Boolean {
        return intent.getBooleanExtra(KEY_ITEM_IMAGE_IS_FLIP_LENS_FRONT, false)
    }

    override fun setConvertImagePathFileToBitmap(
        fullPathFile: String,
        isFlipFromLensFront: Boolean
    ) {
        val bitmap = ImageUtil.bitmapFromFilePath(fullPathFile, isFlipFromLensFront)
        sharedViewModel.customImageBitmap.postValue(bitmap)
    }
}
