package com.apthai.apcameraxcore.galahad.tools

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ApCameraToolSharedViewModel : ViewModel() {
    var customImageBitmap: MutableLiveData<Bitmap> = MutableLiveData()
}