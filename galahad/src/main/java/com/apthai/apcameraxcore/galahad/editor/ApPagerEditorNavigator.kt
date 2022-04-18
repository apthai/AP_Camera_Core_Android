package com.apthai.apcameraxcore.galahad.editor

import com.apthai.apcameraxcore.common.model.ApPhoto

interface ApPagerEditorNavigator {

    fun getApPhotoListPayload() : MutableList<ApPhoto>
}