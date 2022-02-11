package com.apthai.apcameraxcore.galahad

interface ApCameraNavigator {

    fun isCameraPermissionsGranted() : Boolean

    fun startCamera()
    fun takePhoto()

    fun flipCameraFacing()
    fun toggleCameraFlashMode()
}