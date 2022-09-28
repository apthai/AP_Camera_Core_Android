package com.apthai.apcameraxcore.android

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.apthai.apcameraxcore.android.databinding.ActivityMainBinding
import com.apthai.apcameraxcore.common.ApCameraBaseActivity
import com.apthai.apcameraxcore.galahad.ApCameraActivity
import com.apthai.apcameraxcore.galahad.contract.ApCameraContract
import com.apthai.apcameraxcore.galahad.util.ApCameraConst
import com.bumptech.glide.Glide

class MainActivity : ApCameraBaseActivity<MainViewModel>(), MainNavigator, View.OnClickListener {

    override fun tag(): String = MainActivity::class.java.simpleName

    private var activityMainBinding: ActivityMainBinding? = null
    private val binding get() = activityMainBinding

    private var mainViewModel: MainViewModel? = null

    private val apCameraContract =
        registerForActivityResult(ApCameraContract(tag())) { fallbackImageUriStr ->
            if (fallbackImageUriStr.isNullOrEmpty()) return@registerForActivityResult
            Toast.makeText(this, "Retrieve PhotoUri Raw $fallbackImageUriStr", Toast.LENGTH_SHORT)
                .show()
            binding?.mainImageView?.let { imageView ->
                Glide.with(this).load(fallbackImageUriStr).into(imageView)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        mainViewModel = ViewModelProvider.NewInstanceFactory().create(MainViewModel::class.java)
        mainViewModel?.setNavigator(this)

        if (savedInstanceState == null) {
            setUpView()
            initial()
        }
    }

    override fun setUpView() {}

    override fun initial() {
        binding?.mainLaunchCameraButton?.setOnClickListener(this)
    }

    @SuppressLint("CheckResult")
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.main_launch_camera_button -> {
                MaterialDialog(this).show {
                    title(text = "Select once")
                    listItems(
                        items = arrayListOf(
                            "Just Capture and Preview",
                            "Preview with editor",
                            "Multiple Shot and Preview"
                        )
                    ) { dialog, index, _ ->
                        when (index) {
                            0 -> {
                                dialog.dismiss()
                                val cameraBundle = Bundle().apply {
                                    putInt(
                                        ApCameraConst.ApCameraMode.AP_CAMERA_CONST_MODE_NAME,
                                        ApCameraConst.ApCameraMode.AP_CAMERA_VAL_IS_ONLY_CAMERA_APC_MODE
                                    )

                                }
                                apCameraContract.launch(cameraBundle)
                            }
                            1 -> {
                                dialog.dismiss()
                                val cameraBundle = Bundle().apply {
                                    putInt(
                                        ApCameraConst.ApCameraMode.AP_CAMERA_CONST_MODE_NAME,
                                        ApCameraConst.ApCameraMode.AP_CAMERA_VAL_VIEW_GALLERY_MODE
                                    )

                                }
                                apCameraContract.launch(cameraBundle)
                            }
                            else -> {
                                dialog.dismiss()
                                val cameraBundle = Bundle().apply {
                                    putInt(
                                        ApCameraConst.ApCameraMode.AP_CAMERA_CONST_MODE_NAME,
                                        ApCameraConst.ApCameraMode.AP_CAMERA_VAL_MULTIPLE_SHOT_PREVIEW_MODE
                                    )

                                }
                                apCameraContract.launch(cameraBundle)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun launchCameraScreen() {
        val cameraIntent = Intent(this, ApCameraActivity::class.java)
        startActivity(cameraIntent)
    }
}
