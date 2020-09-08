package com.sws.oneonone.fragment

import android.annotation.TargetApi
import android.app.ProgressDialog
import android.content.res.Configuration
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.Nullable
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.otaliastudios.cameraview.VideoResult
import com.sws.oneonone.R
import com.sws.oneonone.`interface`.AwsResponse
import com.sws.oneonone.`interface`.AwsVideoResponse
import com.sws.oneonone.databinding.ActivityVideoPreviewBinding
import com.sws.oneonone.model.*
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.util.*
import com.sws.oneonone.videotrim.*
import com.vincent.videocompressor.VideoCompress
import com.vincent.videocompressor.VideoCompress.CompressListener
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


@Suppress("INACCESSIBLE_TYPE")
class VideoPreviewFragment: BaseFragment(),AwsResponse,AwsVideoResponse,
    NotificationCenter.NotificationCenterDelegate{
    private var binding: ActivityVideoPreviewBinding? = null
    private var videoResult: VideoResult? = null
    private var image: Bitmap? = null
    protected var videoFile: File? = null
    private var mProgressDialog: ProgressDialog? = null
    private var mStartPosition = 0
    private var mDuration = 0
    private var mTimeVideo = 0

    private var mEndPosition = 0
    private val mMaxDuration = 60
    private val mHandler = Handler()
    var dstFile: String? = null
    var isFromAccept = false
    var isChat = false
    var chatFragment: ChatMessageFragment? = null
    val service: ApiClient? = ApiClient()
    var challengeIdModel: ChallengeIdModel? = null
    var type = 0
    var outputDir = ""
    var inputPath = ""
    private var startTime: Long = 0
    private  var endTime:Long = 0
    var destPath = ""
    var chatUri: Uri? = null

  /*  fun setChatVideoResult(@Nullable result: VideoResult?, challengeIdModel: ChallengeIdModel?, chatFragment: ChatMessageFragment?, isChat: Boolean){
        videoResult = result
        this.challengeIdModel = challengeIdModel
        this.chatFragment = chatFragment
        this.isChat = isChat
    }

    fun setChatVideosUri(@Nullable videoFiles: File?, challengeIdModel: ChallengeIdModel?, chatFragment: ChatMessageFragment?, isChat: Boolean) {
        videoFile = videoFiles
        this.challengeIdModel = challengeIdModel
        this.chatFragment = chatFragment
        this.isChat = isChat
    }

    fun setChatImageResult(image: Bitmap?, challengeIdModel: ChallengeIdModel?, chatFragment: ChatMessageFragment?, isChat: Boolean) {
        this.image = image
        this.challengeIdModel = challengeIdModel
        this.chatFragment = chatFragment
        this.isChat = isChat
    }

*/

    fun setVideoResult(@Nullable result: VideoResult?, isFromAccept: Boolean,challengeIdModel: ChallengeIdModel?) {
        videoResult = result
        this.isFromAccept = isFromAccept
        this.challengeIdModel = challengeIdModel
    }

    fun setVideosUri(@Nullable videoFiles: File?,isFromAccept: Boolean,challengeIdModel: ChallengeIdModel?) {
        videoFile = videoFiles
        this.isFromAccept = isFromAccept
        this.challengeIdModel = challengeIdModel
    }


    fun setImageResult(image: Bitmap?,isFromAccept: Boolean,challengeIdModel: ChallengeIdModel?) {
        this.image = image
        this.isFromAccept = isFromAccept
        this.challengeIdModel = challengeIdModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationBarColor.blackNotificationBar(activity)
        outputDir = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityVideoPreviewBinding.bind(
            inflater.inflate(
                R.layout.activity_video_preview,
                container,
                false
            )
        )
        binding!!.lifecycleOwner = this
      //  checkChallenge()
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.trimAcceptedVideo)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val result = videoResult
        val displaymetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displaymetrics)
        val screenWidth: Int = displaymetrics.widthPixels
        val screenHeight: Int = displaymetrics.heightPixels
        Log.d("screenWidth++", screenWidth.toString());
        Log.d("screenHeight++", screenHeight.toString())

        if (screenHeight >= 2000) {
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 150, 0, 200)
            binding!!.llshow.layoutParams = params;
        }

        if (result == null && videoFile == null) {
            binding!!.image.visibility = View.VISIBLE
            binding!!.video.visibility = View.GONE
            if (image != null) {
                binding!!.image.setImageBitmap(image)
            }
        } else if (videoFile != null) {
            binding!!.rlTrim.visibility = View.VISIBLE
            dstFile = (activity.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
                .toString() + "/" + Date().time
                    + Utility.VIDEO_FORMAT)
            binding!!.timeLineView.post {
                setBitmap(Uri.fromFile(videoFile)!!)
                binding!!.video.setVideoURI(Uri.fromFile(videoFile))
            }

            binding!!.rlplayIcon.visibility = View.VISIBLE

            // handle your range seekbar changes
            binding!!.timeLineBar.addOnRangeSeekBarListener(object : OnRangeSeekBarChangeListener {
                override fun onCreate(
                    customRangeSeekBarNew: CustomRangeSeekBar?,
                    index: Int,
                    value: Float
                ) {
                    // Do nothing
                }

                override fun onSeek(
                    customRangeSeekBarNew: CustomRangeSeekBar?,
                    index: Int,
                    value: Float
                ) {
                    onSeekThumbs(index, value)
                }

                override fun onSeekStart(
                    customRangeSeekBarNew: CustomRangeSeekBar?,
                    index: Int,
                    value: Float
                ) {

                    mHandler.removeCallbacks(mUpdateTimeTask)
                    binding!!.seekBarVideo.progress = 0
                    binding!!.video.seekTo(mStartPosition * 1000)
                    binding!!.video.pause()
                        binding!!.imgPlay.setBackgroundResource(R.drawable.ic_white_play)

                }

                override fun onSeekStop(customRangeSeekBarNew: CustomRangeSeekBar?, index: Int, value: Float) {
                }
            })

            binding!!.video.setOnPreparedListener { mp ->

                mDuration = binding!!.video.duration / 1000
                setSeekBarPosition()
            }

        } else {
            binding!!.image.visibility = View.GONE
            binding!!.video.visibility = View.VISIBLE
            binding!!.video.setVideoURI(Uri.fromFile(result!!.file))

        }


        // handle changes on seekbar for video play
        binding!!.seekBarVideo.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar,
                i: Int,
                b: Boolean
            ) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                    mHandler.removeCallbacks(mUpdateTimeTask)
                    binding!!.seekBarVideo.max = mTimeVideo * 1000
                    binding!!.seekBarVideo.setProgress(0)
                    binding!!.video.seekTo(mStartPosition * 1000)
                    binding!!.video.pause()
                    binding!!.imgPlay.setBackgroundResource(R.drawable.ic_white_play)

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask)
                binding!!.video.seekTo(mStartPosition * 1000 - binding!!.seekBarVideo.getProgress())
            }
        })




        binding!!.video.setOnCompletionListener {
            // Implementation here.
            if(videoFile != null){
                onVideoCompleted()
            }else{
                binding!!.video.start()
            }

        }
        binding!!.imgPlay.setOnClickListener {
            if (binding!!.video.isPlaying) {
                    binding!!.video.pause()
                    binding!!.imgPlay.setBackgroundResource(R.drawable.ic_white_play)

            } else {

                    binding!!.video.start()
                    binding!!.imgPlay.setBackgroundResource(R.drawable.ic_white_pause)
                    if (binding!!.seekBarVideo.progress == 0) {
                        updateProgressBar()
                    }

            }
        }


        binding!!.ivSend.setOnClickListener {
            var vPath: String? = ""
            var iPath: String? = ""
            if (videoResult != null) {
                vPath = videoResult?.file?.absolutePath
            } else if (videoFile != null) {
                val diff = mEndPosition - mStartPosition
                if (diff < 3) {
                    Toast.makeText(
                        activity, getString(R.string.video_length_validation),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val mediaMetadataRetriever =
                        MediaMetadataRetriever()
                    mediaMetadataRetriever.setDataSource(
                        activity,
                        Uri.fromFile(videoFile)
                    )

                    //notify that video trimming started
                    if (mOnVideoTrimListener != null) mOnVideoTrimListener.onTrimStarted()
                    BackgroundTask.execute(
                        object : BackgroundTask.Task("", 0L, "") {
                            override fun execute() {
                                try {
                                    Utility.startTrim(
                                        videoFile!!,
                                        dstFile!!,
                                        (mStartPosition * 1000).toLong(),
                                        (mEndPosition * 1000).toLong(),
                                        mOnVideoTrimListener
                                    )
                                } catch (e: Throwable) {
                                    Thread.getDefaultUncaughtExceptionHandler()
                                        .uncaughtException(Thread.currentThread(), e)
                                }
                            }
                        }
                    )
                }
            }


            if(isFromAccept){
                if (result == null && videoFile == null) {
                    type = 1
                    Loader.showLoader(activity)
                    val name = UUID.randomUUID().toString().toUpperCase()
                    val picturePath: File? = AndroidUtilities().getSavedBitmapPath(name, activity, image!!)
                    UploadImageOnAWS.uploadAcceptChallengeImage(activity, picturePath!!.absolutePath, this, "images/")


                } else if(result != null ){
                   compressVideo()
                    type = 2
                   /* UploadVideoOnAWS.uploadVideo(activity, videoResult!!.file.absolutePath, this,videoResult!!.file)
                    Utils.showToast(activity,"check",false)*/
                }
            }else  {
                if (isChat){
                    if(chatFragment != null){
                      //  chatFragment?.selected30SecVideo(vPath, image, videoResult?.file)
                        if (chatUri != null ) {
                            val file = File(chatUri?.path)
                            onBackPressed()
                            chatFragment?.selected30SecVideo(vPath, image, file)
                        }
                    }
                } else {
                    val fragment = NewChallengeFragment()
                    fragment.setResult(vPath, image, videoResult?.file)
                    activity.replaceFragment(fragment)
                }
            }

        }

        binding!!.ivClose.setOnClickListener {
            DiscardPopup().discardDialog(binding!!.rootLayout, activity)
        }
    }

    override fun onPause() {
        super.onPause()
        if (binding!!.video.isPlaying) {
            binding!!.video.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!binding!!.video.isPlaying) {
            if(videoFile == null)
            binding!!.video.start()
        }
    }




    override fun onDestroy() {
        super.onDestroy()
        setVideoResult(null,false,null)

    }


    var mOnVideoTrimListener: OnVideoTrimListener = object : OnVideoTrimListener {
        override fun onTrimStarted() {

        }

        override fun getResult(uri: Uri) {
            chatUri = uri
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                if(isFromAccept){
                    type = 2
                    Loader.showLoader(activity)
                    compressVideo()
                   /* Utils.showToast(activity,"trimm",false)
                    NotificationCenter.getInstance()
                        .postNotificationName(NotificationCenter.trimAcceptedVideo, uri)*/
                }else{
                    NotificationCenter.getInstance()
                        .postNotificationName(NotificationCenter.trimVideo, uri)
                }


            }


        }

        override fun cancelAction() {
            mProgressDialog!!.dismiss()
        }

        override fun onError(message: String?) {
            mProgressDialog!!.dismiss()
        }
    }

    private fun setBitmap(mVideoUri: Uri) {
        binding!!.timeLineView.setVideo(mVideoUri)
    }


    private fun onSeekThumbs(index: Int, value: Float) {
        when (index) {
            BarThumb.LEFT -> {
                mStartPosition = ((mDuration * value / 100L).roundToInt())
                binding!!.video.seekTo(mStartPosition * 1000)
            }
            BarThumb.RIGHT -> {
                mEndPosition = ((mDuration * value / 100L).roundToInt())
            }
        }
        mTimeVideo = mEndPosition - mStartPosition
       binding!!.seekBarVideo.setMax(mTimeVideo * 1000)
        binding!!.seekBarVideo.setProgress(0)
        binding!!.video.seekTo(mStartPosition * 1000)
        var mStart: String = mStartPosition.toString() + ""
        if (mStartPosition < 10) mStart = "0$mStartPosition"
        var mEnd: String = mEndPosition.toString() + ""
        if (mEndPosition < 10) mEnd = "0$mEndPosition"

    }


    private val mUpdateTimeTask: Runnable = object : Runnable {
        override fun run() {
            if (binding!!.seekBarVideo.progress >= binding!!.seekBarVideo.getMax()) {
                binding!!.seekBarVideo.progress = binding!!.video.currentPosition - mStartPosition * 1000

                binding!!.video.seekTo(mStartPosition * 1000)
                binding!!.video.pause()
                binding!!.seekBarVideo.progress = 0
                binding!!.imgPlay.setBackgroundResource(R.drawable.ic_white_play)
            } else {
                binding!!.seekBarVideo.progress = binding!!.video.currentPosition - mStartPosition * 1000
                mHandler.postDelayed(this, 100)
            }
        }
    }

    private fun setSeekBarPosition() {
        if (mDuration >= mMaxDuration) {
            mStartPosition = 0
            mEndPosition = mMaxDuration
            binding!!.timeLineBar.setThumbValue(0, (mStartPosition * 100 / mDuration).toFloat())
            binding!!.timeLineBar.setThumbValue(1, (mEndPosition * 100 / mDuration).toFloat())
        } else {
            mStartPosition = 0
            mEndPosition = mDuration
        }
        mTimeVideo = mDuration
        binding!!.timeLineBar.initMaxWidth()
        binding!!.seekBarVideo.max = mMaxDuration * 1000
        binding!!.video.seekTo(mStartPosition * 1000)
        var mStart = mStartPosition.toString() + ""
        if (mStartPosition < 10) mStart = "0$mStartPosition"
        var mEnd = mEndPosition.toString() + ""
        if (mEndPosition < 10) mEnd = "0$mEndPosition"
    }

    private fun onVideoCompleted() {
        mHandler.removeCallbacks(mUpdateTimeTask)
        binding!!.seekBarVideo.progress = 0
        binding!!.video.seekTo(mStartPosition * 1000)
        binding!!.video.pause()
       binding!!.imgPlay.setBackgroundResource(R.drawable.ic_white_play)
    }
    fun updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100)
    }
/*
    fun checkChallenge() {

        try {
            val rootObject = JsonObject()


            try {
                rootObject.addProperty("userId", AppStaticData().getModel()?.result?.id)
                Log.d("userId", AppStaticData().getModel()?.result?.id)
            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient().create(ApiInterface::class.java)
            val call = apiService.checkChallenge(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<SuccessModel> {
                override fun onResponse(
                    call: Call<SuccessModel>,
                    response: Response<SuccessModel>
                ) {
                    val checkChelenge = response.body()!!
                    val checkResponse = AndroidUtilities().apisResponse(
                        activity!!,
                        checkChelenge.code!!,
                        checkChelenge.message
                    )
                    if (checkResponse) {
                        if (checkChelenge.code == 404) {
                            try {
                                isActiveChallenge = true
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        } else {
                            isActiveChallenge = false
                        }
                    }
                }

                override fun onFailure(call: Call<SuccessModel>, t: Throwable) {
                    Log.e("responseError", t.message)
                    Utils.showToast(activity, "Have some problem...", false)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }*/

    internal class AcceptChallengeAsyncTask(val fragment: VideoPreviewFragment, val awsUrl: String?, val thumbNail: String?): AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params:String):String {
            // your async action
            fragment.acceptChallenge(awsUrl,thumbNail)
            return ""
        }
        override fun onPostExecute(aVoid:String) {

        }
    }

    fun acceptChallenge(awsUrl : String?, thumbNail: String?) {

     /*   try {*/
            val rootObject = JsonObject()
            val challengerId = JsonArray()
            val hashtag = JsonArray()
            challengerId.add(challengeIdModel?.createdBy)
        if(challengeIdModel?.hashtags != null){
            for(hashtags in challengeIdModel?.hashtags!!){
                hashtag.add(hashtags)

            }
        }


            Log.d("VideoUrl","check")

            try {
                rootObject.addProperty("userId", AppStaticData().getModel()?.result?.id)
                rootObject.add("userTo", challengerId)
                rootObject.addProperty("type", type)
                rootObject.addProperty("image", if (type == 1) awsUrl else "")
                rootObject.addProperty("video", if (type == 2) awsUrl else "")
                rootObject.addProperty("thumbnails", thumbNail)
                rootObject.addProperty("challengeType", "2")
                rootObject.addProperty("title",challengeIdModel?.title)
                rootObject.addProperty("request", "A")
                rootObject.addProperty("uid", challengeIdModel?.uid)
                rootObject.add("hashtag",hashtag)
            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.createChallenge(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<CareateChallengeModel> {
                override fun onResponse(
                    call: Call<CareateChallengeModel>,
                    response: Response<CareateChallengeModel>
                ) {

                        try {
                            Loader.hideLoader(activity)
                            val acceptChallenge = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity,acceptChallenge.code,acceptChallenge.message)
                            if(checkResponse) {
                                if (acceptChallenge.code == 200) {
                                    activity.removeFragments(3)
                                    Log.d("VideoUrl", "success")
                                } else {
                                    Log.d("VideoUrl", acceptChallenge.message)
                                }
                            }

                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }

                }

                override fun onFailure(call: Call<CareateChallengeModel>, t: Throwable) {
                    Log.e("responseError", t.message)
                    Log.d("VideoUrl","fail")
                    Loader.hideLoader(activity)
                    Utils.showToast(activity, "Have some problem...", false)
                }
            })
       /* } catch (ex: Exception) {
            ex.printStackTrace()
        }*/
    }

    fun compressVideo(){
        if(videoResult != null){
            inputPath = UtilCompress.getFilePath(activity, Uri.fromFile(videoResult!!.file))
        }else if(videoFile != null){
            inputPath =  UtilCompress.getFilePath(activity, Uri.fromFile(videoFile))
        }
      //  inputPath = UtilCompress.getFilePath(activity, Uri.fromFile(videoFile))
        destPath = outputDir + File.separator + "VID_" + SimpleDateFormat("yyyyMMdd_HHmmss", getLocale()).format(Date()) + ".mp4"
        VideoCompress.compressVideoMedium(inputPath, destPath, object : CompressListener {
            override fun onStart() {
                startTime = System.currentTimeMillis()
                UtilCompress.writeFile(activity,
                    "Start at: " + SimpleDateFormat("HH:mm:ss", getLocale())
                        .format(Date()) + "\n"
                )
            }

            override fun onSuccess() {
                endTime = System.currentTimeMillis()
                UtilCompress.writeFile(activity,
                    "End at: " + SimpleDateFormat("HH:mm:ss", getLocale())
                        .format(Date()) + "\n"
                )
                UtilCompress.writeFile(activity,
                    """
                            Total: ${(endTime - startTime) / 1000}s
                            
                            """.trimIndent()
                )
                UtilCompress.writeFile(activity)
                Utils.showToast(activity,"Sucesss",false)
                getLinkForVideo()
            }

            override fun onFail() {
                endTime = System.currentTimeMillis()
                UtilCompress.writeFile(
                    activity,
                    "Failed Compress!!!" + SimpleDateFormat("HH:mm:ss", getLocale())
                        .format(Date())
                )

                Utils.showToast(activity,"Fail",false)

            }

            override fun onProgress(percent: Float) {
                //   tv_progress.setText("$percent%")
            }
        })
    }

    override fun onStateComplete(response: String?) {
        Loader.hideLoader(activity)
        Log.d("AWSURl", response!!)
            // AcceptChallengeAsyncTask(this, response,"").execute()
        acceptChallenge(response,"")
    }

    private fun getLocale(): Locale? {
        val config = resources.configuration
        var sysLocale: Locale? = null
        sysLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getSystemLocale(config)
        } else {
            getSystemLocaleLegacy(config)
        }
        return sysLocale
    }

    fun getSystemLocaleLegacy(config: Configuration): Locale? {
        return config.locale
    }

    @TargetApi(Build.VERSION_CODES.N)
    fun getSystemLocale(config: Configuration): Locale? {
        return config.locales[0]
    }

    override fun onStateVideoComplete(url: String?, thumbNail: String?) {
       // AcceptChallengeAsyncTask(this, url, thumbNail).execute()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.trimAcceptedVideo)
    }

    override fun didReceivedNotification(id: Int, vararg args: Any?) {
        if(id == NotificationCenter.trimAcceptedVideo){
            val trimVideo = args[0] as Uri
            val videoFile = File(trimVideo!!.path)
            UploadVideoOnAWS.uploadVideo(activity, videoFile.absolutePath, this,videoFile)
        }

    }


    fun getLinkForVideo() {
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
                            uploadLinkVideo(videoLink.result.uploadURL,videoLink.result.uid)
                        }
                    }catch (ex:Exception){

                    }


//                    Utils.showToast(activity, "Success..."+videoLink!!-+.success, false)
                }

                override fun onFailure(call: Call<VideoLInkModel>, t: Throwable) {
                    Log.e("responseError", t.message)
                    Utils.showToast(activity, "Have some problem...", false)
                }
            })

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun uploadLinkVideo(uploadUrl: String,uid:String) {

        try {


            val apiService = service!!.getClient(false,uploadUrl).create(ApiInterface::class.java)
            val videoFile = File(destPath)
            val  uri = Uri.fromFile(videoFile)
            val videofile: File = FilePath.from(activity, uri)
            val file =  File(videoFile.absolutePath)
            if(videoFile.isFile && videoFile.exists()){
                val  requestBody = RequestBody.create(MultipartBody.FORM, videoFile)
                val  fileToUpload = MultipartBody.Part.createFormData("file",videoFile.path, requestBody)
                val call = apiService.uploadVideo(fileToUpload)

                call.enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {

                        try {


                            if(response.code() == 200){
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


            val apiService = service!!.getClient(false,"https://api.cloudflare.com/client/v4/accounts/0f3a9f41d8b87e18512a14911083727d/stream/"+uid).create(ApiInterface::class.java)

            val call = apiService.getVideoUrl("Bearer  7rnozFZaUbEI9-BKhM_FQDv6eD4MZpEL1LPBafhS","7a8b3aea9a348be035f16fdce8564a2105a75","04.umesh@gmail.com")

            call.enqueue(object : Callback<CFVideoUrlModel> {
                override fun onResponse(
                    call: Call<CFVideoUrlModel>,
                    response: Response<CFVideoUrlModel>
                ) {




                    if(response.code() == 200){
                        val cfResposeModel = response.body()!!
                        Utils.showToast(activity, "sucess..", false)
                        Log.d("thumbnail",cfResposeModel.result.thumbnail)
                        Log.d("playback",cfResposeModel.result .playback.hls)
                        acceptChallenge(cfResposeModel.result .playback.hls,cfResposeModel.result.thumbnail)
                    }else{
                        Utils.showToast(activity, "error..."+response.message(), false)
                    }


                }

                override fun onFailure(call: Call<CFVideoUrlModel>, t: Throwable) {
                    Log.e("responseError", t.message)
                    Utils.showToast(activity, "Have some problem...", false)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }



}
