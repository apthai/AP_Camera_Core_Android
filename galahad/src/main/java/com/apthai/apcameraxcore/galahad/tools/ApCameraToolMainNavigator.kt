package com.apthai.apcameraxcore.galahad.tools

interface ApCameraToolMainNavigator {
    fun getPathFileFromIntent(): String
    fun getIsFlipFromLensFrontIntent():Boolean
    fun setConvertImagePathFileToBitmap(fullPathFile: String, isFlipFromLensFront: Boolean)
}