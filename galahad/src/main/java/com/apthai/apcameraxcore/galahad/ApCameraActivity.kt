package com.apthai.apcameraxcore.galahad

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaActionSound
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.apthai.apcameraxcore.common.ApCameraBaseActivity
import com.apthai.apcameraxcore.common.utils.ImageUtil
import com.apthai.apcameraxcore.galahad.databinding.ActivityGalahadCameraBinding
import com.apthai.apcameraxcore.galahad.previewer.contract.ApPreviewResultContract
import com.apthai.apcameraxcore.galahad.tools.ApCameraToolMainActivityResultContract
import com.bumptech.glide.Glide
import com.google.common.util.concurrent.ListenableFuture
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class ApCameraActivity :
    ApCameraBaseActivity<ApCameraViewModel>(),
    ApCameraNavigator,
    View.OnClickListener,
    ImageCapture.OnImageSavedCallback {

    companion object {

        private const val REQUEST_CODE_PERMISSIONS = 112
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
        private const val MIME_IMAGE_TYPE = "image/jpeg"
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

    override fun setUpView() {}

    @SuppressLint("ClickableViewAccessibility")
    override fun initial() {
        if (isCameraPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
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
        val fileName = getFileName()
        val contentValues = getContentValue(fileName)
        val outputOptions = getOutputFileOption(contentValues)
        playShutterSound()
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), this)
    }

    override fun takePhotoWithOutSave() {
        val imageCapture = currentCameraImageCapture ?: return

        imageCapture.takePicture(ContextCompat.getMainExecutor(this),
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
            })
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

    private fun getFileName(): String =
        SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())

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
                launchPreviewPhotoActivity()
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

    override fun initializeImageAnalysis(): ImageAnalysis = ImageAnalysis.Builder().build()

    override fun initializeImageCapture(): ImageCapture = ImageCapture.Builder()
        .setTargetAspectRatio(cameraAspectRatio)
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build().also { imgCapture ->
            imgCapture.flashMode = cameraFlashMode
        }

    override fun getContentValue(outputFileName: String): ContentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, outputFileName)
        put(MediaStore.MediaColumns.MIME_TYPE, MIME_IMAGE_TYPE)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ApCamera-Image")
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
                            val autoFocusPoint = factory.createPoint(motionEvent.x, motionEvent.y)
                            try {
                                animateAutofocusEvent(motionEvent.x, motionEvent.y)
                                currentCamera?.cameraControl?.startFocusAndMetering(
                                    FocusMeteringAction.Builder(
                                        autoFocusPoint,
                                        FocusMeteringAction.FLAG_AF
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

    private val apCameraToolMainActResultContract =
        registerForActivityResult(ApCameraToolMainActivityResultContract()) {

        }
}
