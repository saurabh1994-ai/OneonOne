package com.sws.oneonone.fragment

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.Nullable
import com.otaliastudios.cameraview.VideoResult
import com.sws.oneonone.R
import com.sws.oneonone.`interface`.AwsResponse
import com.sws.oneonone.`interface`.AwsVideoResponse
import com.sws.oneonone.databinding.ActivityVideoPreviewBinding
import com.sws.oneonone.model.ChallengeIdModel
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.util.*
import com.sws.oneonone.videotrim.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import kotlin.math.roundToInt


@Suppress("INACCESSIBLE_TYPE")
class ChatVideoPreviewFragment: BaseFragment(),AwsResponse,AwsVideoResponse,
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
    var chatUri: Uri? = null

    fun setChatVideoResult(@Nullable result: VideoResult?, challengeIdModel: ChallengeIdModel?, chatFragment: ChatMessageFragment?, isChat: Boolean){
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
            var file: File? = null
            if (videoResult != null) {
                vPath = videoResult?.file?.absolutePath
                file = videoResult?.file
            } else if (videoFile != null) {
                val diff = mEndPosition - mStartPosition
                if (diff < 3) {
                    Toast.makeText(activity, getString(R.string.video_length_validation), Toast.LENGTH_LONG).show()
                } else {
                    val mediaMetadataRetriever = MediaMetadataRetriever()
                    mediaMetadataRetriever.setDataSource(activity, Uri.fromFile(videoFile))

                    //notify that video trimming started
                    if (mOnVideoTrimListener != null) mOnVideoTrimListener.onTrimStarted()
                    BackgroundTask.execute(
                        object : BackgroundTask.Task("", 0L, "") {
                            override fun execute() {
                                try {
                                    Utility.startTrim(videoFile!!, dstFile!!, (mStartPosition * 1000).toLong(), (mEndPosition * 1000).toLong(), mOnVideoTrimListener)
                                } catch (e: Throwable) {
                                    Thread.getDefaultUncaughtExceptionHandler()
                                        .uncaughtException(Thread.currentThread(), e)
                                }
                            }
                        }
                    )
                }
            }


            if(chatFragment != null){
                if (!dstFile.isNullOrEmpty()){
                    val file = File(dstFile)
                    onBackPressed()
                    chatFragment?.selected30SecVideo(vPath, image, file)
                }else if (videoFile != null ) {
                    val file = File(videoFile?.path)
                    onBackPressed()
                    chatFragment?.selected30SecVideo(vPath, image, file)
                } else if(vPath != null){
                   onBackPressed()
                   chatFragment?.selected30SecVideo(vPath, image, file)
               } else {
                   onBackPressed()
                   chatFragment?.selected30SecVideo(vPath, image, null)
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
     //   setVideoResult(null,false,null)

    }


    var mOnVideoTrimListener: OnVideoTrimListener = object : OnVideoTrimListener {
        override fun onTrimStarted() {

        }

        override fun getResult(uri: Uri) {
            chatUri = uri

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

    override fun onStateComplete(response: String?) {
        Loader.hideLoader(activity)
        Log.d("AWSURl", response!!)
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
          //  UploadVideoOnAWS.uploadVideo(activity, videoFile.absolutePath, this,videoFile)
        }

    }


//    uploadLinkVideo(videoLink.result.uploadURL,videoLink.result.uid)

}
