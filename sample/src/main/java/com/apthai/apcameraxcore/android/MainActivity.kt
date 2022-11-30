package com.apthai.apcameraxcore.android

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
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
        private const val REQUEST_CODE_PERMISSIONS = 112
        private val REQUIRED_PERMISSIONS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            } else {
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
    }

    override fun tag(): String = MainActivity::class.java.simpleName

    private var activityMainBinding: ActivityMainBinding? = null
    private val binding get() = activityMainBinding

    private var mainViewModel: MainViewModel? = null
    private var simpleImageUriAdapter: SimpleImageViewPagerAdapter? = null

    private val apCameraContract =
        registerForActivityResult(ApCameraContract(tag())) { fallbackImageUriStrList ->
            if (fallbackImageUriStrList.isEmpty()) return@registerForActivityResult
            this.setUpViewPagerAdapter(fallbackImageUriStrList)
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
        if (isCameraPermissionsGranted()) {
            binding?.mainLaunchCameraButton?.setOnClickListener(this)
            this.setUpViewPagerAdapter(arrayListOf())
        } else {
            requestPermissions(
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionOK: MutableList<Int> = mutableListOf()
        for (item in grantResults) {
            if (item != PackageManager.PERMISSION_GRANTED) {
                permissionOK.add(item)
            }
        }
        if (permissionOK.isEmpty()) {
            binding?.mainLaunchCameraButton?.setOnClickListener(this)
            this.setUpViewPagerAdapter(arrayListOf())
        } else {
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
                            2 -> {
                                dialog.dismiss()
                                val cameraBundle = Bundle().apply {
                                    putInt(
                                        ApCameraConst.ApCameraMode.AP_CAMERA_CONST_MODE_NAME,
                                        ApCameraConst.ApCameraMode.AP_CAMERA_VAL_MULTIPLE_SHOT_PREVIEW_MODE
                                    )
                                }
                                apCameraContract.launch(cameraBundle)
                            }
                            else -> {
                                dialog.dismiss()
                                chooseGalleryActResultContract.launch("image/*")
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            (
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                )
        } else {
            (
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                )
        }

    private val chooseGalleryActResultContract =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { rsList ->
            rsList?.let { uriList ->
                if (uriList.isNotEmpty()) {
                    if (rsList.isEmpty()) return@registerForActivityResult
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
                    apCameraContract.launch(cameraBundle)
                }
            }
        }
}
