package com.apthai.apcameraxcore.galahad.previewer

import android.R
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.apthai.apcameraxcore.common.ApCameraBaseActivity
import com.apthai.apcameraxcore.common.model.ApPhoto
import com.apthai.apcameraxcore.galahad.databinding.ActivityGalahadPreviewBinding
import com.apthai.apcameraxcore.galahad.previewer.adapter.ApPhotoViewListAdapter
import com.apthai.apcameraxcore.galahad.previewer.contract.ApPreviewResultContract

class ApPreviewActivity :
    ApCameraBaseActivity<ApPreviewViewModel>(),
    ApPreviewNavigator,
    ApPhotoViewListAdapter.OnPhotoViewItemEventListener {

    override fun tag(): String = ApPreviewActivity::class.java.simpleName

    companion object {

        fun getInstance(context: Context): Intent = Intent(context, ApPreviewActivity::class.java)
    }

    private var activityGalahadPreviewBinding: ActivityGalahadPreviewBinding? = null
    private val binding get() = activityGalahadPreviewBinding

    private var viewModel: ApPreviewViewModel? = null

    private var apPhotoViewListAdapter: ApPhotoViewListAdapter? = null

    private var currentPhotoList: MutableList<ApPhoto> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityGalahadPreviewBinding = ActivityGalahadPreviewBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        viewModel = ViewModelProvider.NewInstanceFactory().create(ApPreviewViewModel::class.java)
        viewModel?.setNavigator(this)

        if (savedInstanceState == null) {
            setUpView()
            initial()
        }
    }

    override fun setUpView() {

        setSupportActionBar(binding?.apPreviewToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        apPhotoViewListAdapter = ApPhotoViewListAdapter(this)
        binding?.apPreviewRecyclerView?.apply {
            layoutManager = GridLayoutManager(this@ApPreviewActivity, 2)
            adapter = apPhotoViewListAdapter
        }
    }

    override fun initial() {
        apPhotoViewListAdapter?.setOnPhotoViewItemEventListener(this)

        viewModel?.shareCurrentPhotos?.observe(this) { apPhotos ->
            apPhotoViewListAdapter?.updateData(apPhotos.asReversed())
        }

        fetchCurrentPhotos()
    }

    override fun getPhotoUriPayload(): String? =
        intent.getStringExtra(ApPreviewResultContract.AP_PREVIEW_CONTRACT_URI_PAYLOAD_CONST)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val photoUri = getPhotoUriPayload()
                photoUri?.let { uriStr ->
                    setResult(
                        Activity.RESULT_OK,
                        Intent().apply {
                            putExtra(
                                ApPreviewResultContract.AP_PREVIEW_CONTRACT_URI_PAYLOAD_CONST,
                                uriStr
                            )
                        }
                    )
                }
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPhotoClick(view: View, apPhoto: ApPhoto, position: Int) {
        transition(view, apPhoto)
    }

    private fun transition(view: View, apPhoto: ApPhoto) {
        val intent = ApTransitionPreviewActivity.getInstance(this, apPhoto)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            view,
            "Transition Test"
        )
        startActivity(intent, options.toBundle())
    }

    override fun fetchCurrentPhotos() {
        val mediaCursor = contentResolver.query(
            fetchMediaCollection,
            fetchMediaProjection,
            fetchMediaSelection,
            fetchMediaSelectionArgs,
            fetchMediaSortOrder
        )

        currentPhotoList = ArrayList()

        mediaCursor?.use { cursor ->
            val fetchIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val fetchFileNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val fetchFileSizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val fetchDateAddedColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val fetchDateModifiedColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val fetchBucketIdColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val fetchBucketNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val fetchMimeTypeColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)

            while (cursor.moveToNext()) {
                val fileId = cursor.getLong(fetchIdColumn)
                val fileName = cursor.getString(fetchFileNameColumn)
                val fileSize = cursor.getInt(fetchFileSizeColumn)
                val fileDateAdded = cursor.getLong(fetchDateAddedColumn)
                val fileDateModified = cursor.getLong(fetchDateModifiedColumn)
                val fileBucketId = cursor.getLong(fetchBucketIdColumn)
                val fileBucketName = cursor.getString(fetchBucketNameColumn)
                val fileMimeType = cursor.getString(fetchMimeTypeColumn)

                val contentUri: Uri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fileId)

                currentPhotoList += ApPhoto(
                    id = fileId,
                    uriPath = contentUri,
                    fileName = fileName,
                    fileSize = fileSize,
                    createdAt = fileDateAdded,
                    modifiedAt = fileDateModified,
                    folderId = fileBucketId,
                    folderName = fileBucketName,
                    mimeType = fileMimeType
                )
            }
        }

        viewModel?.setSharedCurrentPhotos(currentPhotoList)
    }

    override fun onResume() {
        super.onResume()
        binding?.apPreviewRecyclerView?.let {
            fetchCurrentPhotos()
        }
    }
}
