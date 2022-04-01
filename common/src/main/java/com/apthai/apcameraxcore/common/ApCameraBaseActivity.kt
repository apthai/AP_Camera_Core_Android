package com.apthai.apcameraxcore.common

import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity

abstract class ApCameraBaseActivity<V : ApCameraBaseViewModel<*>> : AppCompatActivity() {

    abstract fun tag(): String
    abstract fun setUpView()
    abstract fun initial()

    var fetchMediaCollection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }
    var fetchMediaProjection: Array<String> = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.SIZE,
        MediaStore.Images.Media.DATE_ADDED,
        MediaStore.Images.Media.DATE_MODIFIED,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Images.Media.BUCKET_ID,
        MediaStore.Images.Media.MIME_TYPE
    )
    var fetchMediaSelection: String = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?"
    var fetchMediaSelectionArgs: Array<String> = arrayOf(
        "ApCamera-Image"
    )
    var fetchMediaSortOrder: String = "${MediaStore.Images.Media.DATE_ADDED} ASC"
}
