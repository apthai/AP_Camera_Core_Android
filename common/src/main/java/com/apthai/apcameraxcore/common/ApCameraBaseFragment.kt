package com.apthai.apcameraxcore.common

import androidx.fragment.app.Fragment

abstract class ApCameraBaseFragment<V : ApCameraBaseViewModel<*>> : Fragment() {

    abstract fun tag(): String
    abstract fun setUpView()
    abstract fun initial()
}
