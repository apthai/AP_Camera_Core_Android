package com.apthai.apcameraxcore.galahad.previewer

import com.apthai.apcameraxcore.common.model.ApPhoto

interface ApTransitionPreviewNavigator {

    fun getSelectedApPhotoPayload(): ApPhoto?

    fun getPreviewImageUriStrPayload(): String?
    fun getFromScreenTagPayload(): String?
}
