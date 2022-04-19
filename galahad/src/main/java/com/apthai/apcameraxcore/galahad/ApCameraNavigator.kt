package com.apthai.apcameraxcore.galahad

import android.content.ContentValues
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview

interface ApCameraNavigator {

    fun isCameraPermissionsGranted(): Boolean

    fun initializePreviewSurface(): Preview
    fun initializeImageAnalysis(): ImageAnalysis
    fun initializeImageCapture(): ImageCapture

    fun getContentValue(outputFileName: String): ContentValues
    fun getOutputFileOption(contentValue: ContentValues): ImageCapture.OutputFileOptions

    fun startCamera()
    fun bindCamera()
    fun takePhoto()

    fun flipCameraFacing()
    fun toggleCameraFlashMode()
    fun toggleAspectRatio()

    fun initialAutoFocusAndPinchToZoom()
    fun animateAutofocusEvent(positionX: Float, positionY: Float)

    fun playShutterSound()

    fun takePhotoWithOutSave()

    fun launchPreviewPhotoActivity()
    fun loadDing(isLoading: Boolean)
    fun saveImageToLocalSuccess(currentPathFile: String)

    fun launchPagerPhotoEditorActivity()

    fun launchEditorPhotoActivity()
}
