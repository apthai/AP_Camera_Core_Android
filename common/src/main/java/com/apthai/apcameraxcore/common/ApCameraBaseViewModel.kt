package com.apthai.apcameraxcore.common

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.apthai.apcameraxcore.common.model.ApPhoto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

abstract class ApCameraBaseViewModel<N> : ViewModel(), CoroutineScope {

    abstract fun tag(): String
    private var navigator: WeakReference<N>? = null

    fun setNavigator(navigator: N) {
        this.navigator = WeakReference(navigator)
    }

    fun getNavigator(): WeakReference<N>? = navigator

    private var mutableCurrentPhotos = MutableLiveData<MutableList<ApPhoto>>()
    var shareCurrentPhotos : LiveData<MutableList<ApPhoto>> = mutableCurrentPhotos

    fun setSharedCurrentPhotos(photoList : MutableList<ApPhoto>){
        mutableCurrentPhotos.value = photoList
    }
}
