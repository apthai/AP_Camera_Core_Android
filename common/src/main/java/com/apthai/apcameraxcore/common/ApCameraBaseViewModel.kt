package com.apthai.apcameraxcore.common

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import java.lang.ref.WeakReference

abstract class ApCameraBaseViewModel<N> : ViewModel(), CoroutineScope {

    abstract fun tag(): String
    private var navigator: WeakReference<N>? = null

    fun setNavigator(navigator: N) {
        this.navigator = WeakReference(navigator)
    }

    fun getNavigator(): WeakReference<N>? = navigator
}
