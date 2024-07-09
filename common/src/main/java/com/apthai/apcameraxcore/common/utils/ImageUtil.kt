package com.apthai.apcameraxcore.common.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

object ImageUtil {

    const val BASE_IMAGE_FOLDER: String = "ApCamera-Image"

    fun convertImagePoxyToBitmap(image: Image, flipNeeded: Boolean): Bitmap {
        val byteBuffer: ByteBuffer = image.planes[0].buffer
        byteBuffer.rewind()
        val bytes = ByteArray(byteBuffer.capacity())
        byteBuffer.get(bytes)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val decodeOptions = BitmapFactory.Options()
        decodeOptions.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
        val matrix = Matrix()
//            if (flipNeeded) {
//                matrix.postRotate(this.getRotation(rotationDegree))
//                if (flipNeeded) matrix.preScale(1.0f, -1.0f)
//            }
        // หมุนรูป
        // matrix.postRotate(this.getRotation(rotationDegree))
        // กลับรูป
        if (flipNeeded) matrix.preScale(1.0f, -1.0f) else matrix.preScale(-1.0f, 1.0f)
        val width = image.width.coerceAtMost(bitmap.width)
        val height = image.height.coerceAtMost(bitmap.height)
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }

    fun bitmapFromFilePath(fullPathFile: String, flipNeeded: Boolean = false): Bitmap {
        val mFile = File(fullPathFile)
        val option = BitmapFactory.Options()
        option.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bitmap = BitmapFactory.decodeFile(mFile.absolutePath, option)
        val matrix = Matrix()
        if (flipNeeded) {
            matrix.preScale(1.0f, -1.0f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun saveImageBitmap(absolutePath: String, fileName: String, imageBitmap: Bitmap) {
        if (isExternalStorageWritable()) {
            val rootDir = "$absolutePath/$BASE_IMAGE_FOLDER"
            val myFileDir = File(rootDir)
            if (!myFileDir.exists()) {
                myFileDir.mkdirs()
            }
            val myFile = File(myFileDir, fileName)
            if (myFile.exists()) myFile.delete()
            try {
                val outputStm = FileOutputStream(myFile)
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStm)
                outputStm.flush()
                outputStm.close()
            } catch (e: Exception) {
                throw Exception(e)
            }
        } else {
            throw Exception("Your External storage does not exists.")
        }
    }

    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }
}
