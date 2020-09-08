package com.sws.oneonone.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.os.postDelayed
import androidx.databinding.DataBindingUtil
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.databinding.DialogPreviewBinding
import com.sws.oneonone.fragment.ViewExploreFragment
import com.sws.oneonone.model.*
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.util.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class PreviewBottomDialog : BottomSheetDialogFragment(){
    var binding: DialogPreviewBinding? = null
    var videoUri : Uri? = null
    var image: Bitmap? = null
    var exploreResult : ArrayList<ExploreResult>? = null
    private var cacheDataSourceFactory: CacheDataSourceFactory? = null
    private var simpleExoPlayer: SimpleExoPlayer? = null
    private var simpleCache: SimpleCache? = null
    val TAG = "ActionBottomDialog"
    var service: ApiClient? = ApiClient()
    var activieChallenge:ActivieChallenge? = null
    var activity:BaseActivity? = null
    var pos = 0
    private val handler: Handler = Handler()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog{
        val bottomSheetDialog =
            super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dialog ->
            val dialog = dialog as BottomSheetDialog
            val bottomSheet: FrameLayout? =
                dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<View?>(bottomSheet).state =
                BottomSheetBehavior.STATE_EXPANDED
            BottomSheetBehavior.from<View?>(bottomSheet).skipCollapsed = true
            BottomSheetBehavior.from<View?>(bottomSheet).isHideable = true
        }
        if(exploreResult != null ){
            if(!exploreResult!![0].isView!!){
                addViews()
            }

        }else if(activieChallenge != null){
            if(!activieChallenge?.isView!!){
                addViews()
            }
        }

        return bottomSheetDialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.bind(inflater.inflate(R.layout.dialog_preview, container, false))
        activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(videoUri != null){
            binding!!.video.setVideoURI(videoUri)
            binding!!.video.start()

        }

        if(image != null){
            binding!!.video.visibility = View.GONE
            binding!!.image.visibility = View.VISIBLE
            binding!!.videoUrl.visibility = View.GONE
            binding!!.image.setImageBitmap(image)
        }

        if(exploreResult != null ){
            if (exploreResult!![pos].video!!.isNotEmpty()) {
                binding!!.video.visibility = View.VISIBLE
                binding!!.image.visibility = View.VISIBLE
                binding!!.videoUrl.visibility = View.GONE
                ShowingImage.showBannerImage(
                    activity!!,
                    exploreResult!![pos].thumbnails,
                    binding!!.image
                )
                binding!!.video.setVideoPath(exploreResult!![pos].video!!)
                binding!!.video.start()
                binding!!.video.setOnPreparedListener {
                    binding!!.image.visibility = View.GONE
                }
              //  initPlayer()
            } else {
                binding!!.video.visibility = View.VISIBLE
                binding!!.image.visibility = View.VISIBLE
                binding!!.videoUrl.visibility = View.GONE
                if (!exploreResult!![pos].image.isNullOrEmpty()) {
                    ShowingImage.showBannerImage(
                        activity!!,
                        exploreResult!![pos].image,
                        binding!!.image
                    )
                    handler.postDelayed(runnable, 5000)
                }


            }

        }else if(activieChallenge != null){
            if(activieChallenge!!.video.isNotEmpty()){
                binding!!.video.visibility = View.GONE
                binding!!.image.visibility = View.GONE
                binding!!.videoUrl.visibility = View.VISIBLE
                initPlayer()
            }else{
                binding!!.video.visibility = View.GONE
                binding!!.image.visibility = View.VISIBLE
                binding!!.videoUrl.visibility = View.GONE
                if(!activieChallenge?.image.isNullOrEmpty()){
                    ShowingImage.showBannerImage(activity!!, activieChallenge!!.image, binding!!.image)
                }
            }

        }




        binding!!.ivClose.setOnClickListener {
            dismiss()
        }


       binding!!.video.setOnCompletionListener {
           if(exploreResult != null ){
               setUI()

           }else{
               dismiss()
           }

       }

        binding!!.llshow.setOnTouchListener(object : ViewExploreFragment.OnSwipeTouchListener(activity) {


            override fun onSingleTap() {


                if(exploreResult != null ){
                    setUI()

                }else{
                    dismiss()
                }



            }


        })
    }

    private fun initPlayer() {
        simpleExoPlayer = context?.let {
            SimpleExoPlayer.Builder(it).build()
        }

        simpleCache = ApplicationLoader.simpleCache

        cacheDataSourceFactory = CacheDataSourceFactory(
            simpleCache,
            DefaultHttpDataSourceFactory(context?.let {
                Util.getUserAgent(
                    it, getString(
                        R.string.app_name
                    )
                )
            }),
            CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
        )
        var videoUri:Uri? = null
        if(exploreResult != null){
            if(exploreResult!![pos].video!!.isNotEmpty()){
                videoUri = Uri.parse(exploreResult!![pos].video)
            }
        }else if(activieChallenge != null){
            videoUri = Uri.parse(activieChallenge!!.video)
        }


        val mediaSource =
            ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(videoUri!!)

        binding!!.videoUrl.player = simpleExoPlayer
        simpleExoPlayer?.playWhenReady = true
        simpleExoPlayer?.seekTo(0, 0)
        simpleExoPlayer?.repeatMode = Player.REPEAT_MODE_OFF
        simpleExoPlayer?.addListener(object : Player.EventListener {

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        if(exploreResult != null ){
                            setUI()

                        }else{
                            dismiss()
                        }

                    }



                }
            }


        })
        simpleExoPlayer?.prepare(mediaSource, true, false)
    }

    private fun releasePlayer() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer!!.release()
            simpleExoPlayer = null
        }
    }

    override fun onPause() {
        releasePlayer()
        handler.removeCallbacks(runnable)
        activity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onPause()
        if (binding!!.video.isPlaying) {
            binding!!.video.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!binding!!.video.isPlaying) {
            binding!!.video.start()
        }
    }

    override fun onStop() {
        releasePlayer()
        handler.removeCallbacks(runnable)
        super.onStop()
    }


    companion object {


        fun newInstance(activity: BaseActivity, videoUri : Uri?, image: Bitmap?, exploreResult: ArrayList<ExploreResult>?, activeChallenge : ActivieChallenge?) = PreviewBottomDialog().apply {

            this.activity = activity
            this.videoUri = videoUri
            this.image = image
            this.exploreResult = exploreResult
            this.activieChallenge = activeChallenge

        }
    }


    fun addViews(){
        try {
            val rootObject = JsonObject()


            try {
                var challenge_id = ""
                var uid = ""
                if(exploreResult != null){
                    challenge_id = exploreResult!![0].id!!
                    uid = exploreResult!![0].uid!!
                }else if(activieChallenge != null){
                    challenge_id = activieChallenge?._id!!
                    uid = activieChallenge?.uid!!
                }
                rootObject.addProperty("challengeId",challenge_id)
                rootObject.addProperty("uid", uid)
                rootObject.addProperty("userId",  AppStaticData().getModel()?.result?.id)

            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.addViews(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<SuccessModel> {
                override fun onResponse(call: Call<SuccessModel>, response: Response<SuccessModel>) {
                    if (response.code() == 200) {
                        try {
                            val ViewData = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity!!,ViewData.code,ViewData.message)
                            if(checkResponse) {
                                if (ViewData.code == 200) {
                                    exploreResult!![0].isView = true
                                    NotificationCenter.getInstance()
                                        .postNotificationName(NotificationCenter.viewChallenge)


                                }
                            }else{
                                Utils.showToast(activity!!, "viewId"+ViewData.message, false)
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
                override fun onFailure(call: Call<SuccessModel>, t: Throwable) {
                    Log.e("responseError", t.message)
                    Utils.showToast(activity!!, "Have some problem...", false)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    private val runnable: Runnable = Runnable {
        setUI()
    }


    fun setUI(){
        if(pos < exploreResult!!.size-1){
            pos ++
            if (exploreResult!![pos].video!!.isNotEmpty()) {
                binding!!.video.visibility = View.VISIBLE
                binding!!.image.visibility = View.VISIBLE
                binding!!.videoUrl.visibility = View.GONE
                ShowingImage.showBannerImage(
                    activity!!,
                    exploreResult!![pos].thumbnails,
                    binding!!.image
                )
                binding!!.video.setVideoPath(exploreResult!![pos].video!!)
                binding!!.video.start()
            } else {
                binding!!.video.visibility = View.VISIBLE
                binding!!.image.visibility = View.VISIBLE
                binding!!.videoUrl.visibility = View.GONE
                if (!exploreResult!![pos].image.isNullOrEmpty()) {
                    ShowingImage.showBannerImage(
                        activity!!,
                        exploreResult!![pos].image,
                        binding!!.image
                    )
                    handler.postDelayed(runnable, 5000)
                }


            }

        }else{
            dismiss()
        }
    }

    internal open class OnSwipeTouchListener(ctx: Context?) :
        View.OnTouchListener {
        private val gestureDetector: GestureDetector
        override fun onTouch(v: View, event: MotionEvent): Boolean {

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
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                onDoubleTap()
                return true
            }

            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {

                return true
            }
        }

        open fun onSingleTap(){}
        open fun onDoubleTap(){}

        init {
            gestureDetector = GestureDetector(ctx, GestureListener())
        }
    }

}