package com.sws.oneonone.dialog

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sws.oneonone.R
import com.sws.oneonone.databinding.LayoutChallengeChooseBottomDialogBinding
import com.sws.oneonone.fragment.CameraFragment
import com.sws.oneonone.fragment.VideoPreviewFragment
import com.sws.oneonone.model.ChallengeIdModel
import com.sws.oneonone.util.AndroidUtilities
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.FilePath
import java.io.File


class ChallengeChooseBottomDialog : BottomSheetDialogFragment() {
    var binding: LayoutChallengeChooseBottomDialogBinding? = null
    val TAG = "ActionBottomDialog"
    var activity: BaseActivity? = null
    var GALLERY = 1
    val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123
    var isFromAccept = false
    var uiData: ChallengeIdModel? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.bind(
            inflater.inflate(
                R.layout.layout_challenge_choose_bottom_dialog,
                container,
                false
            )
        )



        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding!!.rlCamera.setOnClickListener {
            if(isFromAccept && uiData != null){
                val fragment = CameraFragment()
                val arguments = Bundle()
                arguments.putBoolean("isFromAccept" , isFromAccept )
                arguments.putParcelable("challengeId",uiData)
                fragment.arguments = arguments;
                activity!!.replaceFragment(fragment)
            }else
            activity!!.replaceFragment(CameraFragment())
            dismiss()
        }

        binding!!.tiktokGallery.setOnClickListener {
            if(isStoragePermissionGranted(activity!!) && checkPermissionREAD_EXTERNAL_STORAGE(activity!!)){

                choosePhotoFromGallary()

            }

        }

        binding!!.snapChatVideos.setOnClickListener {
            if(isStoragePermissionGranted(activity!!) && checkPermissionREAD_EXTERNAL_STORAGE(activity!!)){

                choosePhotoFromGallary()

            }

        }

        binding!!.tvSnapchat.setOnClickListener {
            //dismiss()
            try {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://snapchat.com/add")
                )
               intent.setPackage("com.snapchat.android")
                activity!!.startActivity(intent)
            } catch (e: Exception) {
                AndroidUtilities().openAlertDialog("Can't be shared.Snapchat isn't Installed",activity!!)
            }
        }

    }

    companion object {

        fun newInstance(activity: BaseActivity,isFromAccept: Boolean, uiData: ChallengeIdModel? ) = ChallengeChooseBottomDialog().apply {
            this.activity = activity
            this.isFromAccept = isFromAccept
            this.uiData = uiData

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
                if(isFromAccept && uiData != null){
                    val videofile: File = FilePath.from(activity, contentURI)
                    val fragment = VideoPreviewFragment()
                    fragment.setVideosUri(videofile,true,uiData)
                    activity!!.replaceFragment(fragment)
                }else{
                    val videofile: File = FilePath.from(activity, contentURI)
                    val fragment = VideoPreviewFragment()
                    fragment.setVideosUri(videofile,false,uiData)
                    activity!!.replaceFragment(fragment)
                }


                dismiss()



            }
        }
    }


}