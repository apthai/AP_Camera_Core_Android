package com.apthai.apcameraxcore.galahad.util

import java.text.SimpleDateFormat
import java.util.Locale

object ApCameraUtil {

    private const val AP_CAMERA_DEFAULT_FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    const val AP_CAMERA_DEFAULT_FOLDER = "Pictures/ApCamera-Image"
    const val AP_CAMERA_DEFAULT_MIME_TYPE = "image/jpeg"

    fun getFileName(): String =
        SimpleDateFormat(AP_CAMERA_DEFAULT_FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
}
