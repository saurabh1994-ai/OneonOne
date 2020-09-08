package com.sws.oneonone.fragment

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.sws.oneonone.R
import com.sws.oneonone.databinding.FragmentCameraUiBinding
import com.sws.oneonone.dialog.NotificationDialog
import com.sws.oneonone.dialog.ProfileBottomDialog
import com.sws.oneonone.model.SignUpModel
import com.sws.oneonone.util.*
import com.sws.oneonone.viewModel.CameraUIVM
import org.json.JSONObject
import java.io.File
import java.io.IOException

class CameraUIFragment: BaseFragment() {
    private var binding: FragmentCameraUiBinding? = null
    var cameraUIVM: CameraUIVM? = null
    var GALLERY = 1
    val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123
    var profileId:String? = null
    var notiData = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraUIVM = ViewModelProviders.of(this).get(CameraUIVM::class.java)

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    binding = FragmentCameraUiBinding.bind(inflater.inflate(R.layout.fragment_camera_ui, container, false))
        binding!!.lifecycleOwner = this
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //set data in UI
        val model: SignUpModel? = AppStaticData().getModel()
       ShowingImage.showImage(activity, model?.result?.profileImg, binding!!.userProfile)

        // set Activity
        cameraUIVM?.activity = activity
        // Atteched view Model data to xml object
        binding!!.cameraUIVM = cameraUIVM
        initView()

        if(notiData.isNotEmpty()){
            handlePushNotificaion(notiData)
        }
        if(!profileId.isNullOrEmpty()){
            val  videoBottomSheetDialog = ProfileBottomDialog.newInstance(activity,profileId,null,null)
            videoBottomSheetDialog.show(activity.supportFragmentManager, ProfileBottomDialog().TAG)

        }
        binding!!.openCamera.setOnClickListener {

            val fragment = CameraFragment()
            val arguments = Bundle()
            arguments.putString("comeFrom" , "CameraUIFragment")
            fragment.arguments = arguments
            activity.replaceFragment(fragment)
        }




        binding!!.ibNotification.setOnClickListener {
            activity.replaceFragment(NotificationFragment())
        }

        binding!!.snapChatVideos.setOnClickListener {
            if(isStoragePermissionGranted(activity) && checkPermissionREAD_EXTERNAL_STORAGE(activity)){

                choosePhotoFromGallary()

            }
        }

        binding!!.tiktokGallery.setOnClickListener {
            if(isStoragePermissionGranted(activity) && checkPermissionREAD_EXTERNAL_STORAGE(activity)){

                choosePhotoFromGallary()

            }
        }

    }
    private fun initView() {

        // check has Observers
        if (!cameraUIVM?.comman?.hasObservers()!!) {
            cameraUIVM?.comman?.observe(this, object : Observer<String> {
                override fun onChanged(@Nullable str: String) {
                    when (str) {
                        "back" -> onBackPressed()
                    }
                }
            })
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var valid = false
        for (grantResult in grantResults) {
            valid = valid && grantResult == PackageManager.PERMISSION_GRANTED
        }

        for (i in permissions.indices) {
            if (Manifest.permission.WRITE_EXTERNAL_STORAGE == permissions[i] ) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED    ) {
                    choosePhotoFromGallary()

                }else{
                    Toast.makeText(activity, "Permission is needed for gallery images",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    fun choosePhotoFromGallary() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
        galleryIntent.setDataAndType(null,"video/*")
        startActivityForResult(galleryIntent,
            GALLERY
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            return
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                val contentURI = data.data

                val videofile: File = FilePath.from(activity, contentURI)
                val fragment = VideoPreviewFragment()
                fragment.setVideosUri(videofile,false,null)
                activity.replaceFragment(fragment)



            }
        }
    }

    fun isStoragePermissionGranted(activity: BaseActivity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            true
        }
    }



    fun checkPermissionREAD_EXTERNAL_STORAGE(
        context: BaseActivity
    ): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)

                } else {

                        requestPermissions(
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
                }
                return false
            } else {
                return true
            }
        } else {
            return true
        }
    }

    fun showDialog(msg: String, context: Context,
                   permission: String) {
        val alertBuilder = AlertDialog.Builder(context)
        alertBuilder.setCancelable(true)
        alertBuilder.setTitle("Permission necessary")
        alertBuilder.setMessage("$msg permission is necessary")
        alertBuilder.setPositiveButton(android.R.string.yes
        ) { dialog, which ->
            ActivityCompat.requestPermissions(context as Activity,
                arrayOf(permission),
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
        }
        val alert = alertBuilder.create()
        alert.show()
    }

    companion object {



        fun newInstance(profileId: String?, notiData : String?) = CameraUIFragment().apply {
            this.profileId = profileId!!
            this.notiData = notiData!!


        }
    }





    fun getPath(uri: Uri?): String? {
        val projection =
            arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor =
           activity.contentResolver.query(uri!!, projection, null, null, null) ?: return null
        val column_index: Int =
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val s: String = cursor.getString(column_index)
        cursor.close()
        return s
    }

    private fun handlePushNotificaion(data:String?) {
        //type for each notification
        // type 1 for create challenge
        // type 2 for accept challenge
        //type 3 for decline challenge
        //type 4 for win challenge
        //type 5 for loose challenge
        //type 6 for like challenge
        //type 7 for comment on challenge
        //type 8 for vote on challenge
        //type 9 for following user

        try {
            val jsonObject = JSONObject(data!!)
            var notiData : JSONObject? = null
            var userId = ""
            var type = ""
            var uid = ""
            if(jsonObject.has("notifData"))
                notiData = jsonObject.getJSONObject("notifData")
            if (notiData!!.has("userId"))
                userId = notiData.getString("userId")
            if(notiData.has("type"))
                type = notiData.getString("type")

            if(notiData.has("uid"))
                uid = notiData.getString("uid")


            if(type == "1"){
                activity.replaceFragment(NotificationFragment())
            }else if(type == "2"){
                val myDialog = NotificationDialog.newInstance(activity,type,"")
                myDialog.show(activity.supportFragmentManager, "Hello Testing")
            }else if(type == "3"){
                val myDialog = NotificationDialog.newInstance(activity,type,"") // name key needed for this challenge
                myDialog.show(activity.supportFragmentManager, "Hello Testing")
            }else if(type == "4"){
                val myDialog = NotificationDialog.newInstance(activity,type,"")
                myDialog.show(activity.supportFragmentManager, "Hello Testing")
            }else if(type == "5"){
                val myDialog = NotificationDialog.newInstance(activity,type,"")
                myDialog.show(activity.supportFragmentManager, "Hello Testing")
            }else if(type == "6"||type == "7" || type == "8"){
                activity.replaceFragment(ViewExploreFragment.newInstance(uid))
            }else if(type == "9"){
                val  videoBottomSheetDialog = ProfileBottomDialog.newInstance(activity,userId,null,null)
                videoBottomSheetDialog.show(activity.supportFragmentManager, ProfileBottomDialog().TAG)
            }


        }catch (ex : Exception){

        }
    }
}

