package com.apthai.apcameraxcore.galahad.previewer

import android.R
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.apthai.apcameraxcore.common.ApCameraBaseActivity
import com.apthai.apcameraxcore.galahad.databinding.ActivityGalahadPreviewBinding
import com.apthai.apcameraxcore.galahad.model.ApPhoto
import com.apthai.apcameraxcore.galahad.previewer.adapter.ApPhotoViewListAdapter
import com.apthai.apcameraxcore.galahad.previewer.contract.ApPreviewResultContract
import com.apthai.apcameraxcore.galahad.previewer.contract.ApTransitionPreviewResultContract
import com.apthai.apcameraxcore.galahad.tools.ApCameraToolMainActivityResultContract


class ApPreviewActivity : ApCameraBaseActivity<ApPreviewViewModel>(), ApPreviewNavigator,
    ApPhotoViewListAdapter.OnPhotoViewItemEventListener {

    override fun tag(): String = ApPreviewActivity::class.java.simpleName

    companion object {

        fun getInstance(context: Context): Intent = Intent(context, ApPreviewActivity::class.java)
    }

    private var activityGalahadPreviewBinding: ActivityGalahadPreviewBinding? = null
    private val binding get() = activityGalahadPreviewBinding

    private var viewModel: ApPreviewViewModel? = null

    private var currentPhotoList: MutableList<ApPhoto> = ArrayList()
    private var mediaCollection: Uri? = null
    private var mediaProjection: Array<String>? = null
    private var mediaSelection: String? = null
    private var mediaSelectionArgs: Array<String>? = null
    private var mediaSortOrder: String? = null

    private var apPhotoViewListAdapter: ApPhotoViewListAdapter? = null

    private val apTransitionPreviewContract =
        registerForActivityResult(ApTransitionPreviewResultContract()) {}

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

        mediaCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        mediaProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_ID
        )

        mediaSelection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?"
        mediaSelectionArgs = arrayOf(
            "ApCamera-Image"
        )

        mediaSortOrder = "${MediaStore.Images.Media.DISPLAY_NAME} ASC"

        val mediaQuery = contentResolver.query(
            checkNotNull(mediaCollection),
            mediaProjection,
            mediaSelection,
            mediaSelectionArgs,
            mediaSortOrder
        )
        mediaQuery?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val bucketNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val size = cursor.getInt(sizeColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val bucketId = cursor.getLong(bucketIdColumn)
                val bucketName = cursor.getString(bucketNameColumn)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                currentPhotoList += ApPhoto(
                    uri = contentUri,
                    name = name,
                    size = size,
                    createdAt = dateAdded,
                    bucketId = bucketId,
                    bucketName = bucketName
                )
            }
            apPhotoViewListAdapter?.updateData(currentPhotoList)
        }
    }

    override fun initial() {
        apPhotoViewListAdapter?.setOnPhotoViewItemEventListener(this)
    }

    override fun getPhotoUriPayload(): String? =
        intent.getStringExtra(ApPreviewResultContract.AP_PREVIEW_CONTRACT_URI_PAYLOAD_CONST)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val photoUri = getPhotoUriPayload()
                photoUri?.let { uriStr ->
                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra(
                            ApPreviewResultContract.AP_PREVIEW_CONTRACT_URI_PAYLOAD_CONST,
                            uriStr
                        )
                    })
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
        val intent = ApTransitionPreviewActivity.getInstance(this, apPhoto.uri.toString())
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            view,
            "Transition Test"
        )
        startActivity(intent, options.toBundle())
    }
}