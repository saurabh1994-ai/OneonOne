package com.sws.oneonone.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.otaliastudios.cameraview.*
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Flash
import com.otaliastudios.cameraview.controls.Hdr
import com.otaliastudios.cameraview.controls.Mode
import com.sws.oneonone.R
import com.sws.oneonone.choosePhoto.OpenImageorVideo
import com.sws.oneonone.databinding.FragmentCameraDesignBinding
import com.sws.oneonone.model.ChallengeIdModel
import com.sws.oneonone.util.BaseFragment
import com.sws.oneonone.util.FilePath
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CameraFragment: BaseFragment() {

    private var binding: FragmentCameraDesignBinding? = null
    var timer: CountDownTimer? = null
    // To show stuff in the callback
    private var mCaptureTime: Long = 0
    private var buttonTag: Int = 0
    private var image: WeakReference<PictureResult>? = null
    val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123
    // Flash light
    var isFlash: Boolean = false
    var isRearCamera: Boolean = true
    var isChat: Boolean = false
    var isFromAccept = false
    private val TYPE_IMAGE = 1
    private val TYPE_VIDEO = 2
    var chatFragment: ChatMessageFragment? = null
    var mCurrentPhotoPath:String? = null
    var GALLERY = 1
    var VIDEO = 2
    var challengeIdModel: ChallengeIdModel? = null

    fun chatMessageData(@Nullable chatFragment: ChatMessageFragment?, isChat: Boolean){
        this.chatFragment = chatFragment
        this.isChat = isChat
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        binding = FragmentCameraDesignBinding.bind(inflater.inflate(R.layout.fragment_camera_design, container, false))


        try {
            val arguments: Bundle? = arguments
            isFromAccept = arguments?.getBoolean("isFromAccept",false)!!
            challengeIdModel = arguments.getParcelable("challengeId")

        }catch (ex : Exception){

        }


        binding!!.ivClose.setOnClickListener {
            onBackPressed()
        }
        binding!!.ivTurnCamera.setOnClickListener {
            toggleCamera()
        }

        binding!!.rlGallery.setOnClickListener {
            //  choosePhotoFromGallary()
            chooserDialog()
        }

        binding!!.cameraFlash.setOnClickListener {
            when(buttonTag){
                0->{
                    buttonTag = 2
                    isFlash = true
                    binding!!.camera.flash = Flash.ON
                    binding!!.cameraFlash.setImageResource(R.drawable.flash_sel_icon) //ic_flash_on_white_24dp
                }
                1->{
                    buttonTag = 2
                    isFlash = true
                    binding!!.camera.flash = Flash.AUTO
                    binding!!.cameraFlash.setImageResource(R.drawable.flash_sel_icon) // ic_flash_auto_white_24dp
                }
                2->{
                    buttonTag = 0
                    isFlash = false
                    binding!!.camera.flash = Flash.OFF
                    binding!!.cameraFlash.setImageResource(R.drawable.flash_icon) //ic_flash_off_white_24dp
                }
            }
        }



        binding!!.camera.addCameraListener( Listener())

        binding!!.takePhoto.setOnTouchListener(object : OnSwipeTouchListener(activity) {

            override fun onSingleTap() {
                if(isFlash){
                    binding!!.camera.flash = Flash.ON
                }else{
                    binding!!.camera.flash = Flash.OFF
                }

                binding!!.camera.mode = Mode.PICTURE; // for pictures
                capturePicture()
            }

            override fun onLongPress() {
                binding!!.camera.mode = Mode.VIDEO // for video
                cancelTimer()
                if (isFlash && isRearCamera) {
                    binding!!.camera.hdr = Hdr.ON
                    binding!!.camera.flash = Flash.TORCH
                }

                binding!!.cameraFlash.visibility = View.GONE
                buttonTag = 1

                captureVideo()



            }

            override fun onRelease() {
                binding!!.camera.stopVideo()
                binding!!.camera.hdr = Hdr.ON
                binding!!.camera.flash = Flash.OFF
                cancelTimer()
                binding!!.cameraFlash.visibility = View.VISIBLE
                buttonTag = 0
                binding!!.takePhoto.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.circle_icon))
                binding!!.progressBar.visibility = View.GONE

            }
        })


        if(isStoragePermissionGranted() && checkPermissionREAD_EXTERNAL_STORAGE(activity)){

            getAllShownImagesPath(activity)

        }

        val displaymetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displaymetrics)
        val screenWidth: Int = displaymetrics.widthPixels
        val screenHeight: Int = displaymetrics.heightPixels
        Log.d("screenWidth++",screenWidth.toString());
        Log.d("screenHeight++",screenHeight.toString())

        if(screenHeight >= 2000){
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 150, 0, 300)
            binding!!.rlCameraview.layoutParams = params;
        }

        return binding!!.root
    }

    fun startTimer() {
        timer = object:CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.e("check_timer", (millisUntilFinished / 1000).toString())


                val hms = String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                    ),
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                    )
                )

                val timeunits: List<String> =
                    hms.split(":") //will break the string up into an array

                val minutes = timeunits[0].toLong() //first element
                val seconds = timeunits[1].toLong() //second element

                var mins = ""
                var scnds = ""
                if (minutes < 10) {
                    mins = "0$minutes"
                } else {
                    mins = minutes.toString()
                }



                if (seconds < 10) {
                    scnds = "0$seconds"
                } else {
                    scnds = seconds.toString()
                }

                binding?.timer?.text =  mins + ":" + scnds
            }


            override fun onFinish() {
                Log.e("check_timer", "Done")
                binding!!.camera.stopVideo()

            }
        }.start()
    }

    fun cancelTimer(){
        if(timer != null){
            timer?.cancel()
        }
    }


    override fun onBackPressed() {
        if (activity.supportFragmentManager.backStackEntryCount > 0) {
            fragmentManager!!.popBackStack()
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        } else
            activity.finish()
    }




    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var valid = false
        for (grantResult in grantResults) {
            valid = valid && grantResult == PackageManager.PERMISSION_GRANTED
        }
        if (valid && !binding!!.camera.isOpened) {
            binding!!.camera.open()
        }

        for (i in permissions.indices) {
            if (Manifest.permission.WRITE_EXTERNAL_STORAGE == permissions[i]) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED ) {
                    getAllShownImagesPath(activity)
                }else{
                    Toast.makeText(activity, "Permission is needed for gallery images",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    fun checkPermissionREAD_EXTERNAL_STORAGE(
        context: Context
    ): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(
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
        ) { _, _ ->
            requestPermissions(
                arrayOf(permission),
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
        }
        val alert = alertBuilder.create()
        alert.show()
    }

    override fun onResume() {
        super.onResume()
        binding!!.camera.open()
    }

    override fun onPause() {
        super.onPause()
        cancelTimer()
        binding!!.camera.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding!!.camera.destroy()
    }

    private fun toggleCamera() {
        if (binding!!.camera.isTakingPicture || binding!!.camera.isTakingVideo) return
        when (binding!!.camera.toggleFacing()) {
            Facing.BACK -> {
                isRearCamera = true
            }
            Facing.FRONT -> {
                isRearCamera = false

            }
        }
    }



    private open inner class Listener : CameraListener() {
        override fun onCameraOpened(options: CameraOptions) {


        }

        override fun onCameraError(exception: CameraException) {
            super.onCameraError(exception)
        }

        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
            if ( binding!!.camera.isTakingVideo) {
                return
            }

            // This can happen if picture was taken with a gesture.
            val callbackTime = System.currentTimeMillis()
            if (mCaptureTime == 0L) mCaptureTime = callbackTime - 300

            result.toBitmap { bitmap ->
                val thumbnail = bitmap
                //   val imagePath = saveImage(thumbnail!!)

                if (isChat){
                    onBackPressed()
                    val fragment = ChatVideoPreviewFragment()
                    fragment.setChatImageResult(thumbnail, challengeIdModel, chatFragment, isChat)
                    activity.replaceFragment(fragment)
                } else {
                    val fragment = VideoPreviewFragment()
                    fragment.setImageResult(thumbnail!!, isFromAccept, challengeIdModel)
                    activity.replaceFragment(fragment)
                }


            }
            mCaptureTime = 0
        }

        override fun onVideoTaken(result: VideoResult) {
            super.onVideoTaken(result)

            if (isChat){
                onBackPressed()
                val fragment = ChatVideoPreviewFragment()
                fragment.setChatVideoResult(result, challengeIdModel, chatFragment, isChat)
                activity.replaceFragment(fragment)
            } else {
                val fragment = VideoPreviewFragment()
                fragment.setVideoResult(result, isFromAccept, challengeIdModel)
                activity.replaceFragment(fragment)
            }

        }
    }

    private fun capturePicture() {

        if (binding!!.camera.mode == Mode.PICTURE) {

            mCaptureTime = System.currentTimeMillis()
            binding!!.camera.takePicture()
        }


    }


    private fun captureVideo() {
        if ( binding!!.camera.mode == Mode.VIDEO)
            binding!!.camera.takeVideo(createMediaFile(TYPE_VIDEO)!!, 30000)
        Handler().postDelayed({
            //Do something after 100ms
            binding!!.takePhoto.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.camera_icon_sel))
            binding!!.progressBar.visibility = View.VISIBLE
            startTimer()
        }, 1000)



    }





    internal open class OnSwipeTouchListener(ctx: Context?) :
        View.OnTouchListener {
        private val gestureDetector: GestureDetector
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val action: Int = event.actionMasked
            if (action == MotionEvent.ACTION_UP) {
                // do something here
                Log.i("TAG", "ActionUP:")
                onRelease()
            }
            return gestureDetector.onTouchEvent(event)
        }

        private inner class GestureListener :
            GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {


                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                Log.i("TAG", "onSingleTapConfirmed:")
                onSingleTap()

                return true
            }

            override fun onLongPress(e: MotionEvent) {
                Log.i("TAG", "onLongPress:")

                onLongPress()


            }

            override fun onDoubleTap(e: MotionEvent): Boolean {

                return true
            }

            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {

                return true
            }




        }


        open fun onSingleTap(){}
        open fun onLongPress(){}
        open fun onRelease(){}

        init {
            gestureDetector = GestureDetector(ctx, GestureListener())
        }
    }

    fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted")
                true
            } else {
                Log.v(TAG, "Permission is revoked")
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted")
            true
        }
    }

    fun getAllShownImagesPath(activity: Activity){
        val uriExternal: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor?
        var uriImage : Uri ? = null
        var scndImage : Uri? = null
        val projection = arrayOf(MediaStore.Images.Media._ID)
        var imageId: Long
        cursor = activity.contentResolver.query(uriExternal, projection, null, null, null)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                imageId = cursor.getLong(0)
                uriImage = Uri.withAppendedPath(uriExternal, "" + imageId)
                scndImage = Uri.withAppendedPath(uriExternal, "" + (imageId-1))
            }

            binding!!.ivImage2.setImageURI(uriImage)
            binding!!.ivImage.setImageURI(scndImage)
            cursor.close()
        }
    }

    @Throws(IOException::class)
    private fun createMediaFile(type: Int): File? {

        // Create an image file name
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val fileName =
            if (type == TYPE_IMAGE) "JPEG_" + timeStamp + "_" else "VID_" + timeStamp + "_"
        val storageDir = activity.getExternalFilesDir(
            if (type == TYPE_IMAGE) Environment.DIRECTORY_PICTURES else Environment.DIRECTORY_MOVIES
        )
        var file :File? = null
        if(isStoragePermissionGranted()) {
            file = File.createTempFile(
                fileName,  /* prefix */
                if (type == TYPE_IMAGE) ".jpg" else ".mp4",  /* suffix */
                storageDir /* directory */
            )
            // Get the path of the file created
            mCurrentPhotoPath = file.absolutePath
            Log.d(
                "path",
                "mCurrentPhotoPath: $mCurrentPhotoPath"
            )

        }


        return file
    }

    fun choosePhotoFromGallary() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent,
            GALLERY
        )
    }

    fun chooseVideoFromGallery(){
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
        galleryIntent.setDataAndType(null,"video/*")
        startActivityForResult(galleryIntent,
            VIDEO
        )
    }

    fun chooserDialog() {
        val dialogImages = OpenImageorVideo()
        dialogImages.setActvity(activity)
        dialogImages.setListener(object : OpenImageorVideo.CustomItemListener {
            override fun onTakeTextViewClicked() {
                dialogImages.dismiss()
                chooseVideoFromGallery()
            }

            override fun onUploadTextViewClicked() {
                dialogImages.dismiss()
                choosePhotoFromGallary()
            }
        })
        dialogImages.show(activity.supportFragmentManager, "")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            return
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                val contentURI = data.data
                val filepath = contentURI?.path
                var thumbnail = MediaStore.Images.Media.getBitmap(getActivity()?.getContentResolver(), contentURI)
                if(isChat){
                    onBackPressed()
                    val fragment = ChatVideoPreviewFragment()
                    fragment.setChatImageResult(thumbnail, challengeIdModel, chatFragment, isChat)
                    activity.replaceFragment(fragment)
                } else {
                    val fragment = VideoPreviewFragment()
                    fragment.setImageResult(thumbnail, isFromAccept, challengeIdModel)
                    activity.replaceFragment(fragment)
                }
               /* try {
                    val uriExternal: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    val cursor: Cursor?
                    var uriImage: Uri? = null
                    var scndImage: Uri? = null
                    val projection = arrayOf(MediaStore.Images.Media._ID)
                    var imageId: Long
                    cursor = activity.contentResolver.query(
                        contentURI!!,
                        projection,
                        null,
                        null,
                        null
                    )
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            imageId = cursor.getLong(0)
                            uriImage = Uri.withAppendedPath(uriExternal, "" + imageId)

                        }

                        var galleryBitmap: Bitmap?  = null
                        var thumbnail: Bitmap? = null
                        var exif: ExifInterface? = null
                        if (android.os.Build.VERSION.SDK_INT > 28) {
                            val bmOptions: BitmapFactory.Options = BitmapFactory.Options()
                            galleryBitmap = BitmapFactory.decodeFile(uriImage?.path, bmOptions)
                            thumbnail = galleryBitmap
                            //galleryBitmap = MediaStore.Images.Media.getBitmap(activity.contentResolver, uriImage)
                            try {
                                val exFile = File(uriImage!!.path)
                                exif = ExifInterface(exFile)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        } else {
                            val imageFile: File = FilePath.from(activity, contentURI)
                            galleryBitmap = MediaStore.Images.Media.getBitmap(activity.contentResolver, contentURI)
                            thumbnail = galleryBitmap
                            try {
                                exif = ExifInterface(imageFile.absolutePath)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }


                        var orientation = ExifInterface.ORIENTATION_NORMAL

                        if (exif != null)
                            orientation = exif.getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL
                            )

                        when (orientation) {
                            ExifInterface.ORIENTATION_ROTATE_90 -> galleryBitmap =
                                rotateBitmap(galleryBitmap, 90)
                            ExifInterface.ORIENTATION_ROTATE_180 -> galleryBitmap =
                                rotateBitmap(galleryBitmap, 180)

                            ExifInterface.ORIENTATION_ROTATE_270 -> galleryBitmap =
                                rotateBitmap(galleryBitmap, 270)
                        }


                         thumbnail = galleryBitmap
                        // val imagePath = saveImage(thumbnail!!)


                        if(isChat){
                            onBackPressed()
                            val fragment = ChatVideoPreviewFragment()
                            fragment.setChatImageResult(thumbnail, challengeIdModel, chatFragment, isChat)
                            activity.replaceFragment(fragment)
                        } else {
                            val fragment = VideoPreviewFragment()
                            fragment.setImageResult(thumbnail, isFromAccept, challengeIdModel)
                            activity.replaceFragment(fragment)
                        }

                    }

                } catch (e: IOException) {
                    Log.e("errorMessage", e.message)
                    e.printStackTrace()
                }*/
            }
        } else if(requestCode == VIDEO){
            if (data != null) {
                val contentURI = data.data

                val videofile: File = FilePath.from(activity, contentURI)

                if(isChat){
                    onBackPressed()
                    val fragment = ChatVideoPreviewFragment()
                    fragment.setChatVideosUri(videofile, challengeIdModel, chatFragment, isChat)
                    activity.replaceFragment(fragment)

                } else {
                    val fragment = VideoPreviewFragment()
                    fragment.setVideosUri(videofile, isFromAccept, challengeIdModel)
                    activity.replaceFragment(fragment)
                }


            }
        }
    }

    fun rotateBitmap(bitmap: Bitmap?, degrees: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}