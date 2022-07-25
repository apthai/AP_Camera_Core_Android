package com.apthai.apcameraxcore.galahad.editor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.apthai.apcameraxcore.common.ApCameraBaseFragment
import com.apthai.apcameraxcore.galahad.R
import com.apthai.apcameraxcore.galahad.databinding.FragmentApEditorMenuToolsBinding
import com.apthai.apcameraxcore.galahad.editor.fragment.adapter.EditingToolsAdapter
import com.apthai.apcameraxcore.galahad.editor.tools.ToolType
import com.apthai.apcameraxcore.galahad.util.ApCameraUtil

class ApEditorToolsFragment :
    ApCameraBaseFragment<ApEditorToolsViewModel>(),
    ApEditorToolsNavigator,
    EditingToolsAdapter.OnItemSelected {

    override fun tag(): String = ApEditorToolsFragment::class.java.simpleName

    companion object {

        fun getInstance(fromScreenTag: String): ApEditorToolsFragment = ApEditorToolsFragment().apply {
            arguments = Bundle().apply {
                putString(ApCameraUtil.Generic.AP_CAMERA_DEFAULT_FROM_SCREEN_TAG, fromScreenTag)
            }
        }
    }

    private var fragmentApEditorToolsBinding: FragmentApEditorMenuToolsBinding? = null
    private val binding get() = fragmentApEditorToolsBinding

    private var apEditorToolsViewModel: ApEditorToolsViewModel? = null

    private var editingToolsAdapter: EditingToolsAdapter? = null

    private var listener: ApEditorToolsFragment.OnApEditorToolsEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ap_editor_menu_tools, container, false)
        fragmentApEditorToolsBinding = FragmentApEditorMenuToolsBinding.bind(view)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apEditorToolsViewModel = ViewModelProvider.NewInstanceFactory().create(ApEditorToolsViewModel::class.java)
        apEditorToolsViewModel?.setNavigator(this)

        if (savedInstanceState == null) {
            setUpView()
            initial()
        }
    }

    override fun setUpView() {
        editingToolsAdapter = EditingToolsAdapter(requireActivity(), this)

        val llmTools = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        binding?.apEditorConstraintToolsView?.layoutManager = llmTools
        binding?.apEditorConstraintToolsView?.adapter = editingToolsAdapter
    }

    override fun initial() {}

    fun setOnApEditorToolsEventListener(listener: OnApEditorToolsEventListener) {
        this.listener = listener
    }

    override fun onToolSelected(toolType: ToolType?) {
        listener?.onSelectedToolType(toolType)
    }

    interface OnApEditorToolsEventListener {

        fun onSelectedToolType(toolType: ToolType?)
    }
}
