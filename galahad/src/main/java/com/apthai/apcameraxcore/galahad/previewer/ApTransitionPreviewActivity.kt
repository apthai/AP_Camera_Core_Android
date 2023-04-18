package com.apthai.apcameraxcore.galahad.previewer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModelProvider
import com.apthai.apcameraxcore.common.ApCameraBaseActivity
import com.apthai.apcameraxcore.common.model.ApPhoto
import com.apthai.apcameraxcore.galahad.ApCameraActivity
import com.apthai.apcameraxcore.galahad.R
import com.apthai.apcameraxcore.galahad.databinding.ActivityGalahadPreviewTransitionBinding
import com.apthai.apcameraxcore.galahad.editor.contract.ApEditorResultContract
import com.apthai.apcameraxcore.galahad.previewer.contract.ApTransitionPreviewResultContract
import com.apthai.apcameraxcore.galahad.util.ApCameraConst
import com.apthai.apcameraxcore.galahad.util.ApCameraUtil
import com.bumptech.glide.Glide

class ApTransitionPreviewActivity :
    ApCameraBaseActivity<ApTransitionPreviewViewModel>(),
    ApTransitionPreviewNavigator,
    View.OnClickListener {

    override fun tag(): String = ApTransitionPreviewActivity::class.java.simpleName

    companion object {

        fun getInstance(context: Context, selectedApPhoto: ApPhoto, fromScreenTag: String): Intent =
            Intent(context, ApTransitionPreviewActivity::class.java).apply {
                putExtra(
                    ApTransitionPreviewResultContract.AP_TRANSITION_PREVIEW_PAYLOAD,
                    selectedApPhoto
                )
                putExtra(ApCameraUtil.Generic.AP_CAMERA_DEFAULT_FROM_SCREEN_TAG, fromScreenTag)
            }

        fun getInstance(
            context: Context,
            previewPhotoUriStr: String,
            fromScreenTag: String
        ): Intent =
            Intent(context, ApTransitionPreviewActivity::class.java).apply {
                putExtra(
                    ApCameraConst.ApPreviewPayload.AP_PREVIEW_URI_STR_PAYLOAD,
                    previewPhotoUriStr
                )
                putExtra(ApCameraUtil.Generic.AP_CAMERA_DEFAULT_FROM_SCREEN_TAG, fromScreenTag)
            }
    }

    private var activityGalahadPreviewTransitionBinding: ActivityGalahadPreviewTransitionBinding? =
        null
    private val binding get() = activityGalahadPreviewTransitionBinding

    private var viewModel: ApTransitionPreviewViewModel? = null

    private var _apEditorActivityContract: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityGalahadPreviewTransitionBinding =
            ActivityGalahadPreviewTransitionBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        viewModel =
            ViewModelProvider.NewInstanceFactory().create(ApTransitionPreviewViewModel::class.java)
        viewModel?.setNavigator(this)

        this.initApEditorActivityContract()
        if (savedInstanceState == null) {
            setUpView()
            initial()
        }
    }

    override fun setUpView() {
        setSupportActionBar(binding?.apTransitionPreviewToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        when (getFromScreenTagPayload()) {
            ApCameraActivity::class.java.simpleName -> {
                binding?.apPreviewTransitionConsoleView?.visibility = View.VISIBLE
                val previewImageUriStr = getPreviewImageUriStrPayload()
                previewImageUriStr?.let { previewUriStr ->
                    val imageUri = Uri.parse(previewUriStr)
                    binding?.apPreviewTransitionView?.let { photoPreview ->
                        Glide.with(this).load(imageUri).into(photoPreview)
                    }
                }
            }

            else -> {
                binding?.apPreviewTransitionConsoleView?.visibility = View.GONE
                getSelectedApPhotoPayload()?.let { selectedApPhoto ->
                    supportActionBar?.title = selectedApPhoto.fileName
                    binding?.apPreviewTransitionView?.let { photoPreview ->
                        Glide.with(this).load(selectedApPhoto.uriPath).into(photoPreview)
                    }
                }
            }
        }
    }

    override fun initial() {
        when (getFromScreenTagPayload()) {
            ApCameraActivity::class.java.simpleName -> {
                binding?.apPreviewTransitionConsoleCancelButton?.setOnClickListener(this)
                binding?.apPreviewTransitionConsoleOkButton?.setOnClickListener(this)
            }

            else -> {
                binding?.apPreviewTransitionConsoleCancelButton?.setOnClickListener(null)
                binding?.apPreviewTransitionConsoleOkButton?.setOnClickListener(null)
            }
        }
    }

    override fun getSelectedApPhotoPayload(): ApPhoto? =
        intent?.parcelable(ApTransitionPreviewResultContract.AP_TRANSITION_PREVIEW_PAYLOAD)

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (getFromScreenTagPayload() != ApCameraActivity::class.java.simpleName) {
            menuInflater.inflate(R.menu.ap_preview_menu, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.ap_preview_action_editor -> {
                getSelectedApPhotoPayload()?.let { selectedApPhoto ->
                    this._apEditorActivityContract?.launch(selectedApPhoto.uriPath.toString())
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getPreviewImageUriStrPayload(): String? =
        intent.getStringExtra(ApCameraConst.ApPreviewPayload.AP_PREVIEW_URI_STR_PAYLOAD)

    override fun getFromScreenTagPayload(): String? =
        intent.getStringExtra(ApCameraUtil.Generic.AP_CAMERA_DEFAULT_FROM_SCREEN_TAG)

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ap_preview_transition_console_ok_button -> {
                val fallbackIntent = Intent().apply {
                    putExtra(
                        ApCameraConst.ApPreviewPayload.AP_PREVIEW_URI_STR_PAYLOAD,
                        getPreviewImageUriStrPayload()
                    )
                }
                setResult(Activity.RESULT_OK, fallbackIntent)
                finish()
            }

            else -> {
                finish()
            }
        }
    }

    private fun initApEditorActivityContract() {
        this._apEditorActivityContract =
            registerForActivityResult(ApEditorResultContract()) { editedPhotoUri ->
                editedPhotoUri?.let { photoUriStr ->
                    val uri = Uri.parse(photoUriStr)
                    binding?.apPreviewTransitionView?.let { photoPreview ->
                        Glide.with(this).load(uri).into(photoPreview)
                    }
                }
            }
    }
}
