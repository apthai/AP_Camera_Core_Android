package com.apthai.apcameraxcore.galahad.editor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.apthai.apcameraxcore.common.ApCameraBaseActivity
import com.apthai.apcameraxcore.common.model.ApPhoto
import com.apthai.apcameraxcore.galahad.databinding.ActivityGalahadPagerEditorBinding
import com.apthai.apcameraxcore.galahad.editor.adapter.PagerEditorAdapter

class ApPagerEditorActivity : ApCameraBaseActivity<ApPagerEditorViewModel>(), ApPagerEditorNavigator {

    override fun tag(): String = ApPagerEditorActivity::class.java.simpleName

    companion object{

        private const val AP_EDITOR_PHOTOS_LIST_PAYLOAD = "ap_editor_photos_list_payload"

        fun getInstance(context : Context, apPhotoList : MutableList<ApPhoto>) : Intent = Intent(context, ApPagerEditorActivity::class.java).apply {
            putParcelableArrayListExtra(AP_EDITOR_PHOTOS_LIST_PAYLOAD, ArrayList(apPhotoList))
        }
    }

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

    override fun getApPhotoListPayload(): MutableList<ApPhoto> = intent.getParcelableArrayListExtra<ApPhoto>(
        AP_EDITOR_PHOTOS_LIST_PAYLOAD)?.toMutableList() ?: ArrayList()
}