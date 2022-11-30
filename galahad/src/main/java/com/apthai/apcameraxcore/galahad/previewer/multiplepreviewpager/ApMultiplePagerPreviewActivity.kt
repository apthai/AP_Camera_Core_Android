package com.apthai.apcameraxcore.galahad.previewer.multiplepreviewpager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.apthai.apcameraxcore.common.ApCameraBaseActivity
import com.apthai.apcameraxcore.common.model.ApImageUriAdapter
import com.apthai.apcameraxcore.galahad.R
import com.apthai.apcameraxcore.galahad.databinding.ActivityApMultiplePagerPreviewBinding
import com.apthai.apcameraxcore.galahad.editor.contract.ApEditorResultContract
import com.apthai.apcameraxcore.galahad.previewer.adapter.apmultiplepager.ApMultiplePagerPreviewAdapter
import com.google.android.material.snackbar.Snackbar

class ApMultiplePagerPreviewActivity :
    ApCameraBaseActivity<ApMultiplePagerPreviewViewModel>(),
    ApMultiplePagerPreviewNavigator {

    companion object {
        private const val CONST_IMAGE_LIST_NAME: String = "image_uri_list"
        const val CONST_IMAGE_LIST_FOR_RESULT_NAME: String = "image_uri_result_list"
        fun getIntent(context: Context, imageUriList: ArrayList<String>): Intent {
            return Intent(context, ApMultiplePagerPreviewActivity::class.java).apply {
                putStringArrayListExtra(CONST_IMAGE_LIST_NAME, imageUriList)
            }
        }
    }

    private lateinit var viewModel: ApMultiplePagerPreviewViewModel
    private lateinit var binding: ActivityApMultiplePagerPreviewBinding
    private var apMultiplePagerPreviewAdapter: ApMultiplePagerPreviewAdapter? = null
    private val _imageUriAdapterList: ArrayList<ApImageUriAdapter> = arrayListOf()
    private val _imageUriList: ArrayList<String> = arrayListOf()
    private var _currentPositionViewPagerSelected: Int = 0
    private var _currentSelectedPhotoUri: String = ""

    override fun tag(): String = ApMultiplePagerPreviewActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityApMultiplePagerPreviewBinding.inflate(layoutInflater)
        this.setContentView(this.binding.root)

        this.viewModel = ViewModelProvider.NewInstanceFactory().create(
            ApMultiplePagerPreviewViewModel::class.java
        )
        this.viewModel.setNavigator(this)
        this.setUpView()
        this.initial()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.ap_preview_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.ap_preview_action_editor -> {
                if (this._currentSelectedPhotoUri.isNotEmpty()) {
                    this.apEditorActivityContract.launch(this._currentSelectedPhotoUri)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setUpView() {
        setSupportActionBar(binding.actApMultiplePagerPreviewToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        this.binding.actApMultiplePagerPreviewItemChecked.setOnClickListener {
            this.onClickMenuItemChecked()
        }
        this.binding.actApMultiplePagerPreviewBtnConfirm.setOnClickListener {
            this.onClickMenuConfirm()
        }
    }

    override fun initial() {
        this._imageUriList.addAll(this.getIntentImageUriList())
        this._imageUriAdapterList.addAll(this.imageUriListToImageUriAdapter(this._imageUriList))

        this.setUpViewPagerAdapter(this._imageUriAdapterList)
    }

    override fun getIntentImageUriList(): ArrayList<String> {
        return intent.extras?.getStringArrayList(CONST_IMAGE_LIST_NAME) ?: arrayListOf()
    }

    override fun setUpViewPagerAdapter(imageUriAdapterList: ArrayList<ApImageUriAdapter>) {
        this.apMultiplePagerPreviewAdapter = ApMultiplePagerPreviewAdapter(imageUriAdapterList)
        this.binding.actApMultiplePagerPreviewViewPager.apply {
            adapter = apMultiplePagerPreviewAdapter
            registerOnPageChangeCallback(onViewPagerPageChangeCallback())
        }
    }

    override fun onViewPagerPageChangeCallback(): ViewPager2.OnPageChangeCallback =
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                _currentPositionViewPagerSelected = position
                apMultiplePagerPreviewAdapter?.let { adt ->
                    adt.getItemByPosition(position)?.let { imageAdapter ->
                        _currentSelectedPhotoUri = imageAdapter.uriStr
                    }
                }
                checkUpdateCheckedItemCheckedView()
            }
        }

    override fun imageUriListToImageUriAdapter(imageUriList: ArrayList<String>): ArrayList<ApImageUriAdapter> {
        val imageUriAdapter: ArrayList<ApImageUriAdapter> = arrayListOf()
        imageUriList.forEach {
            imageUriAdapter.add(
                ApImageUriAdapter(
                    uriStr = it
                )
            )
        }
        return imageUriAdapter
    }

    override fun onClickMenuItemChecked() {
        this.apMultiplePagerPreviewAdapter?.setToggleCheckedByPosition(this._currentPositionViewPagerSelected)
        this.checkUpdateCheckedItemCheckedView()
        this.setCountSelectedImage(this.getItemImageSelectedList().size)
    }

    override fun onClickMenuConfirm() {
        val imageUriList = this.viewModel.imageAdapterToImageListStr(
            this.apMultiplePagerPreviewAdapter?.getItemSelected() ?: mutableListOf()
        )
        if (imageUriList.isNotEmpty()) {
            val imageIntentResult =
                intent.putStringArrayListExtra(
                    CONST_IMAGE_LIST_FOR_RESULT_NAME,
                    imageUriList
                )
            setResult(RESULT_OK, imageIntentResult)
            finish()
        } else {
            this.showSnackBar("Please select image")
        }
    }

    override fun checkUpdateCheckedItemCheckedView() {
        val itemImage =
            this.apMultiplePagerPreviewAdapter?.getItemByPosition(_currentPositionViewPagerSelected)
        itemImage?.let {
            this.binding.actApMultiplePagerPreviewItemChecked.isChecked = it.isChecked
        }
    }

    override fun setCountSelectedImage(amountSelected: Int) {
        this.binding.actApMultiplePagerPreviewTextCount.text = amountSelected.toString()
    }

    override fun getItemImageSelectedList(): ArrayList<ApImageUriAdapter> =
        this.apMultiplePagerPreviewAdapter?.getItemSelected() ?: arrayListOf()

    override fun updateNewImage(uriStr: String, currentPosition: Int) {
        this.apMultiplePagerPreviewAdapter?.updatePathUriByPosition(
            uriStr,
            currentPosition
        )
        this._imageUriList[this._currentPositionViewPagerSelected] = uriStr
        this._imageUriList.add(this._currentPositionViewPagerSelected, uriStr)
        this.apMultiplePagerPreviewAdapter?.getItemByPosition(this._currentPositionViewPagerSelected)
            ?.let {
                this._imageUriAdapterList[this._currentPositionViewPagerSelected] = it
            }
    }

    private val apEditorActivityContract =
        registerForActivityResult(ApEditorResultContract()) { editedPhotoUri ->
            editedPhotoUri?.let { uriStr ->
                this._currentSelectedPhotoUri = uriStr
                this.updateNewImage(uriStr, this._currentPositionViewPagerSelected)
                this.viewModel.removeFileFromUri(uriStr)
            }
        }

    override fun showSnackBar(message: String) {
        val snb = Snackbar.make(this.binding.root, message, Snackbar.LENGTH_SHORT)
        snb.setAction(R.string.ap_camera_text_close) {
            snb.dismiss()
        }
        snb.show()
    }
}
