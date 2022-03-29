package com.apthai.apcameraxcore.galahad.previewer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.apthai.apcameraxcore.common.ApCameraBaseActivity
import com.apthai.apcameraxcore.galahad.databinding.ActivityGalahadPreviewTransitionBinding
import com.apthai.apcameraxcore.galahad.previewer.contract.ApTransitionPreviewResultContract
import com.bumptech.glide.Glide

class ApTransitionPreviewActivity : ApCameraBaseActivity<ApTransitionPreviewViewModel>(), ApTransitionPreviewNavigator {

    override fun tag(): String =ApTransitionPreviewActivity::class.java.simpleName

    companion object{

        fun getInstance(context : Context, imagePath : String) : Intent = Intent(context, ApTransitionPreviewActivity::class.java).apply {
            putExtra(ApTransitionPreviewResultContract.AP_TRANSITION_PREVIEW_PAYLOAD, imagePath)
        }
    }

    private var activityGalahadPreviewTransitionBinding : ActivityGalahadPreviewTransitionBinding?=null
    private val binding get() = activityGalahadPreviewTransitionBinding

    private var viewModel : ApTransitionPreviewViewModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityGalahadPreviewTransitionBinding = ActivityGalahadPreviewTransitionBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        viewModel = ViewModelProvider.NewInstanceFactory().create(ApTransitionPreviewViewModel::class.java)
        viewModel?.setNavigator(this)

        if (savedInstanceState == null){
            setUpView()
            initial()
        }
    }

    override fun setUpView() {

        getImagePathPayload()?.let { imagePath->
            binding?.apPreviewTransitionView?.let { photoPreview->
                val uriImagePath = Uri.parse(imagePath)
                Glide.with(this).load(uriImagePath).into(photoPreview)
            }
        }
    }

    override fun initial() {}

    override fun getImagePathPayload(): String? = intent?.getStringExtra(ApTransitionPreviewResultContract.AP_TRANSITION_PREVIEW_PAYLOAD)
}