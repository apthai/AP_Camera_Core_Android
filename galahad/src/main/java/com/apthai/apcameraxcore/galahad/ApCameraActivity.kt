package com.apthai.apcameraxcore.galahad

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.apthai.apcameraxcore.common.ApCameraBaseActivity
import com.apthai.apcameraxcore.galahad.databinding.ActivityApCameraBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ApCameraActivity : ApCameraBaseActivity<ApCameraViewModel>(), ApCameraNavigator, View.OnClickListener {

    companion object {

        private const val REQUEST_CODE_PERMISSIONS = 112
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    override fun tag(): String = ApCameraActivity::class.java.simpleName

    private var activityApCameraBinding: ActivityApCameraBinding? = null
    private val binding get() = activityApCameraBinding

    private var apCameraViewModel: ApCameraViewModel? = null

    private var imageCapture: ImageCapture? = null

    lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityApCameraBinding = ActivityApCameraBinding.inflate(layoutInflater)
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

    override fun initial() {
        if (isCameraPermissionsGranted()){
            startCamera()
        }else{
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        binding?.apCameraViewCaptureButton?.setOnClickListener(this)

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun isCameraPermissionsGranted(): Boolean = REQUIRED_PERMISSIONS.all { permission ->
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider : ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding?.apCameraViewPreview?.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            }catch (exception : Exception){
                Toast.makeText(this, exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun takePhoto() {
        TODO("Not yet implemented")
    }

    override fun onClick(p0: View?) {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}