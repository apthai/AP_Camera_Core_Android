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
import com.apthai.apcameraxcore.galahad.editor.fragment.ApEditorEmojiSelectorFragment
import com.apthai.apcameraxcore.galahad.editor.fragment.ApEditorShapeSelectorFragment
import com.apthai.apcameraxcore.galahad.editor.fragment.ApEditorStickerSelectorFragment
import com.apthai.apcameraxcore.galahad.editor.fragment.ApEditorAddTextEditorFragment
import com.apthai.apcameraxcore.galahad.editor.tools.EditingToolsAdapter
import com.apthai.apcameraxcore.galahad.editor.tools.FileSaveHelper
import com.apthai.apcameraxcore.galahad.editor.tools.ToolType
import com.apthai.apcameraxcore.galahad.util.ApCameraUtil
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ja.burhanrashid52.photoeditor.*
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder
import ja.burhanrashid52.photoeditor.shape.ShapeType

class ApEditorActivity :
    ApCameraBaseActivity<ApEditorViewModel>(),
    ApEditorNavigator,
    View.OnClickListener,
    ApEditorShapeSelectorFragment.Properties,
    ApEditorEmojiSelectorFragment.OnEmojiSelectedListener,
    ApEditorStickerSelectorFragment.OnStickerSelectedListener,
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

    private var apEditorShapeSelectorFragment: ApEditorShapeSelectorFragment? = null
    private var apEditorEmojiSelectorFragment: ApEditorEmojiSelectorFragment? = null
    private var apEditorStickerSelectorFragment: ApEditorStickerSelectorFragment? = null
    private var shapeBuilder: ShapeBuilder? = null
    private var wonderFont: Typeface? = null
    private var editingToolsAdapter : EditingToolsAdapter ?= null

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

        apEditorShapeSelectorFragment = ApEditorShapeSelectorFragment()
        apEditorShapeSelectorFragment?.setPropertiesChangeListener(this)
        apEditorEmojiSelectorFragment = ApEditorEmojiSelectorFragment()
        apEditorEmojiSelectorFragment?.setOnEmojiSelectedListener(this)
        apEditorStickerSelectorFragment = ApEditorStickerSelectorFragment()
        apEditorStickerSelectorFragment?.setOnStickerSelectedListener(this)

        editingToolsAdapter = EditingToolsAdapter(this, this)

        val llmTools = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding?.apEditorConstraintToolsView?.layoutManager = llmTools
        binding?.apEditorConstraintToolsView?.adapter = editingToolsAdapter
    }

    override fun initial() {
        binding?.apEditorToolUndoImageButtonView?.setOnClickListener(this)
        binding?.apEditorToolRedoImageButtonView?.setOnClickListener(this)
        binding?.apEditorSaveImageButtonView?.setOnClickListener(this)
        binding?.apEditorCloseImageButtonView?.setOnClickListener(this)

        photoEditor = binding?.apEditorPhotoEditorView?.run {
            PhotoEditor.Builder(this@ApEditorActivity, this)
                .setPinchTextScalable(true)
                .build()
        }
        photoEditor?.setOnPhotoEditorListener(this)

        getPhotoUriPayload()?.let { photoUriStr ->
            val photoUri = Uri.parse(photoUriStr)
            binding?.apEditorPhotoEditorView?.let {
                Glide.with(this).load(photoUri).into(it.source)
            }
        }
        saveFileHelper = FileSaveHelper(this)
    }

    override fun getPhotoUriPayload(): String? = intent?.getStringExtra(AP_EDITOR_PHOTO_PAYLOAD)

    @SuppressLint("NonConstantResourceId", "MissingPermission")
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ap_editor_tool_undo_image_button_view -> {
                photoEditor?.undo()
            }
            R.id.ap_editor_tool_redo_image_button_view -> {
                photoEditor?.redo()
            }
            R.id.ap_editor_save_image_button_view -> {
                saveImage()
            }
            R.id.ap_editor_close_image_button_view -> {
                onBackPressed()
            }
        }
    }

    override fun onColorChanged(colorCode: Int) {
        photoEditor?.setShape(shapeBuilder?.withShapeColor(colorCode))
        binding?.apEditorCurrentSelectedToolTextLabel?.setText(R.string.ap_editor_select_shape_menu_brush_text_label)
    }

    override fun onOpacityChanged(opacity: Int) {
        photoEditor?.setShape(shapeBuilder?.withShapeOpacity(opacity))
        binding?.apEditorCurrentSelectedToolTextLabel?.setText(R.string.ap_editor_select_shape_menu_brush_text_label)
    }

    override fun onShapeSizeChanged(shapeSize: Int) {
        photoEditor?.setShape(shapeBuilder?.withShapeSize(shapeSize.toFloat()))
        binding?.apEditorCurrentSelectedToolTextLabel?.setText(R.string.ap_editor_select_shape_menu_brush_text_label)
    }

    override fun onShapePicked(shapeType: ShapeType?) {
        photoEditor?.setShape(shapeBuilder?.withShapeType(shapeType))
    }
    override fun onEmojiSelected(emojiStr: String?) {
        photoEditor?.addEmoji(emojiStr)
        binding?.apEditorCurrentSelectedToolTextLabel?.setText(R.string.label_emoji)
    }

    override fun onStickerSelected(bitmap: Bitmap) {
        photoEditor?.addImage(bitmap)
        binding?.apEditorCurrentSelectedToolTextLabel?.setText(R.string.label_sticker)
    }

    override fun onToolSelected(toolType: ToolType?) {
        when (toolType) {
            ToolType.SHAPE -> {
                photoEditor?.setBrushDrawingMode(true)
                shapeBuilder = ShapeBuilder()
                photoEditor?.setShape(shapeBuilder)
                binding?.apEditorCurrentSelectedToolTextLabel?.setText(R.string.ap_editor_select_shape_menu_shape_text_label)
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
                            binding?.apEditorCurrentSelectedToolTextLabel?.setText(R.string.label_text)
                        }
                    })
            }
            ToolType.ERASER -> {
                photoEditor?.brushEraser()
                binding?.apEditorCurrentSelectedToolTextLabel?.setText(R.string.label_eraser_mode)
            }
            ToolType.EMOJI -> {
                showBottomSheetDialogFragment(apEditorEmojiSelectorFragment)
            }
            ToolType.STICKER -> {
                showBottomSheetDialogFragment(apEditorStickerSelectorFragment)
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

    override fun onFilterSelected(photoFilter: PhotoFilter?) {
        photoEditor?.setFilterEffect(photoFilter)
    }

    override fun onBackPressed() {
        val isCacheEmpty =
            photoEditor?.isCacheEmpty ?: throw IllegalArgumentException("isCacheEmpty Expected")

        if (!isCacheEmpty) {
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
                                        binding?.apEditorPhotoEditorView?.let { photoEditorView ->
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
                binding?.apEditorCurrentSelectedToolTextLabel?.setText(R.string.label_text)
            }
        })
    }

    override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) {}

    override fun onStartViewChangeListener(viewType: ViewType?) {}

    override fun onStopViewChangeListener(viewType: ViewType?) {}

    override fun onTouchSourceImage(event: MotionEvent?) {}
}
