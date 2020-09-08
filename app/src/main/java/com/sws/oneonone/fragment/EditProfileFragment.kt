package com.sws.oneonone.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import com.sws.oneonone.R
import com.sws.oneonone.`interface`.AwsResponse
import com.sws.oneonone.choosePhoto.OpenCameraDialog
import com.sws.oneonone.databinding.FragmentEditProfileBinding
import com.sws.oneonone.model.SignUpModel
import com.sws.oneonone.model.ToolbarModel
import com.sws.oneonone.util.*
import com.sws.oneonone.viewModel.EditProfileVM
import com.sws.oneonone.viewModel.ToolbarVM
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class EditProfileFragment: BaseFragment(), AwsResponse {

    private var binding: FragmentEditProfileBinding? = null
    var toolbarVM: ToolbarVM? = null
    var editProfileVM: EditProfileVM? = null
    var pref: PreferenceStore? = null
    var model: SignUpModel? = null

    private var GALLERY = 1
    private var CAMERA = 2
    private var OPTION = 3
    private var IMAGE_DIRECTORY = "/1On1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationBarColor.WhiteNotificationBar(activity)
        toolbarVM = ViewModelProviders.of(this).get(ToolbarVM::class.java)
        editProfileVM = ViewModelProviders.of(this).get(EditProfileVM::class.java)
        pref = PreferenceStore.getInstance()
        // 1On1 App All Dala get from Shared Prefrences
        val gson = Gson()
        val json = pref?.getStringData("OneOnOne")
        model = gson.fromJson(json, SignUpModel::class.java)

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentEditProfileBinding.bind(inflater.inflate(R.layout.fragment_edit_profile, container, false))
        binding!!.lifecycleOwner = this
        return binding!!.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //set Image in imageView
        ShowingImage.showImage(activity, model?.result?.profileImg, binding!!.ivProfile)

        val toolbarModel = ToolbarModel(View.VISIBLE, activity?.getString(R.string.edit_profile) ,View.GONE,View.VISIBLE,View.GONE,View.GONE, 1, "","",View.GONE)
        binding!!.layoutToolbar.toolbarModel = toolbarModel
        binding!!.layoutToolbar.lifecycleOwner = activity

        // ToolBar ViewModel
        toolbarVM?.activity = activity
        binding!!.layoutToolbar.toolbarVM = toolbarVM

        // Edit Profile ViewModel
        editProfileVM?.activity = activity
        editProfileVM?.fullName?.value = model?.result?.fullName
        editProfileVM?.userName?.value = model?.result?.username
        editProfileVM?.email?.value = model?.result?.email
        editProfileVM?.imageUrl?.value = model?.result?.profileImg
        if (model?.result?.gender.equals("M")) {
            editProfileVM?.gender?.value = "Male"
        } else if (model?.result?.gender.equals("F")){
            editProfileVM?.gender?.value = "Female"
        } else {
            editProfileVM?.gender?.value = "Other"
        }
        binding!!.editProfileVM = editProfileVM

        initView()
    }
    private fun initView() {
        // check has Observers
        if (!toolbarVM?.comman?.hasObservers()!!) {
            toolbarVM?.comman?.observe(this, object : Observer<String> {
                override fun onChanged(@Nullable str: String) {
                    when (str) {
                        "onBack" -> onBackPressed()
                    }
                }
            })
        }
        if (!editProfileVM?.comman?.hasObservers()!!) {
            editProfileVM?.comman?.observe(this, object : Observer<String> {
                override fun onChanged(@Nullable str: String) {
                    when (str) {
                        "open_camera" -> openPermission()
                        "back" -> onBackPressed()
                    }
                }
            })
        }
    }


    fun openPermission() {
        // Check if we're running on Android 5.0 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Call some material design APIs here
            if (ContextCompat.checkSelfPermission(
                    ApplicationLoader.applicationContext!!,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) + ContextCompat
                    .checkSelfPermission(
                        ApplicationLoader.applicationContext!!,
                        Manifest.permission.CAMERA
                    )
                != PackageManager.PERMISSION_GRANTED
            ) {
                activity.requestPermissions(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), OPTION
                )
            } else {
                captureImage()
            }
        } else {
            // Implement this feature without material design
            captureImage()
        }
    }

    fun captureImage() {
        val dialogImages = OpenCameraDialog()
        dialogImages.setActvity(activity,false)
        dialogImages.setListener(object : OpenCameraDialog.CustomItemListener {
            override fun onTakeVideoClicked() {

            }

            override fun onTakeTextViewClicked() {
                dialogImages.dismiss()
                takePhotoFromCamera()
            }

            override fun onUploadTextViewClicked() {
                dialogImages.dismiss()
                choosePhotoFromGallary()
            }
        })
        dialogImages.show(activity.supportFragmentManager, "")
    }


    fun choosePhotoFromGallary() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent,
            GALLERY
        )
    }

    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA) {
            takePhotoFromCamera()
        } else if (requestCode == GALLERY) {
            choosePhotoFromGallary()
        } else {
            openPermission()
        }
    }

    private fun saveImage(thumbnail: Bitmap): String? {
        val bytes = ByteArrayOutputStream()
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val wallpaperDirectory = File(
            activity.externalCacheDir.toString() + IMAGE_DIRECTORY
        )
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs()
        }
        try {
            val f = File(
                wallpaperDirectory, Calendar.getInstance()
                    .getTimeInMillis().toString() + ".jpg"
            )
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(
                activity,
                arrayOf(f.getPath()),
                arrayOf("image/jpeg"), null
            )
            fo.close()
            return f.getAbsolutePath()
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        return ""
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            return
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                val contentURI = data.data
                try {
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    val cursor = activity?.contentResolver?.query(
                        contentURI!!,
                        filePathColumn, null, null, null
                    )
                    cursor?.moveToFirst()
                    val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
                    val picturePath = cursor?.getString(columnIndex!!)
                    UploadImageOnAWS.uploadImage(activity, picturePath!!, this)

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else if (requestCode == CAMERA) {
            val thumbnail = data!!.extras!!.get("data") as Bitmap
            // Save image in External storage
            val picturePath: String? = saveImage(thumbnail)
            UploadImageOnAWS.uploadImage(activity, picturePath!!, this)
        }
    }


    override fun onStateComplete(response: String?) {
        editProfileVM?.imageUrl?.value = response
        ShowingImage?.showImage(activity, response, binding!!.ivProfile)
    }
}