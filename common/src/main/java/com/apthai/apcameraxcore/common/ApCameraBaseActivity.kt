package com.apthai.apcameraxcore.common

import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.apthai.apcameraxcore.common.model.ApPhoto

abstract class ApCameraBaseActivity<V : ApCameraBaseViewModel<*>> : AppCompatActivity() {

    abstract fun tag(): String
    abstract fun setUpView()
    abstract fun initial()

    private var fetchMediaCollection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }
    private var fetchMediaProjection: Array<String> = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.SIZE,
        MediaStore.Images.Media.DATE_ADDED,
        MediaStore.Images.Media.DATE_MODIFIED,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Images.Media.BUCKET_ID,
        MediaStore.Images.Media.MIME_TYPE
    )
    private var fetchMediaSelection: String = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?"
    private var fetchMediaSelectionArgs: Array<String> = arrayOf(
        "ApCamera-Image"
    )
    private var fetchMediaSortOrder: String = "${MediaStore.Images.Media.DATE_ADDED} ASC"

    fun fetchCurrentPhotos() : MutableList<ApPhoto> {
        val currentPhotoList : MutableList<ApPhoto> = ArrayList()
        val mediaQuery = contentResolver.query(
            fetchMediaCollection,
            fetchMediaProjection,
            fetchMediaSelection,
            fetchMediaSelectionArgs,
            fetchMediaSortOrder
        )

        mediaQuery?.use { cursor ->
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

            while (cursor.moveToNext()){
                val fileId = cursor.getLong(fetchIdColumn)
                val fileName = cursor.getString(fetchFileNameColumn)
                val fileSize = cursor.getInt(fetchFileSizeColumn)
                val fileDateAdded = cursor.getLong(fetchDateAddedColumn)
                val fileDateModified = cursor.getLong(fetchDateModifiedColumn)
                val fileBucketId = cursor.getLong(fetchBucketIdColumn)
                val fileBucketName = cursor.getString(fetchBucketNameColumn)
                val fileMimeType = cursor.getString(fetchMimeTypeColumn)

                val contentUri : Uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fileId)

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
        return currentPhotoList
    }
}
