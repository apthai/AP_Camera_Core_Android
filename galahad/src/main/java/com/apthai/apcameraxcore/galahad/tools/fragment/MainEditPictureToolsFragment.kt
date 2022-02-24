package com.apthai.apcameraxcore.galahad.tools.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.apthai.apcameraxcore.common.ApCameraBaseFragment
import com.apthai.apcameraxcore.galahad.databinding.FragmentMainEditPictureToolsBinding

class MainEditPictureToolsFragment : ApCameraBaseFragment<MainEditPictureToolsViewModel>(),
    MainEditPictureToolsNavigator {

    companion object {
        private const val IMAGE_PATH_FILE_KEY_INTENT: String = "image_path_file"
        fun getInstance(imagePathFile: String): MainEditPictureToolsFragment {
            val fragment = MainEditPictureToolsFragment()
            val arg = Bundle()
            arg.putString(IMAGE_PATH_FILE_KEY_INTENT, imagePathFile)
            fragment.arguments = arg
            return fragment
        }
    }

    private lateinit var viewBinding: FragmentMainEditPictureToolsBinding
    private lateinit var viewModel: MainEditPictureToolsViewModel


    override fun tag(): String = MainEditPictureToolsFragment::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        this.viewBinding = FragmentMainEditPictureToolsBinding.inflate(inflater, container, false)
        return this.viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.viewModel =
            ViewModelProvider.NewInstanceFactory().create(MainEditPictureToolsViewModel::class.java)
        this.viewModel.setNavigator(this)
        this.setUpView()
        this.initial()
    }

    override fun setUpView() {

    }

    override fun initial() {

    }
}