package com.apthai.apcameraxcore.galahad.previewer

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.apthai.apcameraxcore.common.ApCameraBaseActivity
import com.apthai.apcameraxcore.common.model.ApPhoto
import com.apthai.apcameraxcore.galahad.R
import com.apthai.apcameraxcore.galahad.databinding.ActivityGalahadPagerPreviewBinding
import com.apthai.apcameraxcore.galahad.editor.contract.ApEditorResultContract
import com.apthai.apcameraxcore.galahad.previewer.adapter.ApPagerPhotoViewAdapter
import com.apthai.apcameraxcore.galahad.util.ApCameraUtil

class ApPagerPreviewActivity :
    ApCameraBaseActivity<ApPagerPreviewViewModel>(),
    ApPagerPreviewNavigator {

    override fun tag(): String = ApPagerPreviewActivity::class.java.simpleName

    companion object {

        fun getInstance(context: Context, fromScreenTag: String): Intent =
            Intent(context, ApPagerPreviewActivity::class.java).apply {
                putExtra(ApCameraUtil.Generic.AP_CAMERA_DEFAULT_FROM_SCREEN_TAG, fromScreenTag)
            }
    }

    private var activityGalahadPagerPreviewBinding: ActivityGalahadPagerPreviewBinding? = null
    private val binding get() = activityGalahadPagerPreviewBinding

    private var apPagerPreviewViewModel: ApPagerPreviewViewModel? = null
    private var apPagerPhotoViewAdapter: ApPagerPhotoViewAdapter? = null
    private var currentPhotoList: MutableList<ApPhoto> = ArrayList()

    private var currentSelectedPhotoUri: Uri? = null

    private val apEditorActivityContract =
        registerForActivityResult(ApEditorResultContract()) { editedPhotoUri ->
        }

    private val viewPagerListener: ViewPager2.OnPageChangeCallback =
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val currentSelectedApPhoto = currentPhotoList[position]
                currentSelectedPhotoUri = currentSelectedApPhoto.uriPath
                supportActionBar?.title = currentSelectedApPhoto.fileName
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityGalahadPagerPreviewBinding =
            ActivityGalahadPagerPreviewBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        apPagerPreviewViewModel =
            ViewModelProvider.NewInstanceFactory().create(ApPagerPreviewViewModel::class.java)
        apPagerPreviewViewModel?.setNavigator(this)

        if (savedInstanceState == null) {
            setUpView()
            initial()
        }
    }

    override fun setUpView() {
        setSupportActionBar(binding?.apPagerPreviewToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        apPagerPhotoViewAdapter = ApPagerPhotoViewAdapter(this)
        binding?.apPagerPreviewViewpager?.apply {
            adapter = apPagerPhotoViewAdapter
            registerOnPageChangeCallback(viewPagerListener)
        }

        fetchCurrentPhotos()
    }

    override fun initial() {}

    override fun fetchCurrentPhotos() {
        val mediaCursor = contentResolver.query(
            fetchMediaCollection,
            fetchMediaProjection,
            fetchMediaSelection,
            fetchMediaSelectionArgs,
            fetchMediaSortOrder
        )

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

            currentPhotoList = ArrayList()

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

            apPagerPhotoViewAdapter?.updateData(apPhotoList = currentPhotoList)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.ap_preview_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.ap_preview_action_editor -> {
                currentSelectedPhotoUri?.let { photoUri ->
                    val photoUriRaw = photoUri.toString()
                    apEditorActivityContract.launch(photoUriRaw)
                } ?: kotlin.run {
                    Toast.makeText(this, "Current selected Photo unavailable", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
