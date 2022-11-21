package com.apthai.apcameraxcore.common

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
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

    inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelable(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelable(key) as? T
    }

    inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? =
        when {
            Build.VERSION.SDK_INT >= 33 -> getParcelableArrayList(key, T::class.java)
            else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
        }

    inline fun <reified T : Parcelable> Intent.parcelableArrayList(key: String): ArrayList<T>? =
        when {
            Build.VERSION.SDK_INT >= 33 -> getParcelableArrayListExtra(key, T::class.java)
            else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
        }
}
