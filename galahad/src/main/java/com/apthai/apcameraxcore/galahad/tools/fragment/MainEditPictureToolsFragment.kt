package com.apthai.apcameraxcore.galahad.tools.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.apthai.apcameraxcore.common.ApCameraBaseFragment
import com.apthai.apcameraxcore.galahad.R
import com.apthai.apcameraxcore.galahad.databinding.FragmentMainEditPictureToolsBinding
import com.bumptech.glide.Glide

class MainEditPictureToolsFragment : ApCameraBaseFragment<MainEditPictureToolsViewModel>(),
    MainEditPictureToolsNavigator, View.OnClickListener {

    companion object {
        const val TAG_FRAGMENT_NAME = "MainEditPictureToolsFragment"
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

        Glide.with(this).load(this.getPathFileFromArg())
            .into(this.viewBinding.frmMainEditPictureToolsImageView)
    }

    override fun initial() {
        this.viewBinding.frmMainEditPictureToolsIcBack.setOnClickListener(this)
        this.viewBinding.frmMainEditPictureToolsIcCrop.setOnClickListener(this)
        this.viewBinding.frmMainEditPictureToolsIcWrite.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.frm_main_edit_picture_tools_ic_back ->{
                activity?.finish()
            }
            R.id.frm_main_edit_picture_tools_ic_crop ->{
                Toast.makeText(activity, "Crop is coming soon", Toast.LENGTH_SHORT).show()
            }
            R.id.frm_main_edit_picture_tools_ic_write ->{
                Toast.makeText(activity, "Write or draw is coming soon", Toast.LENGTH_SHORT).show()
            }
            else ->{

            }
        }
    }

    override fun getPathFileFromArg(): String {
        return this.arguments?.getString(IMAGE_PATH_FILE_KEY_INTENT, "") ?: ""
    }
}