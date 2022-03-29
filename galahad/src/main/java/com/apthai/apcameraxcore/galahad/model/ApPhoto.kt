package com.apthai.apcameraxcore.galahad.model

import android.net.Uri

data class ApPhoto(
    val uri : Uri,
    val name : String,
    val size : Int,
    val createdAt : Long,
    val bucketId : Long,
    val bucketName : String
)