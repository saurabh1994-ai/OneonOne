package com.sws.oneonone.fragment


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.adapter.CommentAdapter
import com.sws.oneonone.adapter.ViewExploreProfileAdapter
import com.sws.oneonone.databinding.FragmentViewExploreBinding
import com.sws.oneonone.dialog.*
import com.sws.oneonone.model.*
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.retrofit.Validation
import com.sws.oneonone.util.*
import com.sws.oneonone.viewModel.ViewExplorerVM
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class ViewExploreFragment: BaseFragment() ,NotificationCenter.NotificationCenterDelegate {
    private val DECCELERATE_INTERPOLATOR =
        DecelerateInterpolator()
    private val ACCELERATE_INTERPOLATOR =
        AccelerateInterpolator()
    var binding: FragmentViewExploreBinding? = null
    var service: ApiClient? = ApiClient()
    var uId = ""
    var viewExploreProfileAdapter:ViewExploreProfileAdapter? = null
    var viewExplorerVM: ViewExplorerVM? = null
    var viewExploreList: ArrayList<ViewResultModel>? = null
    private val handler: Handler = Handler()
    var commentsAdapter: CommentAdapter? = null
    var viewExploreData : ViewExploreModel? = null
    var commentsData : CommentsModel? = null
    var ishideComment = false
    var isLastPage: Boolean = false
    var isLoading: Boolean = false
    var pageNo = 1
    var videoUrl = ""
    var status = ""
    var commentList : ArrayList<CommentResultModel> ? = null
    private var cacheDataSourceFactory: CacheDataSourceFactory? = null
    private var simpleExoPlayer: SimpleExoPlayer? = null
    private var simpleCache: SimpleCache? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.commentData)
        viewExplorerVM = ViewModelProviders.of(this).get(ViewExplorerVM::class.java)
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        NotificationBarColor.blackNotificationBar(activity)

        binding = FragmentViewExploreBinding.bind(inflater.inflate(R.layout.fragment_view_explore, container, false))
        getViewExploreData()
        getCommentsData()

        return binding!!.root
    }



    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding!!.ivClose.setOnClickListener {
            onBackPressed()
        }


        val layoutManager1 = LinearLayoutManager(activity)
        layoutManager1.orientation = LinearLayoutManager.HORIZONTAL
        viewExploreProfileAdapter = ViewExploreProfileAdapter()
        binding!!.rvChallenger.layoutManager = layoutManager1
        binding!!.rvChallenger.adapter = viewExploreProfileAdapter

        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        commentsAdapter = CommentAdapter()
        binding!!.rvComment.layoutManager = layoutManager
        binding!!.rvComment.adapter = commentsAdapter

        binding!!.rvComment.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

            override fun loadMoreItems() {
                isLoading = true
                pageNo++
                Log.d("pageNo++",pageNo.toString())
                //you have to call loadmore items to get more data
                getCommentsData()

            }
        })



        binding!!.coverLayout.layoutMain.setOnClickListener {
            binding!!.coverLayout.layoutMain.visibility = View.GONE
        }

        viewExploreProfileAdapter!!.setListerner(object  : ViewExploreProfileAdapter.ClickListerner{
            override fun onSuccess(id: String?) {
               /* releasePlayer()
                handler.removeCallbacks(runnable)
                setUI(pos!!)*/

                val  videoBottomSheetDialog = ProfileBottomDialog.newInstance(activity,id,null,null)
                videoBottomSheetDialog.show(activity.supportFragmentManager, ProfileBottomDialog().TAG)

            }

        })

        binding!!.llViewExplore.setOnTouchListener(object : OnSwipeTouchListener(activity) {


            override fun onSingleTap() {

                if(viewExploreData!!.position >= viewExploreList!!.size -1){
                  //  releasePlayer()
                    handler.removeCallbacks(runnable)
                    viewExploreData!!.position = 0
                    setUI(viewExploreData!!.position)
                    viewExploreProfileAdapter!!.notifyDataSetChanged()
                }else{
                    //releasePlayer()
                    handler.removeCallbacks(runnable)
                    viewExploreData!!.position = viewExploreData!!.position + 1
                    setUI(viewExploreData!!.position)
                    viewExploreProfileAdapter!!.notifyDataSetChanged()
                }



            }

            override fun onDoubleTap() {
                if(viewExploreList!![0].active){
                    if(viewExploreList!!.size > 1){
                        if(!viewExploreList!![viewExploreData!!.position].isVote){
                            addVoteChallenge()
                        }else{
                            openAlertDialog("you have already voted!")
                        }

                    }else{
                        openAlertDialog("you can't vote this challenge because this challenge has not been accepted by anyone!")
                    }
                }else{
                    openAlertDialog("you can't vote this challenge because this challenge has not been active!")
                }


            }





        })


        binding!!.rvComment.setOnTouchListener(object : OnSwipeTouchListener(activity) {

            override fun onSingleTap() {
                //  binding!!.overlayView.visibility = View.INVISIBLE
                binding!!.rvComment.visibility = View.INVISIBLE
                binding!!.commentView.visibility = View.VISIBLE
                binding!!.tvComment.setTextColor(ContextCompat.getColor(activity,R.color.white))
                binding!!.rlComment.setBackgroundResource(0)

            }

        })

        binding!!.btnRespondAndVote.setOnClickListener {
            onVoteClickEvent(viewExploreData)
        }

        binding!!.commentView.setOnTouchListener (object : OnSwipeTouchListener(activity) {

            override fun onSingleTap() {
                // binding!!.overlayView.visibility = View.VISIBLE
                binding!!.rvComment.visibility = View.VISIBLE
                binding!!.commentView.visibility = View.INVISIBLE
                binding!!.tvComment.setTextColor(ContextCompat.getColor(activity,R.color.pink))
                binding!!.rlComment.setBackgroundResource(R.drawable.page_bottom_bg)

            }

        })



        binding!!.tvComment.setOnClickListener {
            if(viewExploreList!![viewExploreData!!.position].comments > 0){
                binding!!.rvComment.visibility = View.VISIBLE
                binding!!.tvComment.setTextColor(ContextCompat.getColor(activity,R.color.pink))
                binding!!.rlComment.setBackgroundResource(R.drawable.page_bottom_bg)
            }
        }

        binding!!.ivLike.setOnClickListener {
            if(viewExploreList!![0].isLike){
                animatePhotoLike(1)
                binding!!.ivLike.setImageResource(R.drawable.heart_icon)
                likeUnlike(1)
            }else {
                animatePhotoLike(2)
                binding!!.ivLike.setImageResource(R.drawable.heart_icon_fill)
                likeUnlike(2)

            }

        }

        if (!viewExplorerVM?.commentData?.hasObservers()!!) {
            viewExplorerVM?.commentData?.observe(this,
                Observer { str ->
                    if (!Validation().checkString(str)) {
                        Utils.showToast(activity, "Please enter some cool comment", false)
                    }else{
                        addComment(str)
                    }
                })
        }

        binding!!.tvLike.setOnClickListener {
            activity.replaceFragment(LikesFragment.newInstance(uId))
        }

        binding!!.tvVotes.setOnClickListener {
            activity.replaceFragment(VoteFragment.newInstance(viewExploreList!![viewExploreData!!.position]._id))
        }

        binding!!.llViews.setOnClickListener {
            activity.replaceFragment(ViewFragment.newInstance(uId,viewExploreList!![viewExploreData!!.position]._id))
        }

        binding!!.ivUpload.setOnClickListener {
            Log.d("UID++++", uId)
            AndroidUtilities().createDynamicLink(activity, "Let's checkout this awsome challenge",uId ,"","")

        }


        binding!!.tvReport.setOnClickListener {
            val  reportBottomsheetDialog =  ReportBottomsheetDialog.newInstance(activity,viewExploreList!![viewExploreData!!.position].userId._id,viewExploreList!![viewExploreData!!.position]._id)
            reportBottomsheetDialog.show(activity.supportFragmentManager, ReportBottomsheetDialog().TAG)
        }

        binding!!.video.setOnCompletionListener {
            handler.removeCallbacks(runnable)
            handler.postDelayed(runnable, 0)
        }

    }



    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.commentData)
    }

    override fun onPause() {
        releasePlayer()
        handler.removeCallbacks(runnable)
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

    fun getViewExploreData(){
        try {
            val rootObject = JsonObject()


            try {
                rootObject.addProperty("uid", uId)
                rootObject.addProperty("userId",  AppStaticData().getModel()?.result?.id)

            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.getChallenges(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<ViewExploreModel> {
                override fun onResponse(call: Call<ViewExploreModel>, response: Response<ViewExploreModel>) {
                    if (response.code() == 200) {
                        try {
                            viewExploreData = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity!!,viewExploreData!!.code!!,viewExploreData!!.message)
                            if(checkResponse) {
                                if (viewExploreData!!.code == 200) {
                                    showAction(viewExploreData)
                                    viewExploreData!!.position = 0
                                    viewExploreProfileAdapter?.setExploreProfileAdapter(
                                        activity,
                                        viewExploreData!!
                                    )
                                    viewExploreList = viewExploreData!!.result
                                    if (viewExploreList!![0].active) {
                                        var requestPos = 0
                                        for (i in 0..viewExploreList!!.size) {
                                            if (viewExploreList!![i].request == "P") {
                                                requestPos = i
                                                break
                                            }
                                        }

                                        getTime(viewExploreList!![requestPos].createdAt)
                                    } else {
                                        viewExplorerVM?.timer?.value =
                                            "00" + "\n" + "00" + "\n" + "00"
                                    }


                                    setUI(0)
                                    for (i in 0..viewExploreData!!.result.size){
                                        if(viewExploreList!![i].createdBy.isNotEmpty()){
                                            viewExplorerVM?.likesCount?.value =  viewExploreList!![i].likes.toString() + " Likes"
                                            viewExplorerVM?.commentCount?.value = viewExploreList!![i].comments.toString() + " Comments"
                                            if(viewExploreList!![i].isLike){
                                                binding!!.ivLike.setImageResource(R.drawable.heart_icon_fill)
                                            }else {
                                                binding!!.ivLike.setImageResource(R.drawable.heart_icon)
                                            }
                                        }

                                    }
                                    //  handler.postDelayed(runnable, 5000)

                                }
                            }else {
                                Utils.showToast(activity, viewExploreData!!.message, false)
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
                override fun onFailure(call: Call<ViewExploreModel>, t: Throwable) {
                    Log.e("responseError", t.message)
                    Utils.showToast(activity, "Have some problem...", false)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    companion object {

        fun newInstance(uid: String?) = ViewExploreFragment().apply {
            this.uId = uid!!

        }
    }


    fun setUI(pos:Int){


        if(!viewExploreList!![pos].image.isNullOrEmpty()){
            binding!!.video.visibility = View.GONE
            binding!!.ivViewExplore.visibility = View.VISIBLE
         //   releasePlayer()
            ShowingImage.showBannerImage(
                activity,
                viewExploreList!![pos].image,
                binding!!.ivViewExplore
            )
            handler.removeCallbacks(runnable)
            handler.postDelayed(runnable, 5000)
        }

        if(!viewExploreList!![pos].video.isNullOrEmpty()) {
            /*   ShowingImage.showBannerImage(
                   activity,
                   viewExploreList!![pos].thumbnails,
                   binding!!.ivViewExplore
               )*/
            videoUrl = viewExploreList!![pos].video
            Log.d("video++",viewExploreList!![pos].video)
            binding!!.video.visibility = View.VISIBLE
            binding!!.ivViewExplore.visibility = View.VISIBLE
            ShowingImage.showBannerImage(
                activity,
                viewExploreList!![pos].thumbnails,
                binding!!.ivViewExplore
            )
            binding!!.video.setVideoPath(videoUrl)
            binding!!.video.start()
            binding!!.video.setOnPreparedListener {
                binding!!.ivViewExplore.visibility = View.GONE
            }
          //  initPlayer()
        }

        viewExplorerVM?.viewCount?.value = viewExploreList!![pos].viewCount.toString()
        viewExplorerVM?.votesCount?.value = viewExploreList!![pos].votes.toString() + " Votes"
        viewExplorerVM?.hashtag?.value = viewExploreList!![pos].hashtags[0]
        binding!!.lifecycleOwner = activity
        binding!!.viewExplorerVM = viewExplorerVM


        if(!viewExploreList!![pos].isView){
            addViews()
        }

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

                    viewExplorerVM?.timer?.value = hrs+"\n"+mins +"\n" + scnds
                }

                override fun onFinish() {
                    viewExplorerVM?.timer?.value = "00"+"\n"+ "00" +"\n" + "00"
                }
            }
            timer.start()

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun getCommentsData(){
        try {
            val rootObject = JsonObject()
            try {
                rootObject.addProperty("uid", uId)
                rootObject.addProperty("pageNumber",  pageNo)

            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.getComments(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<CommentsModel> {
                override fun onResponse(call: Call<CommentsModel>, response: Response<CommentsModel>) {
                    if (response.code() == 200) {
                        try {
                            commentsData = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity!!,commentsData!!.code!!,commentsData!!.message)
                            if(checkResponse) {
                                if (commentsData!!.code == 200) {
                                    commentList = commentsData!!.result
                                    if (commentList!!.size > 0) {
                                        binding!!.tvComment.setTextColor(
                                            ContextCompat.getColor(
                                                activity,
                                                R.color.pink
                                            )
                                        )
                                        binding!!.rlComment.setBackgroundResource(R.drawable.page_bottom_bg)
                                    }
                                    getMoreItems()
                                    viewExplorerVM?.likesCount?.value =
                                        commentsData!!.likesCount.toString() + " Likes"
                                    viewExplorerVM?.commentCount?.value =
                                        commentsData!!.commentsCount.toString() + " Comments"
                                    //  setUI(viewExploreData!!.position)
                                }
                            }else {
                                Utils.showToast(activity, commentsData!!.message, false)
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
                override fun onFailure(call: Call<CommentsModel>, t: Throwable) {
                    Log.e("responseError", t.message)
                    Utils.showToast(activity, "Have some problem...", false)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private val runnable: Runnable = Runnable {
        if(viewExploreData!!.position >= viewExploreList!!.size -1){
            viewExploreData!!.position = 0
            setUI(viewExploreData!!.position)
            viewExploreProfileAdapter!!.notifyDataSetChanged()

        }else{
            viewExploreData!!.position = viewExploreData!!.position + 1
            setUI(viewExploreData!!.position)
            viewExploreProfileAdapter!!.notifyDataSetChanged()
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

    fun addVoteChallenge(){
        try {
            val rootObject = JsonObject()
            try {
                rootObject.addProperty("challengeId",viewExploreList!![viewExploreData!!.position]._id)
                rootObject.addProperty("uid", uId)
                rootObject.addProperty("userId",  AppStaticData().getModel()?.result?.id)

            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.addvoteChallenge(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<SuccessModel> {
                override fun onResponse(call: Call<SuccessModel>, response: Response<SuccessModel>) {
                    if (response.code() == 200) {
                        try {
                            val votesData = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity!!,votesData.code!!,votesData.message)
                            if(checkResponse) {
                                if (votesData.code == 200 && votesData.message != "already voted") {
                                    viewExploreList!![viewExploreData!!.position].votes =
                                        viewExploreList!![viewExploreData!!.position].votes + 1
                                    viewExplorerVM?.votesCount?.value =
                                        viewExploreList!![viewExploreData!!.position].votes.toString() + " Votes"
                                    viewExploreList!![viewExploreData!!.position].isVote = true
                                    openDialog()
                                }
                            }else {
                                openAlertDialog(votesData.message)
                                //Utils.showToast(activity, commentsData.message, false)
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
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
    }

    fun openDialog(){
        val myDialog = VoteDialog.newInstance(activity)
        myDialog.show(activity.supportFragmentManager, "Vote Dialog")

    }

    fun openAlertDialog(txtmsg :String){
        val myDialog = AlertDialog.newInstance(activity,txtmsg)
        myDialog.show(activity.supportFragmentManager, "Alert Dialog")
    }

    fun likeUnlike( type:Int){
        try {
            val rootObject = JsonObject()


            try {
                rootObject.addProperty("type",  type)
                rootObject.addProperty("challengeId",viewExploreList!![viewExploreData!!.position]._id)
                rootObject.addProperty("uid", uId)

            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.likeUnlike(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<SuccessModel> {
                override fun onResponse(call: Call<SuccessModel>, response: Response<SuccessModel>) {
                    if (response.code() == 200) {
                        try {
                            val likeResponse = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity!!,likeResponse.code!!,likeResponse.message)
                            if(checkResponse){
                                if (likeResponse.code == 200) {
                                    if (type == 2) {
                                        commentsData!!.likesCount = commentsData!!.likesCount + 1
                                        viewExplorerVM?.likesCount?.value =
                                            commentsData!!.likesCount.toString() + " Likes"

                                        viewExploreList!![0].isLike = true
                                    } else {
                                        commentsData!!.likesCount = commentsData!!.likesCount - 1
                                        viewExplorerVM?.likesCount?.value =
                                            commentsData!!.likesCount.toString() + " Likes"
                                        viewExploreList!![0].isLike = false

                                    }
                                }

                            } else {
                                openAlertDialog(likeResponse.message)
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
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
    }


    fun addComment(comment:String){
        viewExplorerVM!!.comment!!.value = ""
        try {
            val rootObject = JsonObject()
            try {
                rootObject.addProperty("comment",  comment)
                rootObject.addProperty("uid", uId)
                rootObject.addProperty("challengeId",viewExploreList!![viewExploreData!!.position]._id)
                rootObject.addProperty("userId",  AppStaticData().getModel()?.result?.id)

            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.addComments(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<SuccessModel> {
                override fun onResponse(call: Call<SuccessModel>, response: Response<SuccessModel>) {
                    if (response.code() == 200) {
                        try {
                            val commentsData = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity!!,commentsData.code!!,commentsData.message)
                            if(checkResponse) {
                                if (commentsData.code == 200) {
                                    // openAlertDialog(commentsData.message)
                                } else {
                                    openAlertDialog(commentsData.message)
                                    //Utils.showToast(activity, commentsData.message, false)
                                }
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
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
    }

    fun getMoreItems() {
//after fetching your data assuming you have fetched list in your
// recyclerview adapter assuming your recyclerview adapter is
//rvAdapter
//  after getting your data you have to assign false to isLoading


        if(pageNo == 1)
            commentsAdapter!!.setCommentsList(activity, commentList!!)
        else {
            if(commentList!!.size> 0){
                isLoading = false
                commentsAdapter!!.addData(commentList!!,null)
            }
        }
    }


    fun addViews(){
        try {
            val rootObject = JsonObject()


            try {

                rootObject.addProperty("challengeId",viewExploreList!![viewExploreData!!.position]._id)
                rootObject.addProperty("uid", uId)
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
                            val checkResponse =  AndroidUtilities().apisResponse(activity!!,ViewData.code!!,ViewData.message)
                            if(checkResponse) {
                                if (ViewData.code == 200) {
                                    viewExplorerVM?.viewCount?.value =
                                        (viewExploreList!![viewExploreData!!.position].viewCount + 1).toString()
                                    viewExploreList!![viewExploreData!!.position].viewCount =
                                        viewExploreList!![viewExploreData!!.position].viewCount + 1
                                    viewExploreList!![viewExploreData!!.position].isView = true

                                }
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
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
    }

    private fun releasePlayer() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer!!.playWhenReady = false
            simpleExoPlayer!!.stop()
            simpleExoPlayer!!.release()
            simpleExoPlayer = null
        }
    }



    fun printDifference(startDate: Date, endDate: Date) {
//milliseconds
        var different = endDate.time - startDate.time
        println("startDate : $startDate")
        println("endDate : $endDate")
        println("different : $different")
        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24
        val elapsedDays = different / daysInMilli
        different = different % daysInMilli
        val elapsedHours = different / hoursInMilli
        different = different % hoursInMilli
        val elapsedMinutes = different / minutesInMilli
        different = different % minutesInMilli
        val elapsedSeconds = different / secondsInMilli
        System.out.printf(
            "%d days, %d hours, %d minutes, %d seconds%n",
            elapsedHours, elapsedMinutes, elapsedSeconds
        )
    }


    override fun didReceivedNotification(id: Int, vararg args: Any?) {
        if (id == NotificationCenter.commentData) {
            val data = args[0] as JSONObject
            val comment = data.getString("comment")
            val uid = data.getString("uid")
            val userId = data.getJSONObject("userId")
            val username = userId.getString("username")
            val profileImg = userId.getString("profileImg")
            val userid = userId.getString("_id")
            val userDataModel = UserDataModel(username,profileImg,userid,"","")
            val commentData = CommentResultModel(comment,"",userDataModel)

            if(uId == uid){
                if(pageNo == 1){
                    commentList!!.add(0,commentData)
                    commentsAdapter!!.setCommentsList(activity, commentList!!)
                } else {
                    commentsAdapter!!.addData(commentList!!, commentData)
                }
            }


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

        val videoUri = Uri.parse(videoUrl)
        val mediaSource =
            ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(videoUri)

      //  binding!!.video.player = simpleExoPlayer
        /* binding!!.video.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
         simpleExoPlayer?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING*/
        simpleExoPlayer?.playWhenReady = true
        simpleExoPlayer?.stop()
        simpleExoPlayer?.seekTo(0, 0)
        simpleExoPlayer?.repeatMode = Player.REPEAT_MODE_OFF
        simpleExoPlayer?.addListener(object : Player.EventListener {

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        releasePlayer()
                        handler.removeCallbacks(runnable)
                        handler.postDelayed(runnable, 0)

                    }
                }
            }
        })
        simpleExoPlayer?.prepare(mediaSource, true, false)
    }

    fun showAction(model: ViewExploreModel?){
        if (model?.result?.size!! > 0) {
            if (model?.result!![0].active){
                if (model?.result!![0].isOpenChallenge) {
                    if(model?.result!![0].createdBy.equals(AppStaticData().getModel()?.result?.id)) {
                        if (!model?.result!![0].isStartVote) {
                            viewExplorerVM?.isOpenChallenge?.value = true
                            viewExplorerVM?.btnText?.value = " V "
                        }
                    } else {
                        if (model?.result!![0].count < 4){
                            viewExplorerVM?.isOpenChallenge?.value = true
                            viewExplorerVM?.btnText?.value = " R "
                        }
                        if (model?.result!![0].expireRespondTime){
                            viewExplorerVM?.isOpenChallenge?.value = false
                        }
                        if (model?.result!![0].isAccepted){
                            viewExplorerVM?.isOpenChallenge?.value = false
                        }
                    }
                }
            }
        }
    }

    fun onVoteClickEvent(model: ViewExploreModel?){
        if (model?.result?.size!! > 0) {
            if (model?.result!![0].active) {
                if (model?.result!![0].isOpenChallenge) {
                    if (model?.result!![0].createdBy.equals(AppStaticData().getModel()?.result?.id)) {
                        // Call Start Voting api
                        startVoting()
                    } else {
                        val uiData: ChallengeIdModel? = ChallengeIdModel()
                        uiData?.hashtags = model?.result!![0].hashtags
                        uiData?.createdBy = model?.result!![0].createdBy
                        uiData?.title = model?.result!![0].title
                        uiData?.uid = model?.result!![0].uid

                        val  challengeChooseBottomDialog = ChallengeChooseBottomDialog.newInstance(activity,true, uiData)
                        challengeChooseBottomDialog.show(activity.supportFragmentManager, ChallengeChooseBottomDialog().TAG)

                        // val  challengeChooseBottomDialog = ChallengeChooseBottomDialog.newInstance(activity,false,null)
                       // challengeChooseBottomDialog.show(activity.supportFragmentManager, ChallengeChooseBottomDialog().TAG)
                    }
                }
            }
        }
    }

    fun startVoting(){
       try {
            val rootObject = JsonObject()
            try {
                rootObject.addProperty("uid", uId)
                //rootObject.addProperty("userId",  AppStaticData().getModel()?.result?.id)

            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.startVoting(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<SuccessModel> {
                override fun onResponse(call: Call<SuccessModel>, response: Response<SuccessModel>) {
                    if (response.code() == 200) {
                        val successModel = response.body()!!
                        try {
                            if (successModel?.code == 200) {
                                viewExplorerVM?.isOpenChallenge?.value = false
                                Utils.showToast(activity, "Voting Start", false)
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
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
    }


    private fun animatePhotoLike(type: Int) {
        val animatorSet = AnimatorSet()
        var imgScaleUpYAnim : ObjectAnimator? = null
        var imgScaleUpXAnim : ObjectAnimator? = null
        var imgScaleDownYAnim : ObjectAnimator? = null
        var imgScaleDownXAnim: ObjectAnimator? = null
        if(type == 2){
            binding!!.ivLikeAnim.scaleY = 0.4f
            binding!!.ivLikeAnim.scaleX = 0.4f
             imgScaleUpYAnim =
                ObjectAnimator.ofFloat( binding!!.ivLikeAnim, "scaleY", 0.4f, 1f)
          imgScaleUpXAnim =
                ObjectAnimator.ofFloat(binding!!.ivLikeAnim, "scaleX", 0.4f, 1f)
             imgScaleDownYAnim =
                ObjectAnimator.ofFloat(binding!!.ivLikeAnim, "scaleY", 1f, 0f)
             imgScaleDownXAnim =
                ObjectAnimator.ofFloat(binding!!.ivLikeAnim, "scaleX", 1f, 0f)
        }else{
            binding!!.ivDisLikeAnim.scaleY = 0.4f
            binding!!.ivDisLikeAnim.scaleX = 0.4f
            imgScaleUpYAnim =
                ObjectAnimator.ofFloat( binding!!.ivDisLikeAnim, "scaleY", 0.4f, 1f)
            imgScaleUpXAnim =
                ObjectAnimator.ofFloat(binding!!.ivDisLikeAnim, "scaleX", 0.4f, 1f)
            imgScaleDownYAnim =
                ObjectAnimator.ofFloat(binding!!.ivDisLikeAnim, "scaleY", 1f, 0f)
            imgScaleDownXAnim =
                ObjectAnimator.ofFloat(binding!!.ivDisLikeAnim, "scaleX", 1f, 0f)
        }



        imgScaleUpYAnim?.duration = 500
        imgScaleUpYAnim?.interpolator = DECCELERATE_INTERPOLATOR

        imgScaleUpXAnim?.duration = 500
        imgScaleUpXAnim?.interpolator = DECCELERATE_INTERPOLATOR

        imgScaleDownYAnim.duration = 500
        imgScaleDownYAnim.interpolator = ACCELERATE_INTERPOLATOR

        imgScaleDownXAnim.duration = 500
        imgScaleDownXAnim.interpolator = ACCELERATE_INTERPOLATOR
        animatorSet.playTogether(imgScaleUpYAnim, imgScaleUpXAnim)
        animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                if(type == 2){
                    binding!!.ivLikeAnim.visibility = View.VISIBLE
                }else{
                    binding!!.ivDisLikeAnim.visibility = View.VISIBLE
                }

            }

            override fun onAnimationEnd(animation: Animator) {
               // animationInProgress = false
                if(type == 2){
                    binding!!.ivLikeAnim.visibility = View.GONE
                }else{
                    binding!!.ivDisLikeAnim.visibility = View.GONE
                }

            }
        })
        animatorSet.start()
    }
}



