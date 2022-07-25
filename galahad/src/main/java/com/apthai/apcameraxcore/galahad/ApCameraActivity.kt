package com.apthai.apcameraxcore.galahad

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaActionSound
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.MotionEvent
import android.view.OrientationEventListener
import android.view.ScaleGestureDetector
import android.view.Surface
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.apthai.apcameraxcore.common.ApCameraBaseActivity
import com.apthai.apcameraxcore.common.model.ApPhoto
import com.apthai.apcameraxcore.common.utils.ImageUtil
import com.apthai.apcameraxcore.galahad.databinding.ActivityGalahadCameraBinding
import com.apthai.apcameraxcore.galahad.previewer.contract.ApPagerPreviewResultContract
import com.apthai.apcameraxcore.galahad.previewer.contract.ApPreviewResultContract
import com.apthai.apcameraxcore.galahad.util.ApCameraUtil
import com.bumptech.glide.Glide
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class ApCameraActivity :
    ApCameraBaseActivity<ApCameraViewModel>(),
    ApCameraNavigator,
    View.OnClickListener,
    ImageCapture.OnImageSavedCallback {

    companion object {

        private const val REQUEST_CODE_PERMISSIONS = 112
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).toTypedArray()
    }

    override fun tag(): String = ApCameraActivity::class.java.simpleName

    private var activityApCameraBinding: ActivityGalahadCameraBinding? = null
    private val binding get() = activityApCameraBinding

    private var apCameraViewModel: ApCameraViewModel? = null
    private var cameraProcessFuture: ListenableFuture<ProcessCameraProvider>? = null
    private var currentCameraImageCapture: ImageCapture? = null
    private var currentCameraImageAnalysis: ImageAnalysis? = null
    private lateinit var cameraExecutor: ExecutorService
    private var cameraFacing: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var cameraLensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var cameraFlashMode: Int = ImageCapture.FLASH_MODE_OFF
    private var cameraAspectRatio: Int = AspectRatio.RATIO_4_3

    private var currentCamera: Camera? = null
    private var rootDir: String = ""
    private var currentPhotoUri: Uri? = null

    private val cameraRunnable: Runnable = Runnable {
        bindCamera()
    }

    private val previewActivityContract =
        registerForActivityResult(ApPreviewResultContract()) {}

    private val pagerPreviewActivityContract =
        registerForActivityResult(ApPagerPreviewResultContract()) {}

    private var currentPhotoList: MutableList<ApPhoto> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityApCameraBinding = ActivityGalahadCameraBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        apCameraViewModel =
            ViewModelProvider.NewInstanceFactory().create(ApCameraViewModel::class.java)
        apCameraViewModel?.setNavigator(this)

        this.rootDir = this.externalCacheDir!!.absolutePath
        if (savedInstanceState == null) {
            setUpView()
            initial()
        }
    }

    override fun onStart() {
        super.onStart()
        this.orientationEventListener.enable()
    }

    override fun onStop() {
        super.onStop()
        this.orientationEventListener.disable()
    }

    override fun setUpView() {
        apCameraViewModel?.shareCurrentPhotos?.observe(this) { apPhotos ->
            if (apPhotos.isEmpty()) {
                return@observe
            }
            val reversePhotos = apPhotos.asReversed()
            val firstPhoto: ApPhoto = reversePhotos[0]
            binding?.apCameraViewGalleryButton?.let { imageView ->
                Glide.with(this).load(firstPhoto.uriPath).circleCrop().into(imageView)
            }
        }

        fetchCurrentPhotoList()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initial() {
        if (isCameraPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        binding?.apCameraViewCaptureButton?.setOnClickListener(this)
        binding?.apCameraViewSwitchButton?.setOnClickListener(this)
        binding?.apCameraViewFlashButton?.setOnClickListener(this)
        binding?.apCameraViewAspectRatioButton?.setOnClickListener(this)
        binding?.apCameraViewGalleryButton?.setOnClickListener(this)

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun isCameraPermissionsGranted(): Boolean = REQUIRED_PERMISSIONS.all { permission ->
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun startCamera() {
        cameraProcessFuture = ProcessCameraProvider.getInstance(this).apply {
            addListener(cameraRunnable, ContextCompat.getMainExecutor(this@ApCameraActivity))
        }
    }

    override fun bindCamera() {
        val cameraProvider = cameraProcessFuture?.get()
        cameraProvider?.let { provider ->
            try {
                val cameraPreview = initializePreviewSurface()
                currentCameraImageAnalysis = initializeImageAnalysis()
                currentCameraImageCapture = initializeImageCapture()

                provider.unbindAll()
                currentCamera = provider.bindToLifecycle(
                    this,
                    cameraFacing,
                    cameraPreview,
                    currentCameraImageAnalysis,
                    currentCameraImageCapture
                )
                initialAutoFocusAndPinchToZoom()
            } catch (exception: Exception) {
                Toast.makeText(
                    this,
                    "Camera bind exception from ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } ?: kotlin.run {
            Toast.makeText(this, "Camera provider is unavailable", Toast.LENGTH_SHORT).show()
        }
    }

    override fun takePhoto() {
        val imageCapture = currentCameraImageCapture ?: return
        val fileName = ApCameraUtil.getFileName()
        val contentValues = getContentValue(fileName)
        val outputOptions = getOutputFileOption(contentValues)
        playShutterSound()
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), this)
    }

    override fun takePhotoWithOutSave() {
        val imageCapture = currentCameraImageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {

                @SuppressLint("UnsafeOptInUsageError")
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    super.onCaptureSuccess(imageProxy)

                    imageProxy.image?.let {
                        val imageBitmap = ImageUtil.convertImagePoxyToBitmap(
                            it,
                            cameraLensFacing == CameraSelector.LENS_FACING_FRONT
                        )

                        imageBitmap?.let { itemBitmap ->
                            apCameraViewModel?.saveImageToLocal(
                                "$rootDir/${ImageUtil.BASE_IMAGE_FOLDER}",
                                "ap_camera_make.jpeg",
                                itemBitmap
                            )
                        }
                    }
                }
            }
        )
    }

    override fun saveImageToLocalSuccess(currentPathFile: String) {
    }

    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
        val msg = "Photo capture succeeded: ${outputFileResults.savedUri}"
        Toast.makeText(this@ApCameraActivity, msg, Toast.LENGTH_SHORT).show()
        outputFileResults.savedUri?.let { photoUri ->
            currentPhotoUri = photoUri
            binding?.apCameraViewGalleryButton?.let { galleryButtonView ->
                Glide.with(this).load(currentPhotoUri).circleCrop().into(galleryButtonView)
            }
        }
    }

    override fun onError(exception: ImageCaptureException) {
        Toast.makeText(this@ApCameraActivity, exception.message, Toast.LENGTH_SHORT)
            .show()
    }

    override fun flipCameraFacing() {
        if (cameraFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
            cameraFacing = CameraSelector.DEFAULT_FRONT_CAMERA
            this.cameraLensFacing = CameraSelector.LENS_FACING_FRONT
        } else if (cameraFacing == CameraSelector.DEFAULT_FRONT_CAMERA) {
            cameraFacing =
                CameraSelector.DEFAULT_BACK_CAMERA
            this.cameraLensFacing = CameraSelector.LENS_FACING_BACK
        }

        startCamera()
    }

    override fun toggleCameraFlashMode() {
        when (cameraFlashMode) {
            ImageCapture.FLASH_MODE_AUTO -> {
                cameraFlashMode = ImageCapture.FLASH_MODE_ON
                binding?.apCameraViewFlashButton?.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_ap_camera_toggle_flash_on
                    )
                )
            }
            ImageCapture.FLASH_MODE_ON -> {
                cameraFlashMode = ImageCapture.FLASH_MODE_OFF
                binding?.apCameraViewFlashButton?.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_ap_camera_toggle_flash_off
                    )
                )
            }
            ImageCapture.FLASH_MODE_OFF -> {
                cameraFlashMode = ImageCapture.FLASH_MODE_AUTO
                binding?.apCameraViewFlashButton?.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_ap_camera_toggle_flash_auto
                    )
                )
            }
        }
        currentCameraImageCapture?.let {
            it.flashMode = cameraFlashMode
        }
    }

    @SuppressLint("CheckResult")
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ap_camera_view_capture_button -> {
                takePhoto()
            }
            R.id.ap_camera_view_switch_button -> {
                flipCameraFacing()
            }
            R.id.ap_camera_view_flash_button -> {
                toggleCameraFlashMode()
            }
            R.id.ap_camera_view_aspect_ratio_button -> {
                toggleAspectRatio()
            }
            R.id.ap_camera_view_gallery_button -> {
                MaterialDialog(this).show {
                    listItems(items = listOf("Grid view", "Pager view")) { dialog, index, _ ->
                        dialog.dismiss()
                        when (index) {
                            0 -> launchPreviewPhotoActivity()
                            1 -> launchPagerPreviewPhotoActivity()
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (isCameraPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun initializePreviewSurface(): Preview = Preview.Builder()
        .setTargetAspectRatio(cameraAspectRatio).build().also { preview ->
            preview.setSurfaceProvider(binding?.apCameraViewPreview?.surfaceProvider)
        }

    override fun initializeImageAnalysis(): ImageAnalysis = ImageAnalysis.Builder()
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()

    override fun initializeImageCapture(): ImageCapture = ImageCapture.Builder()
        .setTargetAspectRatio(cameraAspectRatio)
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build().also { imgCapture ->
            imgCapture.flashMode = cameraFlashMode
        }

    override fun getContentValue(outputFileName: String): ContentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, outputFileName)
        put(MediaStore.MediaColumns.MIME_TYPE, ApCameraUtil.AP_CAMERA_DEFAULT_MIME_TYPE)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, ApCameraUtil.AP_CAMERA_DEFAULT_FOLDER)
        }
    }

    override fun getOutputFileOption(contentValue: ContentValues): ImageCapture.OutputFileOptions =
        ImageCapture.OutputFileOptions
            .Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValue
            )
            .build()

    override fun toggleAspectRatio() {
        when (cameraAspectRatio) {
            AspectRatio.RATIO_4_3 -> {
                cameraAspectRatio = AspectRatio.RATIO_16_9
            }
            AspectRatio.RATIO_16_9 -> {
                cameraAspectRatio = AspectRatio.RATIO_4_3
            }
        }
        startCamera()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initialAutoFocusAndPinchToZoom() {
        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector?): Boolean {
                val currentZoomRatio =
                    currentCamera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 0.0f
                val delta = detector?.scaleFactor ?: 0.0f
                val zoomRatio = currentZoomRatio * delta
                currentCamera?.cameraControl?.setZoomRatio(zoomRatio)
                return true
            }
        }
        val scaleGestureDetector = ScaleGestureDetector(this, listener)
        binding?.apCameraViewPreview?.let { previewView ->
            previewView.afterMeasured {
                previewView.setOnTouchListener { _, motionEvent ->
                    scaleGestureDetector.onTouchEvent(motionEvent)
                    return@setOnTouchListener when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                                previewView.width.toFloat(),
                                previewView.height.toFloat()
                            )
                            val afPointWidth = 1.0f / 6.0f
                            val aePointWidth = afPointWidth * 1.5f
                            val manualFocusPoint =
                                factory.createPoint(motionEvent.x, motionEvent.y, afPointWidth)
                            val manualExposePoint =
                                factory.createPoint(motionEvent.x, motionEvent.y, aePointWidth)
                            try {
                                animateAutofocusEvent(motionEvent.x, motionEvent.y)
                                currentCamera?.cameraControl?.startFocusAndMetering(
                                    FocusMeteringAction.Builder(
                                        manualFocusPoint,
                                        FocusMeteringAction.FLAG_AF
                                    ).addPoint(
                                        manualExposePoint,
                                        FocusMeteringAction.FLAG_AE
                                    ).apply {
                                        disableAutoCancel()
                                    }.build()
                                )
                            } catch (exception: CameraInfoUnavailableException) {
                                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                            }
                            true
                        }
                        else -> false
                    }
                }
            }
        }
    }

    private inline fun View.afterMeasured(crossinline block: () -> Unit) {
        if (measuredWidth > 0 && measuredHeight > 0) {
            block()
        } else {
            viewTreeObserver.addOnGlobalLayoutListener(object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        if (measuredWidth > 0 && measuredHeight > 0) {
                            viewTreeObserver.removeOnGlobalLayoutListener(this)
                            block()
                        }
                    }
                })
        }
    }

    override fun animateAutofocusEvent(positionX: Float, positionY: Float) {
        val width = binding?.apCameraFocusCircleImageView?.width ?: 0
        val height = binding?.apCameraFocusCircleImageView?.height ?: 0
        binding?.apCameraFocusCircleImageView?.x = (positionX - width / 2)
        binding?.apCameraFocusCircleImageView?.y = (positionY - height / 2)
        binding?.apCameraFocusCircleImageView?.visibility = View.VISIBLE
        binding?.apCameraFocusCircleImageView?.alpha = 1F
        binding?.apCameraFocusCircleImageView?.animate()?.apply {
            startDelay = 500
            duration = 300
            alpha(0F)
            setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator?) {}

                override fun onAnimationEnd(p0: Animator?) {
                    binding?.apCameraFocusCircleImageView?.visibility = View.INVISIBLE
                }

                override fun onAnimationCancel(p0: Animator?) {}

                override fun onAnimationRepeat(p0: Animator?) {}
            })
        }
    }

    override fun playShutterSound() {
        val audioManager: AudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> {
                MediaActionSound().apply {
                    play(MediaActionSound.SHUTTER_CLICK)
                }
            }
            else -> {}
        }
    }

    override fun loadDing(isLoading: Boolean) {
    }

    private val orientationEventListener by lazy {
        object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) {
                    return
                }
                val rotation = when (orientation) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                currentCameraImageAnalysis?.let {
                    it.targetRotation = rotation
                }
                currentCameraImageCapture?.let {
                    it.targetRotation = rotation
                }
            }
        }
    }

    override fun launchPreviewPhotoActivity() {
        previewActivityContract.launch(tag())
    }

    override fun launchPagerPreviewPhotoActivity() {
        pagerPreviewActivityContract.launch(tag())
    }

    override fun fetchCurrentPhotoList() {
        val mediaCursor = contentResolver.query(
            fetchMediaCollection,
            fetchMediaProjection,
            fetchMediaSelection,
            fetchMediaSelectionArgs,
            fetchMediaSortOrder
        )

        mediaCursor?.use { cursor ->
            val fetchIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val fetchFileNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val fetchFileSizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val fetchDateAddedColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val fetchDateModifiedColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val fetchBucketIdColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val fetchBucketNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val fetchMimeTypeColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)

            while (cursor.moveToNext()) {
                val fileId = cursor.getLong(fetchIdColumn)
                val fileName = cursor.getString(fetchFileNameColumn)
                val fileSize = cursor.getInt(fetchFileSizeColumn)
                val fileDateAdded = cursor.getLong(fetchDateAddedColumn)
                val fileDateModified = cursor.getLong(fetchDateModifiedColumn)
                val fileBucketId = cursor.getLong(fetchBucketIdColumn)
                val fileBucketName = cursor.getString(fetchBucketNameColumn)
                val fileMimeType = cursor.getString(fetchMimeTypeColumn)

                val contentUri: Uri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fileId)

                currentPhotoList += ApPhoto(
                    id = fileId,
                    uriPath = contentUri,
                    fileName = fileName,
                    fileSize = fileSize,
                    createdAt = fileDateAdded,
                    modifiedAt = fileDateModified,
                    folderId = fileBucketId,
                    folderName = fileBucketName,
                    mimeType = fileMimeType
                )
            }
            apCameraViewModel?.setSharedCurrentPhotos(currentPhotoList)
        }
    }
}
