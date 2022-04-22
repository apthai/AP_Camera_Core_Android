package com.apthai.apcameraxcore.galahad.editor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.apthai.apcameraxcore.common.ApCameraBaseActivity
import com.apthai.apcameraxcore.common.ApCameraBaseFragment

class ApEditorToolsFragment : ApCameraBaseFragment<ApEditorToolsViewModel>(), ApEditorToolsNavigator {

    override fun tag(): String = ApEditorToolsFragment::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun setUpView() {
        TODO("Not yet implemented")
    }

    override fun initial() {
        TODO("Not yet implemented")
    }
}