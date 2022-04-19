package com.apthai.apcameraxcore.galahad.editor

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.lifecycle.ViewModelProvider
import com.apthai.apcameraxcore.common.ApCameraBaseActivity
import com.apthai.apcameraxcore.common.model.ApPhoto
import com.apthai.apcameraxcore.galahad.databinding.ActivityGalahadPagerEditorBinding
import com.apthai.apcameraxcore.galahad.editor.adapter.PagerEditorAdapter
import com.apthai.apcameraxcore.galahad.editor.adapter.PhotoEditorAdapter
import com.apthai.apcameraxcore.galahad.util.ApCameraUtil

class ApPagerEditorActivity : ApCameraBaseActivity<ApPagerEditorViewModel>(), ApPagerEditorNavigator {

    override fun tag(): String = ApPagerEditorActivity::class.java.simpleName

    companion object{

        fun getInstance(context : Context, fromScreenTag : String) : Intent = Intent(context, ApPagerEditorActivity::class.java).apply {
            putExtra(ApCameraUtil.GENERIC.AP_CAMERA_GENERIC_SCREEN_TAG, fromScreenTag)
        }
    }

    private var activityGalahadPagerEditorBinding : ActivityGalahadPagerEditorBinding?=null
    private val binding get() = activityGalahadPagerEditorBinding
    private var apPagerEditorViewModel : ApPagerEditorViewModel?=null

    //private var editorFragmentPager : PagerEditorAdapter?=null
    private var photoEditorAdapter : PhotoEditorAdapter? = null

    private var apPhotoList : MutableList<ApPhoto> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityGalahadPagerEditorBinding = ActivityGalahadPagerEditorBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        apPagerEditorViewModel = ViewModelProvider.NewInstanceFactory().create(ApPagerEditorViewModel::class.java)
        apPagerEditorViewModel?.setNavigator(this)

        if (savedInstanceState == null){
            setUpView()
            initial()
        }
    }

    override fun setUpView() {

        photoEditorAdapter = PhotoEditorAdapter(this)
        binding?.apCameraPagerEditorViewpager?.apply {
            adapter = photoEditorAdapter
        }

        fetchCurrentPhotoList()
    }

    override fun initial() {}

    override fun fetchCurrentPhotoList() {
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

                apPhotoList += ApPhoto(
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

        photoEditorAdapter?.updateData(apPhotoList)
    }
}