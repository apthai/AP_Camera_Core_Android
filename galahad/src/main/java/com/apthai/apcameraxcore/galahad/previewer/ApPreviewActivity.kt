package com.apthai.apcameraxcore.galahad.previewer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.apthai.apcameraxcore.common.ApCameraBaseActivity
import com.apthai.apcameraxcore.galahad.databinding.ActivityGalahadPreviewBinding
import com.apthai.apcameraxcore.galahad.previewer.contract.ApPreviewResultContract
import com.bumptech.glide.Glide

class ApPreviewActivity : ApCameraBaseActivity<ApPreviewViewModel>(), ApPreviewNavigator {

    override fun tag(): String = ApPreviewActivity::class.java.simpleName

    private var activityGalahadPreviewBinding : ActivityGalahadPreviewBinding?=null
    private val binding get() = activityGalahadPreviewBinding

    private var viewModel : ApPreviewViewModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityGalahadPreviewBinding = ActivityGalahadPreviewBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        viewModel = ViewModelProvider.NewInstanceFactory().create(ApPreviewViewModel::class.java)
        viewModel?.setNavigator(this)

        if (savedInstanceState == null){
            setUpView()
            initial()
        }
    }

    override fun setUpView() {

        setSupportActionBar(binding?.apPreviewToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val rawPhotoUri = getPhotoUriPayload()
        rawPhotoUri?.let { photoUriStr ->
            val photoUri = Uri.parse(photoUriStr)
            binding?.apPreviewPhotoView?.let { photoView ->
                Glide.with(this).load(photoUri).into(photoView)
            } ?: kotlin.run {
                Toast.makeText(
                    this,
                    "Photo view not available, Pls try again !",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } ?: kotlin.run {
            Toast.makeText(
                this,
                "Photo file or Photo uri is null or empty or error, Pls tyr again !",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun initial() {}

    override fun getPhotoUriPayload(): String? = intent.getStringExtra(ApPreviewResultContract.AP_PREVIEW_CONTRACT_URI_PAYLOAD_CONST)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                val photoUri = getPhotoUriPayload()
                photoUri?.let { uriStr->
                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra(ApPreviewResultContract.AP_PREVIEW_CONTRACT_URI_PAYLOAD_CONST, uriStr)
                    })
                }
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}