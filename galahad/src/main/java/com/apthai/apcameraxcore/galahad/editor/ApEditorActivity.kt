package com.apthai.apcameraxcore.galahad.editor

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.apthai.apcameraxcore.common.ApCameraBaseActivity
import com.apthai.apcameraxcore.galahad.R
import com.apthai.apcameraxcore.galahad.databinding.ActivityGalahadEditorBinding
import com.apthai.apcameraxcore.galahad.editor.filters.FilterListener
import com.apthai.apcameraxcore.galahad.editor.filters.FilterViewAdapter
import com.apthai.apcameraxcore.galahad.editor.fragment.ApEditorStickerEmojiSelectorFragment
import com.apthai.apcameraxcore.galahad.editor.fragment.PropertiesBSFragment
import com.apthai.apcameraxcore.galahad.editor.fragment.ApEditorShapeSelectorFragment
import com.apthai.apcameraxcore.galahad.editor.fragment.StickerBSFragment
import com.apthai.apcameraxcore.galahad.editor.fragment.ApEditorAddTextEditorFragment
import com.apthai.apcameraxcore.galahad.editor.tools.FileSaveHelper
import com.apthai.apcameraxcore.galahad.util.ApCameraUtil
import com.bumptech.glide.Glide
import com.burhanrashid52.photoediting.tools.EditingToolsAdapter
import com.burhanrashid52.photoediting.tools.ToolType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ja.burhanrashid52.photoeditor.*
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder
import ja.burhanrashid52.photoeditor.shape.ShapeType

class ApEditorActivity :
    ApCameraBaseActivity<ApEditorViewModel>(),
    ApEditorNavigator,
    View.OnClickListener,
    PropertiesBSFragment.Properties,
    ApEditorShapeSelectorFragment.Properties,
    ApEditorStickerEmojiSelectorFragment.EmojiListener,
    StickerBSFragment.StickerListener,
    EditingToolsAdapter.OnItemSelected,
    FilterListener,
    OnPhotoEditorListener {

    override fun tag(): String = ApEditorActivity::class.java.simpleName

    companion object {

        const val AP_EDITOR_PHOTO_PAYLOAD = "ap_editor_photo_payload"
        fun getInstance(context: Context, photoUriStr: String): Intent =
            Intent(context, ApEditorActivity::class.java).apply {
                putExtra(AP_EDITOR_PHOTO_PAYLOAD, photoUriStr)
            }
    }

    private var activityApEditorBinding: ActivityGalahadEditorBinding? = null
    private val binding get() = activityApEditorBinding

    private var apEditorViewModel: ApEditorViewModel? = null

    private var photoEditor: PhotoEditor? = null

    private var mPermission: String? = null

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isPermissionGranted(it, mPermission)
        }

    private fun isPermissionGranted(isGranted: Boolean, permission: String?) {}

    private var propertiesBSFragment: PropertiesBSFragment? = null
    private var apEditorShapeSelectorFragment: ApEditorShapeSelectorFragment? = null
    private var apEditorStickerEmojiSelectorFragment: ApEditorStickerEmojiSelectorFragment? = null
    private var stickerBSFragment: StickerBSFragment? = null
    private var shapeBuilder: ShapeBuilder? = null
    private var wonderFont: Typeface? = null
    private val editingToolsAdapter = EditingToolsAdapter(this)
    private val filterViewAdapter = FilterViewAdapter(this)
    private val constraintSet = ConstraintSet()
    private var isFilterVisible = false

    var saveImageUri: Uri? = null
    private var saveFileHelper: FileSaveHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        activityApEditorBinding = ActivityGalahadEditorBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        apEditorViewModel =
            ViewModelProvider.NewInstanceFactory().create(ApEditorViewModel::class.java)
        apEditorViewModel?.setNavigator(this)

        if (savedInstanceState == null) {
            setUpView()
            initial()
        }
    }

    override fun setUpView() {

        propertiesBSFragment = PropertiesBSFragment()
        propertiesBSFragment?.setPropertiesChangeListener(this)
        apEditorShapeSelectorFragment = ApEditorShapeSelectorFragment()
        apEditorShapeSelectorFragment?.setPropertiesChangeListener(this)
        apEditorStickerEmojiSelectorFragment = ApEditorStickerEmojiSelectorFragment()
        apEditorStickerEmojiSelectorFragment?.setEmojiListener(this)
        stickerBSFragment = StickerBSFragment()
        stickerBSFragment?.setStickerListener(this)

        val llmTools = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding?.rvConstraintTools?.layoutManager = llmTools
        binding?.rvConstraintTools?.adapter = editingToolsAdapter
        val llmFilters = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding?.rvFilterView?.layoutManager = llmFilters
        binding?.rvFilterView?.adapter = filterViewAdapter
    }

    override fun initial() {
        binding?.imgUndo?.setOnClickListener(this)
        binding?.imgRedo?.setOnClickListener(this)
        binding?.imgSave?.setOnClickListener(this)
        binding?.imgClose?.setOnClickListener(this)

        photoEditor = binding?.photoEditorView?.run {
            PhotoEditor.Builder(this@ApEditorActivity, this)
                .setPinchTextScalable(true)
                .build()
        }
        photoEditor?.setOnPhotoEditorListener(this)

        getPhotoUriPayload()?.let { photoUriStr ->
            val photoUri = Uri.parse(photoUriStr)
            binding?.photoEditorView?.let {
                Glide.with(this).load(photoUri).into(it.source)
            }
        }
        saveFileHelper = FileSaveHelper(this)
    }

    override fun getPhotoUriPayload(): String? = intent?.getStringExtra(AP_EDITOR_PHOTO_PAYLOAD)

    @SuppressLint("NonConstantResourceId", "MissingPermission")
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgUndo -> {
                photoEditor?.undo()
            }
            R.id.imgRedo -> {
                photoEditor?.redo()
            }
            R.id.imgSave -> {
                saveImage()
            }
            R.id.imgClose -> {
                onBackPressed()
            }
        }
    }

    override fun onColorChanged(colorCode: Int) {
        photoEditor?.setShape(shapeBuilder?.withShapeColor(colorCode))
        binding?.txtCurrentTool?.setText(R.string.ap_editor_select_shape_menu_brush_text_label)
    }

    override fun onOpacityChanged(opacity: Int) {
        photoEditor?.setShape(shapeBuilder?.withShapeOpacity(opacity))
        binding?.txtCurrentTool?.setText(R.string.ap_editor_select_shape_menu_brush_text_label)
    }

    override fun onShapeSizeChanged(shapeSize: Int) {
        photoEditor?.setShape(shapeBuilder?.withShapeSize(shapeSize.toFloat()))
        binding?.txtCurrentTool?.setText(R.string.ap_editor_select_shape_menu_brush_text_label)
    }

    override fun onShapePicked(shapeType: ShapeType?) {
        photoEditor?.setShape(shapeBuilder?.withShapeType(shapeType))
    }

    override fun onEmojiClick(emojiUnicode: String?) {
        photoEditor?.addEmoji(emojiUnicode)
        binding?.txtCurrentTool?.setText(R.string.label_emoji)
    }

    override fun onStickerClick(bitmap: Bitmap?) {
        photoEditor?.addImage(bitmap)
        binding?.txtCurrentTool?.setText(R.string.label_sticker)
    }

    override fun onToolSelected(toolType: ToolType?) {
        when (toolType) {
            ToolType.SHAPE -> {
                photoEditor?.setBrushDrawingMode(true)
                shapeBuilder = ShapeBuilder()
                photoEditor?.setShape(shapeBuilder)
                binding?.txtCurrentTool?.setText(R.string.ap_editor_select_shape_menu_shape_text_label)
                showBottomSheetDialogFragment(apEditorShapeSelectorFragment)
            }
            ToolType.TEXT -> {
                val textEditorDialogFragment = ApEditorAddTextEditorFragment.show(this)
                textEditorDialogFragment.setOnTextEditorListener(object :
                        ApEditorAddTextEditorFragment.TextEditorListener {
                        override fun onDone(inputText: String?, colorCode: Int) {
                            val styleBuilder = TextStyleBuilder()
                            styleBuilder.withTextColor(colorCode)
                            val apFont = ResourcesCompat.getFont(this@ApEditorActivity, R.font.ap_galahad_camera_bold)
                            apFont?.let {
                                styleBuilder.withTextFont(it)
                            }
                            photoEditor?.addText(inputText, styleBuilder)
                            binding?.txtCurrentTool?.setText(R.string.label_text)
                        }
                    })
            }
            ToolType.ERASER -> {
                photoEditor?.brushEraser()
                binding?.txtCurrentTool?.setText(R.string.label_eraser_mode)
            }
            ToolType.FILTER -> {
                binding?.txtCurrentTool?.setText(R.string.label_filter)
                showFilter(true)
            }
            ToolType.EMOJI -> {
                showBottomSheetDialogFragment(apEditorStickerEmojiSelectorFragment)
            }
            ToolType.STICKER -> {
                showBottomSheetDialogFragment(stickerBSFragment)
            }
            else -> {}
        }
    }

    private fun showBottomSheetDialogFragment(fragment: BottomSheetDialogFragment?) {
        if (fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(supportFragmentManager, fragment.tag)
    }

    private fun showFilter(isVisible: Boolean) {
        isFilterVisible = isVisible
        constraintSet.clone(binding?.rootView)
        val rvFilterId: Int =
            binding?.rvFilterView?.id ?: throw IllegalArgumentException("RV Filter ID Expected")
        if (isVisible) {
            constraintSet.clear(rvFilterId, ConstraintSet.START)
            constraintSet.connect(
                rvFilterId, ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.START
            )
            constraintSet.connect(
                rvFilterId, ConstraintSet.END,
                ConstraintSet.PARENT_ID, ConstraintSet.END
            )
        } else {
            constraintSet.connect(
                rvFilterId, ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.END
            )
            constraintSet.clear(rvFilterId, ConstraintSet.END)
        }
        val changeBounds = ChangeBounds()
        changeBounds.duration = 350
        changeBounds.interpolator = AnticipateOvershootInterpolator(1.0f)
        binding?.rootView?.let { TransitionManager.beginDelayedTransition(it, changeBounds) }
        constraintSet.applyTo(binding?.rootView)
    }

    override fun onFilterSelected(photoFilter: PhotoFilter?) {
        photoEditor?.setFilterEffect(photoFilter)
    }

    override fun onBackPressed() {
        val isCacheEmpty =
            photoEditor?.isCacheEmpty ?: throw IllegalArgumentException("isCacheEmpty Expected")

        if (isFilterVisible) {
            showFilter(false)
            binding?.txtCurrentTool?.setText("Photo Editor")
        } else if (!isCacheEmpty) {
            showSaveDialog()
        } else {
            super.onBackPressed()
        }
    }

    @SuppressLint("MissingPermission")
    private fun showSaveDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.msg_save_image))
        builder.setPositiveButton("Save") { _: DialogInterface?, _: Int -> saveImage() }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        builder.setNeutralButton("Discard") { _: DialogInterface?, _: Int -> finish() }
        builder.create().show()
    }

    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
    private fun saveImage() {
        val fileName = ApCameraUtil.getFileName()
        val hasStoragePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if (hasStoragePermission || FileSaveHelper.isSdkHigherThan28()) {
            // ShowLoading HERE
            saveFileHelper?.createFile(
                fileName,
                object : FileSaveHelper.OnFileCreateResult {

                    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
                    override fun onFileCreateResult(
                        created: Boolean,
                        filePath: String?,
                        error: String?,
                        uri: Uri?
                    ) {
                        if (created && filePath != null) {
                            val saveSettings = SaveSettings.Builder()
                                .setClearViewsEnabled(true)
                                .setTransparencyEnabled(true)
                                .build()

                            photoEditor?.saveAsFile(
                                filePath,
                                saveSettings,
                                object : PhotoEditor.OnSaveListener {
                                    override fun onSuccess(imagePath: String) {
                                        saveFileHelper?.notifyThatFileIsNowPubliclyAvailable(
                                            contentResolver
                                        )
                                        // Hide loading HERE
                                        Toast.makeText(
                                            this@ApEditorActivity,
                                            "Image Saved Successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        saveImageUri = uri
                                        binding?.photoEditorView?.let { photoEditorView ->
                                            Glide.with(this@ApEditorActivity).load(saveImageUri)
                                                .into(photoEditorView.source)
                                        }
                                    }

                                    override fun onFailure(exception: Exception) {
                                        // Hide loading HERE
                                        Toast.makeText(
                                            this@ApEditorActivity,
                                            "Failed to save Image",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        } else {
                            // Hide loading HERE
                            error?.let {
                                Toast.makeText(
                                    this@ApEditorActivity,
                                    "$error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            )
        } else {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun requestPermission(permission: String): Boolean {
        val isGranted =
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        if (!isGranted) {
            mPermission = permission
            permissionLauncher.launch(permission)
        }
        return isGranted
    }

    override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) {}

    override fun onEditTextChangeListener(rootView: View?, text: String?, colorCode: Int) {
        val textEditorDialogFragment = ApEditorAddTextEditorFragment.show(this, text.toString(), colorCode)
        textEditorDialogFragment.setOnTextEditorListener(object : ApEditorAddTextEditorFragment.TextEditorListener {
            override fun onDone(inputText: String?, colorCode: Int) {
                val styleBuilder = TextStyleBuilder()
                styleBuilder.withTextColor(colorCode)
                val apFont = ResourcesCompat.getFont(this@ApEditorActivity, R.font.ap_galahad_camera_bold)
                apFont?.let {
                    styleBuilder.withTextFont(it)
                }
                if (rootView != null) {
                    photoEditor?.editText(rootView, inputText, styleBuilder)
                }
                binding?.txtCurrentTool?.setText(R.string.label_text)
            }
        })
    }

    override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) {}

    override fun onStartViewChangeListener(viewType: ViewType?) {}

    override fun onStopViewChangeListener(viewType: ViewType?) {}

    override fun onTouchSourceImage(event: MotionEvent?) {}
}
