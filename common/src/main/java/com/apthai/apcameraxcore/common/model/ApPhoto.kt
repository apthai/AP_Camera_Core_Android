package com.apthai.apcameraxcore.common.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ApPhoto(
    val id: Long,
    val uriPath: Uri,
    val fileName: String,
    val createdAt: Long,
    val modifiedAt: Long,
    val fileSize: Int,
    val folderId: Long,
    val folderName: String,
    val mimeType: String
) : Parcelable
