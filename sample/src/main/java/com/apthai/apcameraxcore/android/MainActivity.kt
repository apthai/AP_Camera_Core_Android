package com.apthai.apcameraxcore.android

import android.Manifest
import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.apthai.apcameraxcore.android.adapter.SimpleImageViewPagerAdapter
import com.apthai.apcameraxcore.android.databinding.ActivityMainBinding
import com.apthai.apcameraxcore.common.ApCameraBaseActivity
import com.apthai.apcameraxcore.galahad.ApCameraActivity
import com.apthai.apcameraxcore.galahad.contract.ApCameraContract
import com.apthai.apcameraxcore.galahad.util.ApCameraConst

class MainActivity : ApCameraBaseActivity<MainViewModel>(), MainNavigator, View.OnClickListener {

    companion object {
//        private const val REQUEST_CODE_PERMISSIONS = 112
        private val REQUIRED_PERMISSIONS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                arrayOf(
                    CAMERA,
                    READ_MEDIA_IMAGES,
                    READ_MEDIA_VIDEO,
                    READ_MEDIA_VISUAL_USER_SELECTED
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    CAMERA,
                    READ_MEDIA_IMAGES,
                    READ_MEDIA_VIDEO
                )
            } else {
                arrayOf(
                    CAMERA,
                    READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
    }

    override fun tag(): String = MainActivity::class.java.simpleName

    private var activityMainBinding: ActivityMainBinding? = null
    private val binding get() = activityMainBinding

    private var mainViewModel: MainViewModel? = null
    private var simpleImageUriAdapter: SimpleImageViewPagerAdapter? = null
    private var _apCameraContract: ActivityResultLauncher<Bundle>? = null
    private var _chooseGalleryActResultContract: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        mainViewModel = ViewModelProvider.NewInstanceFactory().create(MainViewModel::class.java)
        mainViewModel?.setNavigator(this)
        this.initChooseGalleryActResultContract()
        this.initApCameraContract()
        if (savedInstanceState == null) {
            setUpView()
            initial()
        }
    }

    override fun setUpView() {}

    override fun initial() {
        if (isCameraPermissionsGranted()) {
            binding?.mainLaunchCameraButton?.setOnClickListener(this)
            this.setUpViewPagerAdapter(arrayListOf())
        } else {
            requestPermissions.launch(
                REQUIRED_PERMISSIONS
            )
        }
    }

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            // Handle permission requests results
            // See the permission example in the Android platform samples: https://github.com/android/platform-samples
            Log.e("TAG", "requestPermissions: ${results.map { it }}")
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                (
                        ContextCompat.checkSelfPermission(
                            this,
                            READ_MEDIA_IMAGES
                        ) == PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(
                                    this,
                                    READ_MEDIA_VIDEO
                                ) == PERMISSION_GRANTED
                        ) && ContextCompat.checkSelfPermission(
                    this,
                    CAMERA
                ) == PERMISSION_GRANTED
            ) {
                // Full access on Android 13 (API level 33) or higher
                binding?.mainLaunchCameraButton?.setOnClickListener(this)
                this.setUpViewPagerAdapter(arrayListOf())
            } else if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                ContextCompat.checkSelfPermission(
                    this,
                    READ_MEDIA_VISUAL_USER_SELECTED
                ) == PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    CAMERA
                ) == PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    CAMERA
                ) == PERMISSION_GRANTED
            ) {
                // Partial access on Android 14 (API level 34) or higher
                binding?.mainLaunchCameraButton?.setOnClickListener(this)
                this.setUpViewPagerAdapter(arrayListOf())
            } else if (ContextCompat.checkSelfPermission(
                    this,
                    READ_EXTERNAL_STORAGE
                ) == PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    CAMERA
                ) == PERMISSION_GRANTED
            ) {
                // Full access up to Android 12 (API level 32)
                binding?.mainLaunchCameraButton?.setOnClickListener(this)
                this.setUpViewPagerAdapter(arrayListOf())
            } else {
                // Access denied
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    @SuppressLint("CheckResult")
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.main_launch_camera_button -> {
                MaterialDialog(this).show {
                    title(text = "Select Mode")
                    listItems(
                        items = arrayListOf(
                            "Just Capture and Preview",
                            "Preview with editor for folder ap camera",
                            "Multiple Shot and Preview",
                            "Only edit your photo and see the preview (Choose your photo from gallery)"
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
                                _apCameraContract?.launch(cameraBundle)
                            }

                            1 -> {
                                dialog.dismiss()
                                val cameraBundle = Bundle().apply {
                                    putInt(
                                        ApCameraConst.ApCameraMode.AP_CAMERA_CONST_MODE_NAME,
                                        ApCameraConst.ApCameraMode.AP_CAMERA_VAL_VIEW_GALLERY_MODE
                                    )
                                }
                                _apCameraContract?.launch(cameraBundle)
                            }

                            2 -> {
                                dialog.dismiss()
                                val cameraBundle = Bundle().apply {
                                    putInt(
                                        ApCameraConst.ApCameraMode.AP_CAMERA_CONST_MODE_NAME,
                                        ApCameraConst.ApCameraMode.AP_CAMERA_VAL_MULTIPLE_SHOT_PREVIEW_MODE
                                    )
                                }
                                _apCameraContract?.launch(cameraBundle)
                            }

                            else -> {
                                dialog.dismiss()
                                _chooseGalleryActResultContract?.launch("image/*")
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

    override fun setUpViewPagerAdapter(imageUriList: ArrayList<String>) {
        this.simpleImageUriAdapter = SimpleImageViewPagerAdapter(imageUriList)
        this.binding?.mainViewPager?.apply {
            adapter = simpleImageUriAdapter
        }
    }

    private fun isCameraPermissionsGranted(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            (
                    ActivityCompat.checkSelfPermission(
                        this,
                        CAMERA
                    ) == PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.RECORD_AUDIO
                            ) == PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                this,
                                READ_MEDIA_IMAGES
                            ) == PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                this,
                                READ_MEDIA_VIDEO
                            ) == PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) == PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                this,
                                READ_MEDIA_VISUAL_USER_SELECTED
                            ) == PERMISSION_GRANTED
                    )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            (
                    ActivityCompat.checkSelfPermission(
                        this,
                        CAMERA
                    ) == PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.RECORD_AUDIO
                            ) == PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                this,
                                READ_MEDIA_IMAGES
                            ) == PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                this,
                                READ_MEDIA_VIDEO
                            ) == PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) == PERMISSION_GRANTED
                    )
        } else {
            (
                    ActivityCompat.checkSelfPermission(
                        this,
                        CAMERA
                    ) == PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                this,
                                READ_EXTERNAL_STORAGE
                            ) == PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PERMISSION_GRANTED
                    )
        }

    private fun initChooseGalleryActResultContract() {
        this._chooseGalleryActResultContract =
            registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { rsList ->
                rsList.let { uriList ->
                    if (uriList.isNotEmpty()) {
                        val cameraBundle = Bundle().apply {
                            putInt(
                                ApCameraConst.ApCameraMode.AP_CAMERA_CONST_MODE_NAME,
                                ApCameraConst.ApCameraMode.AP_CAMERA_VAL_ONLY_EDIT_PHOTO_MODE
                            )
                            putStringArrayList(
                                ApCameraConst.ApCameraPayload.AP_CAMERA_INPUT_IMAGE_PATH_LIST_CONST_NAME,
                                rsList.map { it.toString() } as ArrayList<String>
                            )
                        }
                        this._apCameraContract?.launch(cameraBundle)
                    }
                }
            }
    }

    private fun initApCameraContract() {
        this._apCameraContract =
            registerForActivityResult(ApCameraContract(tag())) { fallbackImageUriStrList ->
                if (fallbackImageUriStrList.isEmpty()) return@registerForActivityResult
                this.setUpViewPagerAdapter(fallbackImageUriStrList)
            }
    }
}
