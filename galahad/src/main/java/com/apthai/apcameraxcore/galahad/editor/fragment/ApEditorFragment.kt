package com.apthai.apcameraxcore.galahad.editor.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.apthai.apcameraxcore.common.ApCameraBaseFragment
import com.apthai.apcameraxcore.common.model.ApPhoto
import com.apthai.apcameraxcore.galahad.R
import com.apthai.apcameraxcore.galahad.databinding.FragmentApEditorBinding

class ApEditorFragment : ApCameraBaseFragment<ApEditorFragmentViewModel>(),
    ApEditorFragmentNavigator {

    override fun tag(): String = ApEditorFragment::class.java.simpleName

    companion object{

        private const val AP_PAGER_EDITOR_PAYLOAD = "ap_pager_editor_payload_apc"

        fun getInstance(apPhoto : ApPhoto) : ApEditorFragment = ApEditorFragment().apply {
            arguments = Bundle().apply {
                putParcelable(AP_PAGER_EDITOR_PAYLOAD, apPhoto)
            }
        }
    }

    private var fragmentApEditor: FragmentApEditorBinding? = null
    private val binding get() = fragmentApEditor

    private var viewModel: ApEditorFragmentViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ap_editor, container, false)
        fragmentApEditor = FragmentApEditorBinding.bind(view)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel =
            ViewModelProvider.NewInstanceFactory().create(ApEditorFragmentViewModel::class.java)
        viewModel?.setNavigator(this)

        if (savedInstanceState == null) {
            setUpView()
            initial()
        }
    }

    override fun setUpView() {}

    override fun initial() {}
}