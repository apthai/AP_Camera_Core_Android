package com.apthai.apcameraxcore.galahad.previewer

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.apthai.apcameraxcore.common.ApCameraBaseActivity
import com.apthai.apcameraxcore.galahad.ApCameraUtil
import com.apthai.apcameraxcore.galahad.databinding.ActivityGalahadMultipleBinding
import com.apthai.apcameraxcore.galahad.model.ApCameraImage
import java.io.File

class ApMultiplePreviewActivity : ApCameraBaseActivity<ApMultiplePreviewViewModel>(),
    ApMultiplePreviewNavigator {

    override fun tag(): String = ApMultiplePreviewActivity::class.java.simpleName

    companion object{

        const val AP_MULTIPLE_PREVIEW_PAYLOAD = "ap_multiple_preview_payload"
        fun getInstance(context : Context, imagePath : String) = Intent(context, ApMultiplePreviewActivity::class.java).apply {
            putExtra(AP_MULTIPLE_PREVIEW_PAYLOAD, imagePath)
        }
    }

    private var activityGalahadMultipleBinding: ActivityGalahadMultipleBinding? = null
    private val binding get() = activityGalahadMultipleBinding

    private var viewModel: ApMultiplePreviewViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityGalahadMultipleBinding = ActivityGalahadMultipleBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        viewModel = ViewModelProvider.NewInstanceFactory().create(ApMultiplePreviewViewModel::class.java)
        viewModel?.setNavigator(this)

        if (savedInstanceState == null){
            setUpView()
            initial()
        }
    }

    override fun setUpView() {
        setSupportActionBar(binding?.apMultiplePreviewToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun initial() {
        /*val dirFile = File(externalMediaDirs(Environment.DIRECTORY_PICTURES), ApCameraUtil.General.AP_CAMERA_DEFAULT_IMAGE_PATH)
        if (!dirFile.isDirectory){
            return
        }
        val allFiles = dirFile.listFiles()
        if (allFiles.isEmpty()){
            return
        }
        Toast.makeText(this, "Discovered ${allFiles.size}", Toast.LENGTH_SHORT).show()*/

        val imageList = mutableListOf<ApCameraImage>()

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            MediaStore.Images.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} ASC"

        val query = contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )
        query?.use { cursor->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

            while (cursor.moveToNext()){
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val contentUri : Uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                )
                if (name.contains(packageName.toString())){
                    imageList += ApCameraImage(contentUri, name)
                }
            }
        }
        Toast.makeText(this, "ImageList size = ${imageList.size}", Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}