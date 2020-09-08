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
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.sws.oneonone.R
import com.sws.oneonone.`interface`.AwsResponse
import com.sws.oneonone.adapter.CheckChatChildAdapter
import com.sws.oneonone.choosePhoto.OpenCameraDialog
import com.sws.oneonone.databinding.FragmentNewGroupBinding
import com.sws.oneonone.firebase.NotificationHandle
import com.sws.oneonone.firebaseModel.ChatUserModel
import com.sws.oneonone.firebaseModel.GroupModel
import com.sws.oneonone.model.ChildModel
import com.sws.oneonone.model.SignUpModel
import com.sws.oneonone.model.ToolbarModel
import com.sws.oneonone.util.*
import com.sws.oneonone.viewModel.ToolbarVM
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class NewGroupNameFragment: BaseFragment(), AwsResponse {

    private var binding: FragmentNewGroupBinding? = null
    // firebase access details
    private var mFirebaseDatabase: DatabaseReference? = null
    private var mFirebaseInstance: FirebaseDatabase? = null

    // declaired shared-preference
    var pref: PreferenceStore? = null

    // app data from shared-preference
    var model: SignUpModel? = null
    var toolbarVM: ToolbarVM? = null
    var groupKey: String? = ""
    var groupImage: String? = ""

    private var GALLERY = 1
    private var CAMERA = 2
    private var OPTION = 3
    private var IMAGE_DIRECTORY = "/1On1"

    // selected item array list
    var mList: ArrayList<ChildModel> =  ArrayList<ChildModel>()
    var idList: ArrayList<String> =  ArrayList<String>()
    fun setSelectedList(@Nullable list: ArrayList<ChildModel>) {
        mList = list
        if(mList != null){

            for(i in 0..(mList?.size-1)){
                groupKey = groupKey+"_"+mList?.get(i)?.getId()
                idList?.add(mList?.get(i)?.getId()!!)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarVM = ViewModelProviders.of(this).get(ToolbarVM::class.java)
        pref = PreferenceStore.getInstance()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        NotificationBarColor?.WhiteNotificationBar(activity)
        binding = FragmentNewGroupBinding.bind(inflater.inflate(R.layout.fragment_new_group, container, false))
        binding!!.tvParticipet?.text = mList?.size.toString()+ " Participants"
        binding!!.setLifecycleOwner(this)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val  json: String? = pref?.getStringData("OneOnOne")
        var gson = Gson()
        model = gson.fromJson(json, SignUpModel::class.java)


        // Tool bar model
        val toolbarModel: ToolbarModel = ToolbarModel(View.VISIBLE,"New Group", View.GONE,View.VISIBLE,View.VISIBLE,View.VISIBLE, 1, "CREATE", "",View.GONE)
        binding!!.layoutToolbar.toolbarModel = toolbarModel
        binding!!.layoutToolbar.lifecycleOwner = this
        // ToolBar ViewModel
        toolbarVM?.activity = activity
        binding!!.layoutToolbar.toolbarVM = toolbarVM

        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL

        binding!!.rvNewChat.layoutManager = layoutManager
        binding!!.rvNewChat.adapter = CheckChatChildAdapter(activity, mList,  false, false)

        binding!!.ivSelectImage?.setOnClickListener {
            openPermission()
        }

        initView()
    }


    private fun initView() {
        // check has Observers
        if (!toolbarVM?.comman?.hasObservers()!!) {
            toolbarVM?.comman?.observe(this, object : Observer<String> {
                override fun onChanged(@Nullable str: String) {
                    when (str) {
                        "onBack" -> {
                            val fragment = NewGroupFragment()
                            onBackPressed()
                            activity?.replaceFragment(fragment)
                        }
                        "next" -> {
                            val text: String? = binding!!.etGroupName?.text.trim().toString()
                            if(!text.isNullOrEmpty()){
                                if(!groupKey.isNullOrEmpty()) {
                                    //add group admin id
                                    idList?.add(model?.result?.id!!)
                                    //create group chat details model
                                    val mModel: GroupModel? = GroupModel()
                                    mModel?.setFmcToken(AppStaticData?.getFmcToken())
                                    mModel?.setIsTyping(false)
                                    mModel?.setLastSeen(Utils?.currentTime())
                                    mModel?.setGroupImg(groupImage)
                                    mModel?.setGroupName(text)
                                    mModel?.setCreatedBy(model?.result?.id)
                                    mModel?.setMembers(idList)
                                    createGroupChat(model?.result?.id+groupKey, mModel)
                                }
                            } else {
                                Toast.makeText(activity!!, "Please enter group name!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            })
        }
    }

    fun createGroupChat(key: String?,  model: GroupModel?) {
        mFirebaseInstance = FirebaseDatabase.getInstance()
        mFirebaseDatabase = mFirebaseInstance?.getReference("ChatGroup")
        if (Utils.isNetworkAvailable(activity!!)) {
            val scoresRef = FirebaseDatabase.getInstance().getReference("ChatGroup")
            scoresRef.keepSynced(false)
        } else {
            val scoresRef = FirebaseDatabase.getInstance().getReference("ChatGroup")
            scoresRef.keepSynced(true)
        }
        mFirebaseDatabase?.child(key!!)?.setValue(model)

        val msgModel: ChatUserModel? = ChatUserModel()
        createMessageInChat(key, msgModel)
        val list = model?.getMembers()
        var senderName = AppStaticData().getModel()?.result?.fullName
        for(i in 0..(list?.size!! - 1)){
            if(!model?.getCreatedBy().equals(list?.get(i))) {

                NotificationHandle?.send(senderName,
                    model?.getGroupName() + " new group Created!",
                    list?.get(i)
                )
            }
        }
    }

    fun createMessageInChat(key: String?, model: ChatUserModel?){
        mFirebaseInstance = FirebaseDatabase.getInstance()
        mFirebaseDatabase = mFirebaseInstance?.getReference("Chat")
        if (Utils.isNetworkAvailable(activity!!)) {
            val scoresRef = FirebaseDatabase.getInstance().getReference("Chat")
            scoresRef.keepSynced(false)
        } else {
            val scoresRef = FirebaseDatabase.getInstance().getReference("Chat")
            scoresRef.keepSynced(true)
        }
        //mFirebaseDatabase?.child(key!!)?.setValue(model)


        val Autokey: String? =  mFirebaseInstance?.getReference("Chat")?.child(key!!)?.push()?.key
        mFirebaseDatabase?.child(key!!)?.child(Autokey!!)?.setValue(model)

        onBackPressed()
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
        dialogImages.setActvity(activity, false)
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
                    UploadImageOnAWS.uploadImageForGroupChat(activity, picturePath!!, this)

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else if (requestCode == CAMERA) {
            val thumbnail = data!!.extras!!.get("data") as Bitmap
            // Save image in External storage
            val picturePath: String? = saveImage(thumbnail)
            UploadImageOnAWS.uploadImageForGroupChat(activity, picturePath!!, this)
        }
    }


    override fun onStateComplete(response: String?) {
        groupImage = response
        ShowingImage?.showImage(activity, response, binding!!.ivGroupImage)
    }

}