package com.apthai.apcameraxcore.common.model

import android.net.Uri

data class ApPhoto(
    val id : Long,
    val uriPath : Uri,
    val fileName : String,
    val createdAt : Long,
    val modifiedAt : Long,
    val fileSize : Int,
    val folderId : Long,
    val folderName : String,
    val mimeType : String
)