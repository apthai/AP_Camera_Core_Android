package com.apthai.apcameraxcore.galahad

import android.app.Application

/**
 * Created by Burhanuddin Rashid on 1/23/2018.
 */
class ApCameraApp : Application() {
    override fun onCreate() {
        super.onCreate()
        apCameraApp = this
    }

    companion object {
        var apCameraApp: ApCameraApp? = null
            private set
        private val TAG = ApCameraApp::class.java.simpleName
    }
}