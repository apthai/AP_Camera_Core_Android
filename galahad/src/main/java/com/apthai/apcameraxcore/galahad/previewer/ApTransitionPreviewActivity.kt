package com.apthai.apcameraxcore.galahad.previewer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import com.apthai.apcameraxcore.common.ApCameraBaseActivity
import com.apthai.apcameraxcore.common.model.ApPhoto
import com.apthai.apcameraxcore.galahad.R
import com.apthai.apcameraxcore.galahad.databinding.ActivityGalahadPreviewTransitionBinding
import com.apthai.apcameraxcore.galahad.editor.contract.ApEditorResultContract
import com.apthai.apcameraxcore.galahad.previewer.contract.ApTransitionPreviewResultContract
import com.bumptech.glide.Glide

class ApTransitionPreviewActivity : ApCameraBaseActivity<ApTransitionPreviewViewModel>(),
    ApTransitionPreviewNavigator {

    override fun tag(): String = ApTransitionPreviewActivity::class.java.simpleName

    companion object {

        fun getInstance(context: Context, selectedApPhoto: ApPhoto): Intent =
            Intent(context, ApTransitionPreviewActivity::class.java).apply {
                putExtra(
                    ApTransitionPreviewResultContract.AP_TRANSITION_PREVIEW_PAYLOAD,
                    selectedApPhoto
                )
            }
    }

    private var activityGalahadPreviewTransitionBinding: ActivityGalahadPreviewTransitionBinding? =
        null
    private val binding get() = activityGalahadPreviewTransitionBinding

    private var viewModel: ApTransitionPreviewViewModel? = null

    private val apEditorActivityContract =
        registerForActivityResult(ApEditorResultContract()) { editedPhotoUri ->
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityGalahadPreviewTransitionBinding =
            ActivityGalahadPreviewTransitionBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        viewModel =
            ViewModelProvider.NewInstanceFactory().create(ApTransitionPreviewViewModel::class.java)
        viewModel?.setNavigator(this)

        if (savedInstanceState == null) {
            setUpView()
            initial()
        }
    }

    override fun setUpView() {
        setSupportActionBar(binding?.apTransitionPreviewToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getSelectedApPhotoPayload()?.let { selectedApPhoto ->
            supportActionBar?.title = selectedApPhoto.fileName
            binding?.apPreviewTransitionView?.let { photoPreview ->
                Glide.with(this).load(selectedApPhoto.uriPath).into(photoPreview)
            }
        }
    }

    override fun initial() {}

    override fun getSelectedApPhotoPayload(): ApPhoto? =
        intent?.getParcelableExtra<ApPhoto>(ApTransitionPreviewResultContract.AP_TRANSITION_PREVIEW_PAYLOAD)

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.ap_preview_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.ap_preview_action_editor -> {
                getSelectedApPhotoPayload()?.let { selectedApPhoto ->
                    apEditorActivityContract.launch(selectedApPhoto.uriPath.toString())
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
