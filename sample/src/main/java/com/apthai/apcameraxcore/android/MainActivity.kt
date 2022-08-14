package com.apthai.apcameraxcore.android

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

class MainActivity : ApCameraBaseActivity<MainViewModel>(), MainNavigator, View.OnClickListener {

    override fun tag(): String = MainActivity::class.java.simpleName

    companion object {
    }

    private var activityMainBinding: ActivityMainBinding? = null
    private val binding get() = activityMainBinding

    private var mainViewModel: MainViewModel? = null

    private val apCameraContract =
        registerForActivityResult(ApCameraContract(tag())) { fallbackImageUriStr ->
            if (fallbackImageUriStr.isNullOrEmpty()) return@registerForActivityResult
            Toast.makeText(this, "Retrieve PhotoUri Raw $fallbackImageUriStr", Toast.LENGTH_SHORT)
                .show()
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

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.main_launch_camera_button -> {
                MaterialDialog(this).show {
                    title(text = "Select once")
                    listItems(
                        items = arrayListOf(
                            "Just Capture and Preview",
                            "Preview with editor"
                        )
                    ) { dialog, index, _ ->
                        when (index) {
                            0 -> {
                                dialog.dismiss()
                                val cameraBundle = Bundle().apply {
                                    putBoolean(ApCameraConst.ApCameraPayload.AP_CAMERA_IS_ONLY_CALL_CAMERA, true)
                                }
                                apCameraContract.launch(cameraBundle)
                            }
                            else -> {
                                dialog.dismiss()
                                launchCameraScreen()
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
