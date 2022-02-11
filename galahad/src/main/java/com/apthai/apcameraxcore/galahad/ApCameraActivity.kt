package com.apthai.apcameraxcore.galahad

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.apthai.apcameraxcore.common.ApCameraBaseActivity
import com.apthai.apcameraxcore.galahad.databinding.ActivityGalahadCameraBinding
import com.google.common.util.concurrent.ListenableFuture
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ApCameraActivity : ApCameraBaseActivity<ApCameraViewModel>(), ApCameraNavigator,
    View.OnClickListener, ImageCapture.OnImageSavedCallback {

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
    private var cameraFlashMode: Int = ImageCapture.FLASH_MODE_OFF

    private val cameraRunnable: Runnable = Runnable {
        bindCamera()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityApCameraBinding = ActivityGalahadCameraBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        apCameraViewModel =
            ViewModelProvider.NewInstanceFactory().create(ApCameraViewModel::class.java)
        apCameraViewModel?.setNavigator(this)

        if (savedInstanceState == null) {
            setUpView()
            initial()
        }
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
                provider.bindToLifecycle(
                    this,
                    cameraFacing,
                    cameraPreview,
                    currentCameraImageAnalysis,
                    currentCameraImageCapture
                )
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
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), this)
    }

    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
        val msg = "Photo capture succeeded: ${outputFileResults.savedUri}"
        Toast.makeText(this@ApCameraActivity, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onError(exception: ImageCaptureException) {
        Toast.makeText(this@ApCameraActivity, exception.message, Toast.LENGTH_SHORT)
            .show()
    }

    override fun flipCameraFacing() {
        if (cameraFacing == CameraSelector.DEFAULT_BACK_CAMERA) cameraFacing =
            CameraSelector.DEFAULT_FRONT_CAMERA
        else if (cameraFacing == CameraSelector.DEFAULT_FRONT_CAMERA) cameraFacing =
            CameraSelector.DEFAULT_BACK_CAMERA
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
        startCamera()
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

    override fun initializePreviewSurface(): Preview = Preview.Builder().build().also { preview ->
        preview.setSurfaceProvider(binding?.apCameraViewPreview?.surfaceProvider)
    }

    override fun initializeImageAnalysis(): ImageAnalysis = ImageAnalysis.Builder().build()

    override fun initializeImageCapture(): ImageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build().also { imgCapture ->
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
}