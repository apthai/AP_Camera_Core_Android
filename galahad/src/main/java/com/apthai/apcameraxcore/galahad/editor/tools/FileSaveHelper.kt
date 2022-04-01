package com.apthai.apcameraxcore.galahad.editor.tools

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.apthai.apcameraxcore.galahad.util.ApCameraUtil
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.Throws

/**
 * General contract of this class is to
 * create a file on a device.
 *
 * How to Use it-
 * Call [FileSaveHelper.createFile]
 * if file is created you would receive it's file path and Uri
 * and after you are done with File call [FileSaveHelper.notifyThatFileIsNowPubliclyAvailable]
 *
 * Remember! in order to shutdown executor call [FileSaveHelper.addObserver] or
 * create object with the [FileSaveHelper]
 */
class FileSaveHelper(private val mContentResolver: ContentResolver) : LifecycleObserver {
    private val executor: ExecutorService? = Executors.newSingleThreadExecutor()
    private val fileCreatedResult: MutableLiveData<FileMeta> = MutableLiveData()
    private var resultListener: OnFileCreateResult? = null
    private val observer = Observer { fileMeta: FileMeta ->
        resultListener?.onFileCreateResult(
            fileMeta.isCreated,
            fileMeta.filePath,
            fileMeta.error,
            fileMeta.uri
        )
    }

    constructor(activity: AppCompatActivity) : this(activity.contentResolver) {
        addObserver(activity)
    }

    private fun addObserver(lifecycleOwner: LifecycleOwner) {
        fileCreatedResult.observe(lifecycleOwner, observer)
        lifecycleOwner.lifecycle.addObserver(this)
    }

    /**
     * The effects of this method are
     * 1- insert new Image File data in MediaStore.Images column
     * 2- create File on Disk.
     *
     * @param fileNameToSave fileName
     * @param listener       result listener
     */
    fun createFile(fileNameToSave: String, listener: OnFileCreateResult?) {
        resultListener = listener
        executor?.submit {
            var cursor: Cursor? = null
            try {
                // Build the edited image URI for the MediaStore
                val newImageDetails = ContentValues()
                val imageCollection = buildUriCollection(newImageDetails)
                val imageProjections: Array<String> = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA
                )
                val editedImageUri =
                    getEditedImageUri(fileNameToSave, newImageDetails, imageCollection)
                editedImageUri?.let { editedUri ->
                    cursor = mContentResolver.query(
                        editedUri,
                        imageProjections,
                        null,
                        null,
                        null
                    )
                    val idColumn = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val indexColumn = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    cursor?.moveToFirst()
                    idColumn?.let { idc->
                        val id = cursor?.getLong(idc)
                        val uri = ContentUris.withAppendedId(editedUri, id!!)
                        // Post the file created result with the resolved image file path
                        //updateResult(true, x.toString(), null, editedImageUri, newImageDetails)
                    }
                    indexColumn?.let { cIndex->
                        val filePath = cursor?.getString(cIndex)
                        updateResult(true, filePath, null, editedImageUri, newImageDetails)
                    }
                    /*val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    cursor?.moveToFirst()
                    columnIndex?.let { cIndex ->
                        val filePath = cursor?.getString(cIndex)
                        // Post the file created result with the resolved image file path
                        updateResult(true, filePath, null, editedImageUri, newImageDetails)
                    } ?: kotlin.run {
                        updateResult(false, null, "File path from cursor is null", null, null)
                    }*/
                } ?: kotlin.run {
                    updateResult(false, null, "Edited image uri is null", null, null)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                updateResult(false, null, ex.message, null, null)
            } finally {
                cursor?.close()
            }
        }
    }

    @Throws(IOException::class)
    private fun getEditedImageUri(
        fileNameToSave: String,
        newImageDetails: ContentValues,
        imageCollection: Uri
    ): Uri? {
        newImageDetails.put(MediaStore.Images.Media.DISPLAY_NAME, fileNameToSave)
        newImageDetails.put(MediaStore.Images.Media.MIME_TYPE, ApCameraUtil.AP_CAMERA_DEFAULT_MIME_TYPE)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            newImageDetails.put(MediaStore.Images.Media.RELATIVE_PATH, ApCameraUtil.AP_CAMERA_DEFAULT_FOLDER)
        }
        val editedImageUri = mContentResolver.insert(imageCollection, newImageDetails)
        editedImageUri?.let { imageUri ->
            val outputStream = mContentResolver.openOutputStream(imageUri)
            outputStream?.close()
            return editedImageUri
        }
        return null
    }

    @SuppressLint("InlinedApi")
    private fun buildUriCollection(newImageDetails: ContentValues): Uri {
        val imageCollection: Uri
        if (isSdkHigherThan28()) {
            imageCollection = MediaStore.Images.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
            newImageDetails.put(MediaStore.Images.Media.IS_PENDING, 1)
        } else {
            imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        return imageCollection
    }

    @SuppressLint("InlinedApi")
    fun notifyThatFileIsNowPubliclyAvailable(contentResolver: ContentResolver) {
        if (isSdkHigherThan28()) {
            executor?.submit {
                val value = fileCreatedResult.value
                value?.let { vl ->
                    vl.imageDetails?.clear()
                    vl.imageDetails?.put(MediaStore.Images.Media.IS_PENDING, 0)
                    val vlUri = vl.uri
                    vlUri?.let {
                        contentResolver.update(it, value.imageDetails, null, null)
                    }
                }
            }
        }
    }

    private class FileMeta(
        var isCreated: Boolean,
        var filePath: String?,
        var uri: Uri?,
        var error: String?,
        var imageDetails: ContentValues?
    )

    interface OnFileCreateResult {
        /**
         * @param created  whether file creation is success or failure
         * @param filePath filepath on disk. null in case of failure
         * @param error    in case file creation is failed . it would represent the cause
         * @param Uri      Uri to the newly created file. null in case of failure
         */
        fun onFileCreateResult(created: Boolean, filePath: String?, error: String?, uri: Uri?)
    }

    private fun updateResult(
        result: Boolean,
        filePath: String?,
        error: String?,
        uri: Uri?,
        newImageDetails: ContentValues?
    ) {
        fileCreatedResult.postValue(FileMeta(result, filePath, uri, error, newImageDetails))
    }

    companion object {
        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
        fun isSdkHigherThan28(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        }
    }
}
