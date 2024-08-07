package com.apthai.apcameraxcore.galahad

import android.Manifest
import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.media.AudioManager
import android.media.MediaActionSound
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.OrientationEventListener
import android.view.ScaleGestureDetector
import android.view.Surface
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
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
import com.apthai.apcameraxcore.galahad.previewer.contract.ApJustPreviewResultContract
import com.apthai.apcameraxcore.galahad.previewer.contract.ApMultiplePagerPreviewResultContract
import com.apthai.apcameraxcore.galahad.previewer.contract.ApPagerPreviewResultContract
import com.apthai.apcameraxcore.galahad.previewer.contract.ApPreviewResultContract
import com.apthai.apcameraxcore.galahad.util.ApCameraConst
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

//        private const val REQUEST_CODE_PERMISSIONS = 112
        private val REQUIRED_PERMISSIONS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                arrayOf(
                    CAMERA,
                    READ_MEDIA_IMAGES,
                    READ_MEDIA_VIDEO,
                    READ_MEDIA_VISUAL_USER_SELECTED
                )
            } else
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(
                        CAMERA,
                        READ_MEDIA_IMAGES,
                        READ_MEDIA_VIDEO
                    )
                } else {
                    arrayOf(
                        CAMERA,
                        READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }

        fun getInstance(context: Context, payload: Bundle, fromScreenTag: String): Intent =
            Intent(context, ApCameraActivity::class.java).apply {
                putExtra(ApCameraConst.ApCameraPayload.AP_CAMERA_BUNDLE_PAYLOAD_CONST, payload)
                putExtra(ApCameraUtil.Generic.AP_CAMERA_DEFAULT_FROM_SCREEN_TAG, fromScreenTag)
            }
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
    private var currentPhotoMultipleShot: ArrayList<String> = arrayListOf()

    private val cameraRunnable: Runnable = Runnable {
        bindCamera()
    }
    private var _previewActivityContract: ActivityResultLauncher<String>? = null
    private var _pagerPreviewActivityContract: ActivityResultLauncher<String>? = null
    private var _apMultiPagerPreviewActContract: ActivityResultLauncher<ArrayList<String>>? = null
    private var _previewTransitionContract: ActivityResultLauncher<String>? = null

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
        this.initActivityContract()
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

//    override fun onResume() {
//        super.onResume()
//        Log.e(tag(), "chooseGalleryActResultContract onResume ")
//    }

    override fun setUpView() {
        /*Initialize payload*/
        cameraLensFacing = getCameraFacingTypePayload()
        cameraFlashMode = getCameraFlashTypePayload()
        cameraAspectRatio = getCameraAspectRatioTypePayload()
//        val isOnlyCamera = getIsOnlyCallCameraPayload()

        this.setUpCameraMode()

//        if (!isOnlyCamera) {
//            this.setUpCameraViewGalleryForView()
//        } else {
//            binding?.apCameraViewGalleryButton?.visibility = View.GONE
//        }
    }

    override fun setUpCameraMode() {
        when (this.getCameraMode()) {
            ApCameraConst.ApCameraMode.AP_CAMERA_VAL_IS_ONLY_CAMERA_APC_MODE -> {
                binding?.apCameraViewGalleryButton?.visibility = View.GONE
            }

            ApCameraConst.ApCameraMode.AP_CAMERA_VAL_MULTIPLE_SHOT_PREVIEW_MODE -> {
                binding?.apCameraViewGalleryButton?.visibility = View.GONE
            }

            ApCameraConst.ApCameraMode.AP_CAMERA_VAL_VIEW_GALLERY_MODE -> {
                this.setUpCameraViewGalleryForView()
            }

            ApCameraConst.ApCameraMode.AP_CAMERA_VAL_ONLY_EDIT_PHOTO_MODE -> {
                binding?.apCameraViewGalleryButton?.visibility = View.GONE
                val imageList = getImageUriFromIntentListString()
                if (imageList.isNotEmpty()) {
                    this.launchApMultiplePagerPreviewActivity(imageList)
                } else {
                    Toast.makeText(this, "image your is empty..", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }

            else -> {
                binding?.apCameraViewGalleryButton?.visibility = View.GONE
            }
        }
    }

    override fun setUpCameraViewGalleryForView() {
        binding?.apCameraViewGalleryButton?.visibility = View.VISIBLE
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

    override fun initActivityContract() {
        this.initPreviewActivityContract()
        this.initPagerPreviewActivityContract()
        this.initAPMultiplePagerPreviewActivityContract()
        this.initPreviewTransitionContract()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initial() {
        if (isCameraPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions.launch(
                REQUIRED_PERMISSIONS
            )
        }

        binding?.apCameraViewCaptureButton?.setOnClickListener(this)
        binding?.apCameraViewSwitchButton?.setOnClickListener(this)
        binding?.apCameraViewFlashButton?.setOnClickListener(this)
        binding?.apCameraViewAspectRatioButton?.setOnClickListener(this)
        binding?.apCameraViewGalleryButton?.setOnClickListener(this)

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun isCameraPermissionsGranted(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            (
                    ActivityCompat.checkSelfPermission(
                        this,
                        CAMERA
                    ) == PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.RECORD_AUDIO
                            ) == PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                this,
                                READ_MEDIA_IMAGES
                            ) == PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                this,
                                READ_MEDIA_VIDEO
                            ) == PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) == PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                this,
                                READ_MEDIA_VISUAL_USER_SELECTED
                            ) == PERMISSION_GRANTED
                    )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            (
                    ActivityCompat.checkSelfPermission(
                        this,
                        CAMERA
                    ) == PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.RECORD_AUDIO
                            ) == PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                this,
                                READ_MEDIA_IMAGES
                            ) == PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                this,
                                READ_MEDIA_VIDEO
                            ) == PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) == PERMISSION_GRANTED
                    )
        } else {
            (
                    ActivityCompat.checkSelfPermission(
                        this,
                        CAMERA
                    ) == PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                this,
                                READ_EXTERNAL_STORAGE
                            ) == PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PERMISSION_GRANTED
                    )
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
                val cameraInfo = currentCamera?.cameraInfo
                cameraInfo?.let { info ->
                    observeCameraState(info)
                }

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
        val fileName = getCameraFileNamePayload()
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

                        imageBitmap.let { itemBitmap ->
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
        outputFileResults.savedUri?.let { photoUri ->
            currentPhotoUri = photoUri
//            val isOnlyCamera = getIsOnlyCallCameraPayload()
            when (this.getCameraMode()) {
                ApCameraConst.ApCameraMode.AP_CAMERA_VAL_IS_ONLY_CAMERA_APC_MODE -> {
                    this._previewTransitionContract?.launch(currentPhotoUri.toString())
                }

                ApCameraConst.ApCameraMode.AP_CAMERA_VAL_MULTIPLE_SHOT_PREVIEW_MODE -> {
                    currentPhotoMultipleShot.add(photoUri.toString())
                    if (currentPhotoMultipleShot.isNotEmpty()) {
                        binding?.apCameraViewGalleryButton?.let { galleryButtonView ->
                            galleryButtonView.visibility = View.VISIBLE
                            Glide.with(this).load(currentPhotoUri).circleCrop()
                                .into(galleryButtonView)
                        }
                    } else {
                        binding?.apCameraViewGalleryButton?.visibility = View.GONE
                    }
                }

                ApCameraConst.ApCameraMode.AP_CAMERA_VAL_VIEW_GALLERY_MODE -> {
                    binding?.apCameraViewGalleryButton?.let { galleryButtonView ->
                        Glide.with(this).load(currentPhotoUri).circleCrop().into(galleryButtonView)
                    }
                }

                else -> {
                    this._previewTransitionContract?.launch(currentPhotoUri.toString())
                }
            }

            // previous version
//            if (isOnlyCamera) {
//                previewTransitionContract.launch(currentPhotoUri.toString())
//            } else {
//                binding?.apCameraViewGalleryButton?.let { galleryButtonView ->
//                    Glide.with(this).load(currentPhotoUri).circleCrop().into(galleryButtonView)
//                }
//            }
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
            R.id.ap_camera_view_capture_button -> this.takePhoto()
            R.id.ap_camera_view_switch_button -> this.flipCameraFacing()
            R.id.ap_camera_view_flash_button -> this.toggleCameraFlashMode()
            R.id.ap_camera_view_aspect_ratio_button -> this.toggleAspectRatio()
            R.id.ap_camera_view_gallery_button -> this.onClickViewGalleryButton()
        }
    }

    @SuppressLint("CheckResult")
    override fun onClickViewGalleryButton() {
        when (this.getCameraMode()) {
            ApCameraConst.ApCameraMode.AP_CAMERA_VAL_MULTIPLE_SHOT_PREVIEW_MODE -> {
                if (this.currentPhotoMultipleShot.isNotEmpty()) {
                    this.launchApMultiplePagerPreviewActivity(this.currentPhotoMultipleShot)
                }
            }

            else -> {
                MaterialDialog(this).show {
                    listItems(
                        items = listOf(
                            "Grid view",
                            "Pager view",
                            "Other"
                        )
                    ) { dialog, index, _ ->
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
        currentPhotoUri?.let { latestUri ->
            val fallbackIntent = Intent().apply {
                putExtra(
                    ApCameraConst.ApCameraPayload.AP_CAMERA_OUTPUT_URI_STRING,
                    latestUri.toString()
                )
            }
            setResult(Activity.RESULT_OK, fallbackIntent)
        }
        cameraExecutor.shutdown()
    }

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            Log.e("TAG", "requestPermissions: ${results.map { it }}")
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                (
                        ContextCompat.checkSelfPermission(
                            this,
                            READ_MEDIA_IMAGES
                        ) == PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(
                                    this,
                                    READ_MEDIA_VIDEO
                                ) == PERMISSION_GRANTED
                        ) && ContextCompat.checkSelfPermission(
                    this,
                    CAMERA
                ) == PERMISSION_GRANTED
            ) {
                // Full access on Android 13 (API level 33) or higher
                startCamera()
            } else if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                ContextCompat.checkSelfPermission(
                    this,
                    READ_MEDIA_VISUAL_USER_SELECTED
                ) == PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    CAMERA
                ) == PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    CAMERA
                ) == PERMISSION_GRANTED
            ) {
                // Partial access on Android 14 (API level 34) or higher
                startCamera()
            } else if (ContextCompat.checkSelfPermission(
                    this,
                    READ_EXTERNAL_STORAGE
                ) == PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    CAMERA
                ) == PERMISSION_GRANTED
            ) {
                // Full access up to Android 12 (API level 32)
                startCamera()
            } else {
                // Access denied
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_LONG
                ).show()
            }
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
        .setResolutionSelector(
            ResolutionSelector.Builder()
                .setAllowedResolutionMode(ResolutionSelector.PREFER_HIGHER_RESOLUTION_OVER_CAPTURE_RATE)
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                .build()
        )
        .build().also {
            it.setSurfaceProvider(binding?.apCameraViewPreview?.surfaceProvider)
        }

    override fun initializeImageAnalysis(): ImageAnalysis = ImageAnalysis.Builder()
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()

    override fun initializeImageCapture(): ImageCapture = ImageCapture.Builder()
        .setResolutionSelector(
            ResolutionSelector.Builder()
                .setAllowedResolutionMode(ResolutionSelector.PREFER_HIGHER_RESOLUTION_OVER_CAPTURE_RATE)
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                .build()
        )
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build().also { imgCapture ->
            imgCapture.flashMode = cameraFlashMode
        }

    override fun getContentValue(outputFileName: String): ContentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, outputFileName)
        put(MediaStore.MediaColumns.MIME_TYPE, ApCameraUtil.AP_CAMERA_DEFAULT_MIME_TYPE)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, getCameraDirectoryPathPayload())
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
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio =
                    currentCamera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 0.0f
                val delta = detector.scaleFactor
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
                override fun onAnimationStart(animation: Animator) {
                }

                override fun onAnimationEnd(animation: Animator) {
                    binding?.apCameraFocusCircleImageView?.visibility = View.INVISIBLE
                }

                override fun onAnimationCancel(animation: Animator) {
                }

                override fun onAnimationRepeat(animation: Animator) {
                }
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
        this._previewActivityContract?.launch(tag())
    }

    override fun launchPagerPreviewPhotoActivity() {
        this._pagerPreviewActivityContract?.launch(tag())
    }

    override fun launchApMultiplePagerPreviewActivity(imageUriList: ArrayList<String>) {
        this._apMultiPagerPreviewActContract?.launch(imageUriList)
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

    override fun getCameraFacingTypePayload(): Int = this.getPlayLoadBundle()
        ?.getInt(ApCameraConst.ApCameraPayload.AP_CAMERA_FACING_TYPE, cameraLensFacing)
        ?: cameraLensFacing

    override fun getCameraFlashTypePayload(): Int = this.getPlayLoadBundle()
        ?.getInt(ApCameraConst.ApCameraPayload.AP_CAMERA_FLASH_TYPE, cameraFlashMode)
        ?: cameraFlashMode

    override fun getCameraAspectRatioTypePayload(): Int = this.getPlayLoadBundle()?.getInt(
        ApCameraConst.ApCameraPayload.AP_CAMERA_ASPECT_RATIO_TYPE,
        cameraAspectRatio
    ) ?: cameraAspectRatio

    override fun getCameraDirectoryPathPayload(): String = this.getPlayLoadBundle()
        ?.getString(ApCameraConst.ApCameraPayload.AP_CAMERA_DIRECTORY_ROOT_PATH)
        ?: ApCameraUtil.AP_CAMERA_DEFAULT_FOLDER

    override fun getCameraFileNamePayload(): String =
        this.getPlayLoadBundle()?.getString(ApCameraConst.ApCameraPayload.AP_CAMERA_FILE_NAME)
            ?: ApCameraUtil.getFileName()

    override fun getIsOnlyCallCameraPayload(): Boolean =
        this.getPlayLoadBundle()
            ?.getBoolean(ApCameraConst.ApCameraPayload.AP_CAMERA_IS_ONLY_CALL_CAMERA, false)
            ?: false

    override fun getFromScreenTagName(): String =
        intent.getStringExtra(ApCameraUtil.Generic.AP_CAMERA_DEFAULT_FROM_SCREEN_TAG) ?: ""

    override fun getCameraMode(): Int =
        this.getPlayLoadBundle()?.getInt(ApCameraConst.ApCameraMode.AP_CAMERA_CONST_MODE_NAME)
            ?: ApCameraConst.ApCameraMode.AP_CAMERA_VAL_IS_ONLY_CAMERA_APC_MODE

    override fun getImageUriFromIntentListString(): ArrayList<String> {
        return this.getPlayLoadBundle()
            ?.getStringArrayList(ApCameraConst.ApCameraPayload.AP_CAMERA_INPUT_IMAGE_PATH_LIST_CONST_NAME)
            ?: arrayListOf()
    }

    override fun getPlayLoadBundle(): Bundle? =
        intent.getBundleExtra(ApCameraConst.ApCameraPayload.AP_CAMERA_BUNDLE_PAYLOAD_CONST)

    //TODO START FOR INITIAL CONTRACT API
    private fun initAPMultiplePagerPreviewActivityContract() {
        this._apMultiPagerPreviewActContract =
            registerForActivityResult(ApMultiplePagerPreviewResultContract()) {
                val intent =
                    intent.putStringArrayListExtra(
                        ApCameraConst.ApCameraPayload.AP_CAMERA_OUTPUT_URI_STRING,
                        it
                    )
                setResult(RESULT_OK, intent)
                finish()
            }
    }

    private fun initPreviewTransitionContract() {
        this._previewTransitionContract =
            registerForActivityResult(ApJustPreviewResultContract(tag())) { previewUri ->
                previewUri?.let { uri ->
                    if (uri.isEmpty()) return@let
                    val fallbackUri = Uri.parse(uri)
                    currentPhotoUri = fallbackUri
                    currentPhotoUri?.let { latestUri ->
                        val fallbackIntent = Intent().apply {
                            putStringArrayListExtra(
                                ApCameraConst.ApCameraPayload.AP_CAMERA_OUTPUT_URI_STRING,
                                arrayListOf(latestUri.toString())
                            )
                        }
                        setResult(Activity.RESULT_OK, fallbackIntent)
                    }
                    finish()
                }
            }
    }

    private fun initPagerPreviewActivityContract() {
        this._pagerPreviewActivityContract =
            registerForActivityResult(ApPagerPreviewResultContract()) {}
    }

    private fun initPreviewActivityContract() {
        this._previewActivityContract = registerForActivityResult(ApPreviewResultContract()) {}
    }
    //TODO END FOR INITIAL CONTRACT API

    private fun observeCameraState(cameraInfo: CameraInfo) {
        cameraInfo.cameraState.observe(this) { cameraState ->
            run {
                when (cameraState.type) {
                    CameraState.Type.PENDING_OPEN -> {
                        // Ask the user to close other camera apps
                        Toast.makeText(
                            this@ApCameraActivity,
                            "CameraState: Pending Open",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    CameraState.Type.OPENING -> {
                        // Show the Camera UI
                        Toast.makeText(
                            this@ApCameraActivity,
                            "CameraState: Opening",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    CameraState.Type.OPEN -> {
                        // Setup Camera resources and begin processing
                        Toast.makeText(
                            this@ApCameraActivity,
                            "CameraState: Open",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    CameraState.Type.CLOSING -> {
                        // Close camera UI
                        Toast.makeText(
                            this@ApCameraActivity,
                            "CameraState: Closing",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    CameraState.Type.CLOSED -> {
                        // Free camera resources
                        Toast.makeText(
                            this@ApCameraActivity,
                            "CameraState: Closed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            cameraState.error?.let { error ->
                when (error.code) {
                    // Open errors
                    CameraState.ERROR_STREAM_CONFIG -> {
                        // Make sure to setup the use cases properly
                        Toast.makeText(
                            this@ApCameraActivity,
                            "Stream config error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Opening errors
                    CameraState.ERROR_CAMERA_IN_USE -> {
                        // Close the camera or ask user to close another camera app that's using the
                        // camera
                        Toast.makeText(
                            this@ApCameraActivity,
                            "Camera in use",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    CameraState.ERROR_MAX_CAMERAS_IN_USE -> {
                        // Close another open camera in the app, or ask the user to close another
                        // camera app that's using the camera
                        Toast.makeText(
                            this@ApCameraActivity,
                            "Max cameras in use",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    CameraState.ERROR_OTHER_RECOVERABLE_ERROR -> {
                        Toast.makeText(
                            this@ApCameraActivity,
                            "Other recoverable error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Closing errors
                    CameraState.ERROR_CAMERA_DISABLED -> {
                        // Ask the user to enable the device's cameras
                        Toast.makeText(
                            this@ApCameraActivity,
                            "Camera disabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    CameraState.ERROR_CAMERA_FATAL_ERROR -> {
                        // Ask the user to reboot the device to restore camera function
                        Toast.makeText(
                            this@ApCameraActivity,
                            "Fatal error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Closed errors
                    CameraState.ERROR_DO_NOT_DISTURB_MODE_ENABLED -> {
                        // Ask the user to disable the "Do Not Disturb" mode, then reopen the camera
                        Toast.makeText(
                            this@ApCameraActivity,
                            "Do not disturb mode enabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}
