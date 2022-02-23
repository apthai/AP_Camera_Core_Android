package com.apthai.apcameraxcore.common.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import java.nio.ByteBuffer

class ImageUtil {

    fun convertImagePoxyToBitmap(image: Image, flipNeeded: Boolean): Bitmap? {
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
        //หมุนรูป
        // matrix.postRotate(this.getRotation(rotationDegree))
        //กลับรูป
        if (flipNeeded) matrix.preScale(1.0f, -1.0f) else matrix.preScale(-1.0f, 1.0f)
        val width = image.width.coerceAtMost(bitmap.width)
        val height = image.height.coerceAtMost(bitmap.height)
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }

}