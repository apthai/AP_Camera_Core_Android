package com.apthai.apcameraxcore.common

import androidx.appcompat.app.AppCompatActivity

abstract class ApCameraBaseActivity<V : ApCameraBaseViewModel<*>> : AppCompatActivity() {

    abstract fun tag(): String
    abstract fun setUpView()
    abstract fun initial()
}
