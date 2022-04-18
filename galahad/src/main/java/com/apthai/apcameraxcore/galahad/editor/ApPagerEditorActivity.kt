package com.apthai.apcameraxcore.galahad.editor

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.apthai.apcameraxcore.common.ApCameraBaseActivity
import com.apthai.apcameraxcore.galahad.databinding.ActivityGalahadPagerEditorBinding
import com.apthai.apcameraxcore.galahad.editor.adapter.PagerEditorAdapter

class ApPagerEditorActivity : ApCameraBaseActivity<ApPagerEditorViewModel>(), ApPagerEditorNavigator {

    override fun tag(): String = ApPagerEditorActivity::class.java.simpleName

    private var activityGalahadPagerEditorBinding : ActivityGalahadPagerEditorBinding?=null
    private val binding get() = activityGalahadPagerEditorBinding
    private var apPagerEditorViewModel : ApPagerEditorViewModel?=null

    private var editorFragmentPager : PagerEditorAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityGalahadPagerEditorBinding = ActivityGalahadPagerEditorBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        apPagerEditorViewModel = ViewModelProvider.NewInstanceFactory().create(ApPagerEditorViewModel::class.java)
        apPagerEditorViewModel?.setNavigator(this)

        if (savedInstanceState == null){
            setUpView()
            initial()
        }
    }

    override fun setUpView() {}

    override fun initial() {}
}