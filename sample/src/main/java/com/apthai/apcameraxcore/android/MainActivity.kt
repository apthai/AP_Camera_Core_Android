package com.apthai.apcameraxcore.android

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.apthai.apcameraxcore.android.databinding.ActivityMainBinding
import com.apthai.apcameraxcore.common.ApCameraBaseActivity
import com.apthai.apcameraxcore.galahad.ApCameraActivity
import com.apthai.apcameraxcore.galahad.editor.ApEditorActivity

class MainActivity : ApCameraBaseActivity<MainViewModel>(), MainNavigator, View.OnClickListener {

    override fun tag(): String = MainActivity::class.java.simpleName

    companion object{

    }

    private var activityMainBinding : ActivityMainBinding?=null
    private val binding get() = activityMainBinding

    private var mainViewModel : MainViewModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        mainViewModel = ViewModelProvider.NewInstanceFactory().create(MainViewModel::class.java)
        mainViewModel?.setNavigator(this)

        if (savedInstanceState==null){
            setUpView()
            initial()
        }
    }

    override fun setUpView() {}

    override fun initial() {
        binding?.mainLaunchCameraButton?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.main_launch_camera_button -> {
                launchCameraScreen()
            }
        }
    }

    override fun launchCameraScreen() {
        val cameraIntent = Intent(this, ApCameraActivity::class.java)
        startActivity(cameraIntent)
    }

    override fun launchEditorScreen() {
        val editorIntent = ApEditorActivity.getInstance(this, tag())
        startActivity(editorIntent)
    }
}
