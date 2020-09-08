package com.sws.oneonone.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.*
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Nullable
import androidx.lifecycle.Observer
import com.sws.oneonone.R
import androidx.lifecycle.ViewModelProviders
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.EventListener
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.sws.oneonone.databinding.FragmentWaterChallengeBinding
import com.sws.oneonone.dialog.ChallengeChooseBottomDialog
import com.sws.oneonone.model.CareateChallengeModel
import com.sws.oneonone.model.ChallengeIdModel
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.util.*
import com.sws.oneonone.viewModel.AcceptDeclineChallengeVM
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class WaterChallengeFragment: BaseFragment(){
    private var binding: FragmentWaterChallengeBinding? = null
    var acceptVM: AcceptDeclineChallengeVM? = null
    var popupWindow: PopupWindow? = null
    var uiData: ChallengeIdModel? = null
    var type = 0
    val service: ApiClient? = ApiClient()
    private var cacheDataSourceFactory: CacheDataSourceFactory? = null
    private var simpleExoPlayer: SimpleExoPlayer? = null
    private var simpleCache: SimpleCache? = null

    fun setUIData(@Nullable result: ChallengeIdModel?) {
        uiData = result
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        acceptVM = ViewModelProviders.of(this).get(AcceptDeclineChallengeVM::class.java)
      }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       //black
        NotificationBarColor.blackNotificationBar(activity)
        binding = FragmentWaterChallengeBinding.bind(inflater.inflate(R.layout.fragment_water_challenge, container, false))
        binding!!.lifecycleOwner = this
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.lifecycleOwner = this
        // Accept Decline view model view
        acceptVM?.activity = activity
        binding!!.acceptVM = acceptVM

        // if image is not empty
        if(!uiData?.image.isNullOrEmpty()){
            type = 1
            ShowingImage.showBannerImage(activity, uiData?.image, binding!!.imageView)
        }
        //if video is not empty
        if(!uiData?.video.isNullOrEmpty()){
            type = 2
            ShowingImage.showBannerImage(activity, uiData?.thumbnails, binding!!.imageView)

        }

        if(uiData != null){
            acceptVM?.hashtag?.value = uiData?.hashtags?.get(0)
            binding!!.challengeIdModel = uiData
            if(uiData?.active!!) {
                uiData?.createdAt?.let { getTime(it) }
            }else{
                acceptVM?.timer?.value = "00"+"\n"+ "00" +"\n" + "00"
            }
        }

        binding!!.playVideo.setOnClickListener {
            binding!!.video.visibility = View.VISIBLE
            binding!!.playVideo.visibility = View.GONE
            binding!!.imageView.visibility = View.GONE
            binding!!.video.setVideoPath(uiData?.video)
            binding!!.video.start()
          //  initPlayer()
        }


        binding!!.video.setOnCompletionListener {
            binding!!.video.visibility = View.GONE
            binding!!.playVideo.visibility = View.VISIBLE
            binding!!.imageView.visibility = View.VISIBLE
        }

        initView()
    }

    private fun initView() {
        // check has Observers
        if (!acceptVM?.comman?.hasObservers()!!) {
            acceptVM?.comman?.observe(this, Observer { str ->
                when (str) {
                    "onBack" -> onBackPressed()
                    "acceptDeclineDialog" -> acceptDeclineDialog(this)
                    "acceptChallenge" -> {
                        val  challengeChooseBottomDialog = ChallengeChooseBottomDialog.newInstance(activity,true, uiData)
                        challengeChooseBottomDialog.show(activity.supportFragmentManager, ChallengeChooseBottomDialog().TAG)
                       /* val fragment = CameraFragment()
                        val arguments = Bundle()
                        arguments.putBoolean("isFromAccept" , true )
                        arguments.putParcelable("challengeId",uiData)
                        fragment.arguments = arguments;
                        activity.replaceFragment(fragment)*/
                    }
                }
            })
        }


    }

    fun acceptDeclineDialog(fragment: WaterChallengeFragment) {
        // Initialize a new layout inflater instance
        val inflater: LayoutInflater =
            activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // Inflate a custom view using layout inflater
        val view = inflater.inflate(R.layout.layout_popup_alert, null)
        // Initialize a new instance of popup window
        popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
            LinearLayout.LayoutParams.MATCH_PARENT // Window height
        )
        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow?.elevation = 10.0F
        }

        // Get the widgets reference from custom view
        val btnAccept: TextView = view.findViewById<TextView>(R.id.btnCancel)
        val btnDecline: TextView = view.findViewById<TextView>(R.id.btnDecline)
        // Set a click listener for popup's button widget
        btnAccept.setOnClickListener {
            // Dismiss the popup window
            popupWindow?.dismiss()
        }

        btnDecline.setOnClickListener {
            // Dismiss the popup window
            deniedChallengeAsyncTask(fragment).execute()
            popupWindow?.dismiss()

        }
        // Finally, show the popup window on app
        TransitionManager.beginDelayedTransition(binding!!.rootLayout)
        popupWindow?.showAtLocation(
            binding!!.rootLayout, // Location to display popup window
            Gravity.CENTER, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )

    }


    fun getTime(time: String) {
        @SuppressLint("SimpleDateFormat")
        val spf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val dateFormat2 = SimpleDateFormat("HH:mm:SS")
        spf.timeZone = TimeZone.getTimeZone("UTC")
        dateFormat2.timeZone = TimeZone.getDefault()

        try {
            val millisToAdd = 86400000
            val startDate = Calendar.getInstance().time
            val endDate = spf.parse(time)
            endDate.time = endDate.time + millisToAdd
            val newTime = dateFormat2.format(endDate!!)
            var different = endDate.time - startDate.time
            val timer = object: CountDownTimer(different, 1000)  {
                override fun onTick(millisUntilFinished: Long) {

                    val hms = String.format(
                        "%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                            TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                        ),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                        )
                    )

                    val timeunits: List<String> = hms.split(":") //will break the string up into an array

                    val hours  = timeunits[0].toLong()
                    val minutes = timeunits[1].toLong() //first element
                    val seconds = timeunits[2].toLong() //second element

                    var  mins = ""
                    var hrs = ""
                    var scnds = ""
                    if(minutes < 10){
                        mins = "0$minutes"
                    }else{
                        mins = minutes.toString()
                    }


                    if(hours < 10){
                        hrs = "0$hours"
                    }else{
                        hrs = hours.toString()
                    }

                    if(seconds < 10){
                        scnds = "0$seconds"
                    }else{
                        scnds = seconds.toString()
                    }

                    acceptVM?.timer?.value = hrs+"\n"+mins +"\n" + scnds
                }

                override fun onFinish() {
                    acceptVM?.timer?.value = "00"+"\n"+ "00" +"\n" + "00"

                }
            }
            timer.start()

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    fun deniedChallenge() {

        try {
            val rootObject = JsonObject()
            val challengerId = JsonArray()
            challengerId.add(uiData?.createdBy)

            try {
                rootObject.addProperty("userId", AppStaticData().getModel()?.result?.id)
                rootObject.add("userTo", challengerId)
                rootObject.addProperty("type", type)
                rootObject.addProperty("image", if (type == 1) uiData?.image else "")
                rootObject.addProperty("video", if (type == 2) uiData?.video else "")
                rootObject.addProperty("thumbnails", uiData?.thumbnails)
                rootObject.addProperty("challengeType", "2")
                rootObject.addProperty("title",uiData?.title)
                rootObject.addProperty("request", "D")
                rootObject.addProperty("uid", uiData?.uid)
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
                    if (response.code() == 200) {
                        try {
                            val mMyFollowingModel = response.body()!!
                             AndroidUtilities().apisResponse(activity,mMyFollowingModel.code,mMyFollowingModel.message)

                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call<CareateChallengeModel>, t: Throwable) {
                    Log.e("responseError", t.message)
                    Utils.showToast(activity, "Have some problem...", false)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    internal class deniedChallengeAsyncTask(val fragment: WaterChallengeFragment): AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params:String):String {
            // your async action
            fragment.deniedChallenge()
            fragment.onBackPressed()
            return ""
        }
        override fun onPostExecute(aVoid:String) {

        }
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

        val videoUri = Uri.parse(uiData?.video)
        val mediaSource =
            ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(videoUri)

      //  binding!!.video.player = simpleExoPlayer
        //binding!!.video.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        simpleExoPlayer?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        simpleExoPlayer?.playWhenReady = true
        simpleExoPlayer?.seekTo(0, 0)
        simpleExoPlayer?.repeatMode = Player.REPEAT_MODE_OFF
        simpleExoPlayer?.addListener(object : EventListener{

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        binding!!.video.visibility = View.GONE
                        binding!!.playVideo.visibility = View.VISIBLE
                        binding!!.imageView.visibility = View.VISIBLE

                    }
                }
            }


        })
        simpleExoPlayer?.prepare(mediaSource, true, false)
    }

    private fun releasePlayer() {
        if (simpleExoPlayer != null) {
            binding!!.video.visibility = View.GONE
            binding!!.playVideo.visibility = View.VISIBLE
            binding!!.imageView.visibility = View.VISIBLE
            simpleExoPlayer!!.release()
            simpleExoPlayer = null
        }
    }



    override fun onPause() {
        releasePlayer()
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
        super.onStop()
    }
}

