package com.sws.oneonone.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.FirebaseError
import com.google.firebase.database.*
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.`interface`.AwsResponse
import com.sws.oneonone.`interface`.AwsVideoResponse
import com.sws.oneonone.adapter.ChatMessageAdapter
import com.sws.oneonone.choosePhoto.OpenCameraDialog
import com.sws.oneonone.databinding.FragmentChatMessageBinding
import com.sws.oneonone.firebaseModel.ChatUserModel
import com.sws.oneonone.firebaseModel.UserModel
import com.sws.oneonone.model.CFVideoUrlModel
import com.sws.oneonone.model.VideoLInkModel
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.util.*
import com.sws.oneonone.viewModel.ChatMessageVM
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*

class ChatMessageFragment() : BaseFragment(), AwsResponse, AwsVideoResponse {
    private var binding: FragmentChatMessageBinding? = null

    private var GALLERY = 1
    private var CAMERA = 4
    private var VGALLERY = 2
    private var OPTION = 3
    private var IMAGE_DIRECTORY = "/1On1"
    private var contentURI: Uri? = null

    var service: ApiClient? = ApiClient()
    private var mFirebaseDatabase: DatabaseReference? = null
    private var mFirebaseInstance: FirebaseDatabase? = null

    var chatMessageVM: ChatMessageVM? = null
    private var chatWith: String? = null
    private var chatKey: String? = null
    private var userModel: UserModel? = null
    var adapter: ChatMessageAdapter? = null
    var msgList: ArrayList<ChatUserModel>? = null
    var duplicateMsgList: ArrayList<ChatUserModel>? = null


    fun setChatWithUserId(@Nullable chatKey: String?, chatWith: String?, userModel: UserModel?) {
        this.chatKey = chatKey
        this.chatWith = chatWith
        this.userModel = userModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatMessageVM = ViewModelProviders.of(this).get(ChatMessageVM::class.java)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity?.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        activity?.getWindow().setStatusBarColor(ContextCompat.getColor(activity,R.color.white));// set status background white
        binding = FragmentChatMessageBinding.bind(inflater.inflate(R.layout.fragment_chat_message, container, false))
        binding!!.setLifecycleOwner(this)

        if(!userModel?.getUserImg().isNullOrEmpty()){
            ShowingImage?.showImage(activity!!, userModel?.getUserImg(), binding!!.profileImage)
        } else {
            binding!!.profileImage.setImageResource(R.drawable.avtar_icon)
        }
        return binding!!.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.ToolBarLayout?.setOnClickListener {
            if (chatMessageVM?.isGroup?.value!!){
                val fragment = GroupViewFragment()
                fragment?.setGroupDetails(chatKey, chatWith, userModel)
                activity!!.replaceFragment(fragment)
            }
        }

        binding!!.lifecycleOwner = activity
        // Chat User ViewModel
        chatMessageVM?.activity = activity
        chatMessageVM?.chatWith?.value = chatWith
        chatMessageVM?.chatKey?.value = chatKey
        chatMessageVM?.groupMembers = userModel?.getMembers()
        val yourArray: List<String> = chatKey!!.split("_")
        if (yourArray?.size  > 2) {
            initializeFirebase()
            chatMessageVM?.isGroup?.value = true
            chatMessageVM?.chatLastSeen?.value = "Tap to here for group info"
            setMemberNameTitle(userModel)

        } else {
            chatMessageVM?.isGroup?.value = false
            chatMessageVM?.chatLastSeen?.value = userModel?.getLastSeen()
        }
        chatMessageVM?.chatName?.value = userModel?.getUserName()

        val lastSeen: Long? = Utils().getMilliFromDate(userModel?.getLastSeen())
        chatMessageVM?.chatLastSeen?.value = Utils().convertDate(lastSeen)

        binding!!.chatMessageVM = chatMessageVM
        binding!!.chatMessageVM?.showingList()

        msgList = ArrayList<ChatUserModel>()
        duplicateMsgList = ArrayList<ChatUserModel>()
        var layoutManager = LinearLayoutManager(activity!!, LinearLayoutManager.VERTICAL, false)
        layoutManager?.setStackFromEnd(true)
        binding!!.rvGroupList.layoutManager = layoutManager
        binding!!.rvGroupList.setHasFixedSize(true)
        //binding!!.rvGroupList.isNestedScrollingEnabled = false
        adapter = ChatMessageAdapter(activity, msgList, userModel?.getUserImg())
        binding!!.rvGroupList.adapter = adapter

        initView()

        binding!!.back?.setOnClickListener {
            onBackPressed()
        }
        binding!!.ivCamera?.setOnClickListener {
            val fragment = CameraFragment()
            fragment.chatMessageData(this, true)
            val arguments = Bundle()
            arguments.putString("comeFrom" , "ChatMessageFragment")
            fragment.arguments = arguments
            activity.replaceFragment(fragment)
            //openPermission()
        }
    }

    private fun initView() {
        // check has Observers
        if (!chatMessageVM?.comman?.hasObservers()!!) {
            chatMessageVM?.comman?.observe(this, object : Observer<ArrayList<ChatUserModel>> {
                override fun onChanged(@Nullable modelList: ArrayList<ChatUserModel>) {
                    msgList?.clear()
                    msgList?.addAll(modelList)
                    binding!!.rvGroupList?.smoothScrollToPosition(adapter?.itemCount!!)
                    adapter?.notifyDataSetChanged()
                }
            })
        }
    }


   /* fun openPermission() {
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
                activity?.requestPermissions(
                    arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
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
*/
    fun captureImage() {
        val dialogImages = OpenCameraDialog()
        dialogImages.setActvity(activity, true)
        dialogImages.setListener(object : OpenCameraDialog.CustomItemListener {
            override fun onTakeVideoClicked() {
                dialogImages.dismiss()
                takeVideoFromCamera()
            }
            override fun onTakeTextViewClicked() {
                dialogImages.dismiss()
               // takePhotoFromCamera()
                takeVideoFromGallery()
            }
            override fun onUploadTextViewClicked() {
                dialogImages.dismiss()
                choosePhotoFromGallary()
            }
        })
        dialogImages.show(activity.supportFragmentManager, "")
    }

    private fun takeVideoFromCamera() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(intent, VGALLERY)
    }


    fun choosePhotoFromGallary() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, GALLERY)
    }

    private fun takeVideoFromGallery() {
        var galleryIntent = Intent(Intent.ACTION_PICK,
            android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, VGALLERY);
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == VGALLERY) {
            takeVideoFromGallery()
        } else if(requestCode == VGALLERY) {
            takeVideoFromCamera()
        } else if (requestCode == GALLERY) {
            choosePhotoFromGallary()
        } else {
           // openPermission()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            return
        }
        if (requestCode == GALLERY) {
            // photo from gallery
            if (data != null) {
                contentURI = data.data
                try {
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    val cursor = activity?.getContentResolver()?.query(
                        contentURI!!,
                        filePathColumn, null, null, null
                    )
                    cursor?.moveToFirst()
                    val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
                    val picturePath = cursor?.getString(columnIndex!!)
                    UploadImageOnAWS?.uploadChatImage(activity, picturePath!!, this)

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else if (requestCode == VGALLERY) {
            if (data != null) {
                contentURI = data.data

                val videofile: File = FilePath.from(activity, contentURI)
                val fragment = VideoPreviewFragment()
               // fragment.setChatVideoURI(videofile, this, true)
                activity.replaceFragment(fragment)

                //val selectedVideoPath: String? = getPath(contentURI!!)
                //UploadVideoOnAWS?.uploadVideo(activity, selectedVideoPath!!, this)
            }
        } else if(requestCode == CAMERA){
            //PHOTO selection
            if (data != null) {
                contentURI = data?.data
                val videofile: File = FilePath.from(activity, contentURI)
                val fragment = VideoPreviewFragment()
                //fragment.setChatVideoURI(videofile, this, true)
                activity.replaceFragment(fragment)

                // var picturePath: String? = getPath(contentURI!!)
               // UploadImageOnAWS?.uploadChatImage(activity, picturePath!!, this)
            }
        }
    }

    override fun onStateComplete(response: String?) {
        chatMessageVM?.sendImage(response)
    }

    override fun onStateVideoComplete(url: String?, thumbnail: String?) {
        Log.e("checkVideoData", url+" \n "+ thumbnail)
        chatMessageVM?.sendVideo(url, thumbnail)
        // chatMessageVM?.sendImage(response)
    }

    /*fun getPath(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = activity.contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            val column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } else
            return null
    }
*/

    // Seleceted Video
    fun selected30SecVideo(videoPath: String?, image: Bitmap?, videoFile: File?){
        Log.e("allFileFPrint", videoPath+"\n"+image+"\n"+ videoFile)
        if (videoFile != null) {
            Loader?.showLoader(activity)
            getLinkForVideo(videoFile)
        } else if (image != null){
            val name = UUID.randomUUID().toString().toUpperCase()
            val picturePath: File? = AndroidUtilities().getSavedBitmapPath(name, activity, image!!)
            if (picturePath!!.exists()) {
                UploadImageOnAWS?.uploadChatImage(activity, picturePath?.absolutePath, this)
            }

        }
    }

    fun getImageUri(inImage:Bitmap):String? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(getActivity()?.getContentResolver(), inImage, "OneOnOne", null)
        return path //Uri.parse(path)
    }
    fun getLinkForVideo(videoFile: File?) {
        val rootObject = JsonObject()
        try {

            rootObject.addProperty("maxDurationSeconds", 60)
            val apiService = service!!.getClient(true,"").create(ApiInterface::class.java)
            val call = apiService.getLinkForVideo("7a8b3aea9a348be035f16fdce8564a2105a75","04.umesh@gmail.com", rootObject)
            call.enqueue(object : Callback<VideoLInkModel> {
                override fun onResponse(
                    call: Call<VideoLInkModel>,
                    response: Response<VideoLInkModel>
                ) {

                    try {
                        val videoLink = response.body()!!
                        if(videoLink.success){
                            Log.d("playback"," Pass 1")
                            uploadLinkVideo(videoLink.result.uploadURL,videoLink.result.uid, videoFile)
                        }
                    }catch (ex:Exception){

                    }


//                    Utils.showToast(activity, "Success..."+videoLink!!-+.success, false)
                }

                override fun onFailure(call: Call<VideoLInkModel>, t: Throwable) {
                    Log.e("responseError", t.message)
                    Loader?.hideLoader(activity)
                    Utils.showToast(activity, "Have some problem...", false)
                }
            })

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun uploadLinkVideo(uploadUrl: String,uid:String, videoFile: File?) {

        try {
            val apiService = service!!.getClient(false,uploadUrl).create(ApiInterface::class.java)
            if(videoFile!!.isFile && videoFile?.exists()){
                val  requestBody = RequestBody.create(MultipartBody.FORM, videoFile)
                val  fileToUpload = MultipartBody.Part.createFormData("file",videoFile?.path, requestBody)
                val call = apiService.uploadVideo(fileToUpload)

                call.enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {

                        try {


                            if(response.code() == 200){
                                Log.d("playback"," Pass 2")
                                Utils.showToast(activity, "sucess..", false)
                                getvideoUrlFromServer(uid)
                            }else{
                                Utils.showToast(activity, "error..."+response.message(), false)
                            }
                        }catch (ex:Exception){
                            Log.e("exceptionError",ex.message)
                        }

                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("responseError", t.message)
                        Loader?.hideLoader(activity)
                        Utils.showToast(activity, "Have some problem...", false)
                    }
                })
            }else{
                Utils.showToast(activity, "file does not exist", false)
            }


        } catch (ex: Exception) {

            ex.printStackTrace()
        }
    }


    fun getvideoUrlFromServer(uid: String){
        try {


            val apiService = service!!.getClient(false,"https://api.cloudflare.com/client/v4/accounts/0f3a9f41d8b87e18512a14911083727d/stream/"+uid).create(
                ApiInterface::class.java)

            val call = apiService.getVideoUrl("Bearer  7rnozFZaUbEI9-BKhM_FQDv6eD4MZpEL1LPBafhS","7a8b3aea9a348be035f16fdce8564a2105a75","04.umesh@gmail.com")

            call.enqueue(object : Callback<CFVideoUrlModel> {
                override fun onResponse(
                    call: Call<CFVideoUrlModel>,
                    response: Response<CFVideoUrlModel>
                ) {
                    if(response.code() == 200){
                        Loader?.hideLoader(activity)
                        val cfResposeModel = response.body()!!
                        Utils.showToast(activity, "sucess..", false)
                        Log.d("thumbnail",cfResposeModel.result.thumbnail)
                        Log.d("playback",cfResposeModel.result .playback.hls)
                        if (!cfResposeModel.result.thumbnail?.isNullOrEmpty()) {
                            chatMessageVM?.sendVideo(
                                cfResposeModel.result.playback.hls,
                                cfResposeModel.result.thumbnail
                            )
                        }
                       // createChallenge(cfResposeModel.result .playback.hls,cfResposeModel.result.thumbnail)
                    }else{
                        Utils.showToast(activity, "error..."+response.message(), false)
                    }


                }

                override fun onFailure(call: Call<CFVideoUrlModel>, t: Throwable) {
                    Loader?.hideLoader(activity)
                    Log.e("responseError", t.message)
                    Utils.showToast(activity, "Have some problem...", false)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }









    //  initialize firebase database
    fun initializeFirebase() {
        mFirebaseInstance = FirebaseDatabase.getInstance()
        mFirebaseDatabase = mFirebaseInstance?.getReference("Users")
        // check internet connection
        if (Utils.isNetworkAvailable(activity!!)) {
            val scoresRef = FirebaseDatabase.getInstance().getReference("Users")
            scoresRef.keepSynced(false)
        } else {
            val scoresRef = FirebaseDatabase.getInstance().getReference("Users")
            scoresRef.keepSynced(true)
        }
    }

    fun setMemberNameTitle(modelMember: UserModel?){
        var titleText: String? = ""
        for(i in 0..(modelMember?.getMembers()?.size!! -1)){
            var keyUser: String? =  modelMember?.getMembers()?.get(i)
            mFirebaseInstance = FirebaseDatabase.getInstance()
            // get reference to 'users' node
            mFirebaseDatabase = mFirebaseInstance?.getReference("Users")

            var mDatabase = FirebaseDatabase.getInstance().getReference()
            mDatabase.child("Users").child(keyUser!!)
                .addValueEventListener(object: ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        //for (childDataSnapshot in dataSnapshot.getChildren()) {
                        val user = dataSnapshot.getValue(UserModel::class.java)
                        // var user: UserModel? = childDataSnapshot.getValue(UserModel::class.java)
                        if (user != null) {
                            val userName: String? = user?.getUserName()
                            if (i == 0) {
                                titleText = userName
                                chatMessageVM?.chatLastSeen?.value = titleText
                            } else {
                                titleText = titleText +","+userName
                                chatMessageVM?.chatLastSeen?.value = titleText
                            }


                            // }
                        }
                        fun onCancelled(firebaseError: FirebaseError) {
                            //Error
                        }
                    }
                })
        }
    }

}