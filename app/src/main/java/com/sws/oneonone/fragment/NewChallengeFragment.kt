package com.sws.oneonone.fragment

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.Spannable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.`interface`.AwsResponse
import com.sws.oneonone.`interface`.AwsVideoResponse
import com.sws.oneonone.adapter.FollowingSelectedAdapter
import com.sws.oneonone.adapter.HashtagAdapter
import com.sws.oneonone.databinding.FragmentNewChallengesBinding
import com.sws.oneonone.dialog.PreviewBottomDialog
import com.sws.oneonone.model.*
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.util.*
import com.sws.oneonone.viewModel.NewChallengesVM
import com.sws.oneonone.viewModel.ToolbarVM
import com.vincent.videocompressor.VideoCompress
import com.vincent.videocompressor.VideoCompress.CompressListener
import okhttp3.MediaType
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
import java.util.regex.Pattern
import kotlin.collections.ArrayList


@Suppress("INACCESSIBLE_TYPE")
class NewChallengeFragment: BaseFragment() ,AwsResponse,AwsVideoResponse,
    NotificationCenter.NotificationCenterDelegate {
    private var binding: FragmentNewChallengesBinding? = null
    var toolbarVM: ToolbarVM? = null
    var newChallengesVM: NewChallengesVM? = null
    var videoPath: String? = null
    var challengeImage: Bitmap? = null
    var videoFile: File? = null
    var service: ApiClient? = ApiClient()
    var followingList: ArrayList<MyFollowingResult> = ArrayList()
    var challengersIdList: ArrayList<String> = ArrayList()
    var hrs = 18
    var type = 0
    val hashtagList = ArrayList<String>()
    var isShowDialog = false
    var pageNo = 1
    var hastagAdapter:HashtagAdapter? = null
    var videoUri:Uri? = null
    var outputDir = ""
    var inputPath = ""
    private var startTime: Long = 0
    private  var endTime:Long = 0
    var destPath = ""
    var isCompress = false
    var  createChallengeModel : CareateChallengeModel? = null
    var ishashtagButton = false




    fun setResult(
        @Nullable videoPath: String?,
        image: Bitmap?,
        videoFile: File?

    ) {
        this.videoPath = videoPath
        this.challengeImage = image
        this.videoFile = videoFile
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // NotificationBar change backgraound/text color
        toolbarVM = ViewModelProviders.of(this).get(ToolbarVM::class.java)
        newChallengesVM = ViewModelProviders.of(this).get(NewChallengesVM::class.java)
        outputDir = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath
        if (videoFile != null ) {
            videoCompress()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity.window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;//  set status text dark
        activity.window.statusBarColor =
            ContextCompat.getColor(activity, R.color.white);// set status background white
        binding = FragmentNewChallengesBinding.bind(
            inflater.inflate(
                R.layout.fragment_new_challenges,
                container,
                false
            )
        )
        binding!!.lifecycleOwner = this
        showDialog()
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.selectedItem)
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.trimVideo)
        return binding!!.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Tool bar model
        val toolbarModel = ToolbarModel(
            View.VISIBLE,
            "New Challenge",
            View.GONE,
            View.VISIBLE,
            View.VISIBLE,
            View.GONE,
            1,
            "Send",
            "",
            View.GONE
        )
        binding!!.layoutToolbar.toolbarModel = toolbarModel
        binding!!.layoutToolbar.lifecycleOwner = this

        toolbarVM?.activity = activity
        binding!!.layoutToolbar.toolbarVM = toolbarVM

        newChallengesVM?.activity = activity
        newChallengesVM?.videoPath?.value = videoPath
        binding!!.newChallengesVM = newChallengesVM

        if (!CreateChallengerData.selectList.isNullOrEmpty()) {


            followingList = CreateChallengerData.selectList


        }
        showView()
        val layoutManager1 = LinearLayoutManager(activity)
        layoutManager1.orientation = LinearLayoutManager.HORIZONTAL

        binding!!.rvAddChallenges.layoutManager = layoutManager1
        binding!!.rvAddChallenges.adapter = FollowingSelectedAdapter(activity)

        if(followingList.size >= 4 || !binding!!.openChallenge.isChecked){
            newChallengesVM?.openChallenge?.value = false
        }
        challengersIdList.clear()
        for (challengersData in followingList) {
            challengersIdList.add(challengersData.id!!)
        }

        binding!!.openChallenge.setOnCheckedChangeListener(object:CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView:CompoundButton, isChecked:Boolean) {
                if (isChecked){
                    if(followingList.size >= 4){
                        newChallengesVM?.openChallenge?.value = false
                    } else {
                        newChallengesVM?.openChallenge?.value = true
                    }
                } else {
                    newChallengesVM?.openChallenge?.value = false
                }
            }
        })

        binding!!.hashTag.setOnClickListener {
            ishashtagButton = true
            binding!!.hastagView.visibility = View.VISIBLE
            binding!!.rvHastag.visibility = View.VISIBLE
            val text : String? = binding!!.etHastag.text.toString().trim()
            var pageNo = 1
            getHashtag("#")

            if (text.equals("")) {

                binding!!.etHastag.setText("#@")
                binding!!.etHastag.setSelection(2)
            } else {

                binding!!.etHastag.setText(text + " " +"#@")
                binding!!.etHastag.setSelection(text!!.length + 3)
            }
        }
        binding!!.etHastag.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (after < count) {
                    if (!ishashtagButton) {
                        Log.d("DELETE_KEY_PRESSED", "beforeTextChanged: ")
                        val inputStr = s as Spannable
                        for (old in inputStr.getSpans(
                            start,
                            inputStr.length,
                            ForegroundColorSpan::class.java
                        ))
                            inputStr.removeSpan(old)
                        val p = Pattern.compile(AndroidUtilities().linkPatterns)
                        val matcher = p.matcher(inputStr)
                        var  afterString = ""
                        while (matcher.find()){
                            binding!!.hastagView.visibility = View.VISIBLE
                            binding!!.rvHastag.visibility = View.VISIBLE
                            inputStr.setSpan(
                                ForegroundColorSpan(
                                    ContextCompat.getColor(
                                        activity,
                                        R.color.pink
                                    )
                                ), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            val  hastag  = s.substring(matcher.start(), matcher.end())
                            val  parts = s.split(hastag)

                            afterString = parts [1]
                            if(!ishashtagButton){
                                getHashtag(hastag)
                            }

                        }
                        //    getHashtag("#")



                        if(afterString.isNotEmpty() || s.isEmpty() || !matcher.find()){
                            binding!!.hastagView.visibility = View.GONE
                            binding!!.rvHastag.visibility = View.GONE

                        }

                    }
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if(!ishashtagButton){
                    val inputStr = s as Spannable
                    for (old in inputStr.getSpans(
                        start,
                        inputStr.length,
                        ForegroundColorSpan::class.java
                    ))
                        inputStr.removeSpan(old)
                    val p = Pattern.compile(AndroidUtilities().linkPatterns)
                    val matcher = p.matcher(inputStr)
                    var  afterString = ""
                    while (matcher.find()){
                        binding!!.hastagView.visibility = View.VISIBLE
                        binding!!.rvHastag.visibility = View.VISIBLE
                        inputStr.setSpan(
                            ForegroundColorSpan(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.pink
                                )
                            ), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        val  hastag  = s.substring(matcher.start(), matcher.end())
                        val  parts = s.split(hastag)

                        afterString = parts [1]
                        if(!ishashtagButton){
                            getHashtag(hastag)
                        }

                    }


                    if(afterString.isNotEmpty() || s.isEmpty()){
                        binding!!.hastagView.visibility = View.GONE
                        binding!!.rvHastag.visibility = View.GONE

                    }

                }


            }

        })





        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        hastagAdapter = HashtagAdapter()
        binding!!.rvHastag.layoutManager = layoutManager
        binding!!.rvHastag.adapter = hastagAdapter



        initView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.selectedItem)
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.trimVideo)
    }


    private fun initView() {
       /* if(videoFile != null){
            getLinkForVideo()
        }*/

        if (challengeImage != null) {
            binding!!.challengImage.setImageBitmap(challengeImage)
            binding!!.playVideo.visibility = View.GONE
            binding!!.challengImage.setOnClickListener {
                openPreviewDialog(null,challengeImage!!)
            }

        }



        binding!!.sixhrs.setOnClickListener {

            hrs = 6
            setHours()
        }

        binding!!.twelvehrs.setOnClickListener {
            hrs = 12
            setHours()
        }

        binding!!.eighteenhrs.setOnClickListener {
            hrs = 18
            setHours()
        }

        binding!!.twentyfourhrs.setOnClickListener {
            hrs = 24
            setHours()
        }

        // check has Observers
        if (!toolbarVM?.comman?.hasObservers()!!) {
            toolbarVM?.comman?.observe(this, Observer { str ->
                when (str) {
                    "onBack" -> onBackPressed()
                    "next" -> {
                        hashtagList.clear()

                        if (isValidate()) {
                            if (videoFile == null && videoUri == null) {
                                type = 1
                                val name = UUID.randomUUID().toString().toUpperCase()
                                val picturePath: File? = AndroidUtilities().getSavedBitmapPath(name, activity, challengeImage!!)
                                UploadImageOnAWS.uploadChallengeImage(activity, picturePath!!.absolutePath, this, "images/")
                               // nextShowDialog()
                            } else  {
                                type = 2
                                if(videoFile != null){
                                    if(isCompress){
                                        Loader.showLoader(activity)
                                        getLinkForVideo()
                                    }else{
                                        AndroidUtilities().openAlertDialog("Please wait",activity)
                                    }

                                }else if(videoUri != null) {
                                    if (isCompress) {
                                        val videoFile = File(destPath)
                                        val uri = Uri.fromFile(videoFile)
                                        val videofile: File = FilePath.from(activity, uri)
                                        Loader.showLoader(activity)
                                        getLinkForVideo()
                                     /*   UploadVideoOnAWS.uploadVideo(
                                            activity,
                                            videofile.absolutePath,
                                            this,
                                            videofile
                                        )*/

                                    } else {
                                        AndroidUtilities().openAlertDialog("Please wait", activity)
                                    }
                                }


                            }
                            // nextShowDialog()
                        }

                    }
                }
            })
        }
    }

    private fun showDialog() {
        if (!isShowDialog) {
            isShowDialog = true
            // Initialize a new layout inflater instance
            val inflater: LayoutInflater =
                activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            // Inflate a custom view using layout inflater
            val view = inflater.inflate(R.layout.layout_popup_challenges, null)
            // Initialize a new instance of popup window
            val popupWindow = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
                LinearLayout.LayoutParams.MATCH_PARENT // Window height
            )
            // Set an elevation for the popup window
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.elevation = 10.0F
            }

            // Get the widgets reference from custom view
            val okBtn: TextView = view.findViewById<TextView>(R.id.okBtn)
            // Set a click listener for popup's button widget
            okBtn.setOnClickListener {
                // Dismiss the popup window
                popupWindow.dismiss()
            }


            // Finally, show the popup window on app
            TransitionManager.beginDelayedTransition(binding!!.rootLayout)
            popupWindow.showAtLocation(
                binding!!.rootLayout, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
            )
        }

    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun nextShowDialog() {
        // Initialize a new layout inflater instance
        val inflater: LayoutInflater =
            activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // Inflate a custom view using layout inflater
        val view = inflater.inflate(R.layout.layout_popup_new_challenger, null)
        // Initialize a new instance of popup window
        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
            LinearLayout.LayoutParams.MATCH_PARENT // Window height
        )
        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10.0F
        }
       /* if(videoFile != null){
            videoFile!!.delete()
        }
*/

        // Get the widgets reference from custom view
        val btnExplore: TextView = view.findViewById(R.id.btnExplore)
        val btnChange: TextView = view.findViewById(R.id.btnChange)
        val llSnapchat = view.findViewById<LinearLayout>(R.id.llSnapchat)
        val llWatsupp = view.findViewById<LinearLayout>(R.id.llWatsupp)
        val llSms = view.findViewById<LinearLayout>(R.id.llSms)
        val llShareLink = view.findViewById<LinearLayout>(R.id.llShareLink)
        // Set a click listener for popup's button widget
        btnExplore.setOnClickListener {
            // Dismiss the popup window
            popupWindow.dismiss()

            if(videoFile != null){
                activity.replaceFragment(HomeFragment.newInstance("","","",true,true))
            }else if(videoUri != null){
                activity.replaceFragment(HomeFragment.newInstance("","","",true,true))
            }else{
                activity.replaceFragment(HomeFragment.newInstance("","","",true,false))
            }


        }

        btnChange.setOnClickListener {
            // Dismiss the popup window
            popupWindow.dismiss()
            // onBackPressed()
            activity.replaceFragment(CameraFragment())
        }

        llSnapchat.setOnClickListener {
            AndroidUtilities().createDynamicLink(activity, "Let's checkout this awsome challenge",createChallengeModel!!.result.uid ,"","SnapChat")
        }

        llWatsupp.setOnClickListener {
            AndroidUtilities().createDynamicLink(activity, "Let's checkout this awsome challenge",createChallengeModel!!.result.uid ,"","Whatsupp")
        }

        llSms.setOnClickListener {
            AndroidUtilities().createDynamicLink(activity, "Let's checkout this awsome challenge",createChallengeModel!!.result.uid ,"","Sms")
        }

        llShareLink.setOnClickListener {
            AndroidUtilities().createDynamicLink(activity, "Let's checkout this awsome challenge",createChallengeModel!!.result.uid ,"","")

        }

        // Set a dismiss listener for popup window

        // Finally, show the popup window on app
        TransitionManager.beginDelayedTransition(binding!!.rootLayout)
        popupWindow.showAtLocation(
            binding!!.rootLayout, // Location to display popup window
            Gravity.CENTER, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )
    }


    override fun onStateComplete(response: String?) {

        Log.d("AWSURl", response!!)
        createChallenge(response, "")
      //  createChallengeAsyncTask( this, response,"").execute()


    }


    fun isValidate(): Boolean {
        if (binding!!.etHastag.text.trim().isEmpty()) {
            Toast.makeText(activity, "Please put some cool hashtag", Toast.LENGTH_SHORT).show()
            return false
        } else if(!binding!!.openChallenge.isChecked){
            if (followingList.size == 0) {
                Toast.makeText(activity, "Please add atleast one challengers", Toast.LENGTH_SHORT)
                    .show()
                return false

            }
        }


        return true
    }

    fun setHours() {
        when (hrs) {
            6 -> {
                binding!!.sixhrs.setBackgroundResource(R.drawable.bg_red_fill_30)
                binding!!.sixhrs.setTextColor(ContextCompat.getColor(activity, R.color.white))
                binding!!.twelvehrs.setBackgroundResource(R.drawable.bg_gray_border_20)
                binding!!.twelvehrs.setTextColor(ContextCompat.getColor(activity, R.color.black))
                binding!!.eighteenhrs.setBackgroundResource(R.drawable.bg_gray_border_20)
                binding!!.eighteenhrs.setTextColor(ContextCompat.getColor(activity, R.color.black))
                binding!!.twentyfourhrs.setBackgroundResource(R.drawable.bg_gray_border_20)
                binding!!.twentyfourhrs.setTextColor(
                    ContextCompat.getColor(
                        activity,
                        R.color.black
                    )
                )
            }
            12 -> {
                binding!!.sixhrs.setBackgroundResource(R.drawable.bg_gray_border_20)
                binding!!.sixhrs.setTextColor(ContextCompat.getColor(activity, R.color.black))
                binding!!.twelvehrs.setBackgroundResource(R.drawable.bg_red_fill_30)
                binding!!.twelvehrs.setTextColor(ContextCompat.getColor(activity, R.color.white))
                binding!!.eighteenhrs.setBackgroundResource(R.drawable.bg_gray_border_20)
                binding!!.eighteenhrs.setTextColor(ContextCompat.getColor(activity, R.color.black))
                binding!!.twentyfourhrs.setBackgroundResource(R.drawable.bg_gray_border_20)
                binding!!.twentyfourhrs.setTextColor(
                    ContextCompat.getColor(
                        activity,
                        R.color.black
                    )
                )
            }
            18 -> {
                binding!!.sixhrs.setBackgroundResource(R.drawable.bg_gray_border_20)
                binding!!.sixhrs.setTextColor(ContextCompat.getColor(activity, R.color.black))
                binding!!.twelvehrs.setBackgroundResource(R.drawable.bg_gray_border_20)
                binding!!.twelvehrs.setTextColor(ContextCompat.getColor(activity, R.color.black))
                binding!!.eighteenhrs.setBackgroundResource(R.drawable.bg_red_fill_30)
                binding!!.eighteenhrs.setTextColor(ContextCompat.getColor(activity, R.color.white))
                binding!!.twentyfourhrs.setBackgroundResource(R.drawable.bg_gray_border_20)
                binding!!.twentyfourhrs.setTextColor(
                    ContextCompat.getColor(
                        activity,
                        R.color.black
                    )
                )
            }

            24 -> {
                binding!!.sixhrs.setBackgroundResource(R.drawable.bg_gray_border_20)
                binding!!.sixhrs.setTextColor(ContextCompat.getColor(activity, R.color.black))
                binding!!.twelvehrs.setBackgroundResource(R.drawable.bg_gray_border_20)
                binding!!.twelvehrs.setTextColor(ContextCompat.getColor(activity, R.color.black))
                binding!!.eighteenhrs.setBackgroundResource(R.drawable.bg_gray_border_20)
                binding!!.eighteenhrs.setTextColor(ContextCompat.getColor(activity, R.color.black))
                binding!!.twentyfourhrs.setBackgroundResource(R.drawable.bg_red_fill_30)
                binding!!.twentyfourhrs.setTextColor(
                    ContextCompat.getColor(
                        activity,
                        R.color.white
                    )
                )
            }
        }

    }

    fun createChallenge(AwsUrl: String?,thumbNail: String?) {
        val hashtag = binding!!.etHastag.text.toString()
        val pattern = Pattern.compile(AndroidUtilities().linkPatterns)
        val matcher = pattern.matcher(hashtag)
        while (matcher.find()) {
            val getHashtag = hashtag.substring(matcher.start(), matcher.end())
            hashtagList.add(getHashtag)
        }
       /* try {*/
            val rootObject = JsonObject()
            val challengerId = JsonArray()
            val hashList = JsonArray()
            for (userto in challengersIdList) {
                challengerId.add(userto)
            }

            for (hashtags in hashtagList) {
                hashList.add(hashtags)

            }

            Log.d("VideoUrl","check")
            var isOpenCh: String? = "0"
            if (newChallengesVM?.openChallenge?.value!!){
                isOpenCh = "1"
            } else {
                isOpenCh =  "0"
            }

            try {
                rootObject.addProperty("userId", AppStaticData().getModel()?.result?.id)
                rootObject.add("userTo", challengerId)
                rootObject.addProperty("type", type)
                rootObject.addProperty("image", if (type == 1) AwsUrl else "")
                rootObject.addProperty("video", if (type == 2) AwsUrl else "")
                rootObject.addProperty("thumbnails", thumbNail)
                rootObject.addProperty("challengeType", "1")
                rootObject.addProperty("title", binding!!.etHastag.text.toString().trim())
                rootObject.add("hashtag", hashList)
                rootObject.addProperty("acceptTime", hrs)
                rootObject.addProperty("isOpenChallenge", isOpenCh)
            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            Log.d("VideoUrl","Doublecheck")
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.createChallenge(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<CareateChallengeModel> {
                override fun onResponse(
                    call: Call<CareateChallengeModel>,
                    response: Response<CareateChallengeModel>
                ) {
                    if (response.code() == 200) {
                        try {
                            Loader.hideLoader(activity)
                            createChallengeModel = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity,createChallengeModel!!.code,createChallengeModel!!.message)
                            if(checkResponse) {
                                if (createChallengeModel!!.code == 200) {
                                    Log.d("VideoUrl", "Success")

                                    nextShowDialog()


                                } else {
                                    Log.d("VideoUrl", "failed")
                                    Utils.showToast(activity, "failed", false)
                                    AndroidUtilities().openAlertDialog(createChallengeModel!!.message,activity)



                                }
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call<CareateChallengeModel>, t: Throwable) {
                    Loader.hideLoader(activity)
                    Log.e("responseError", t.message)
                    Log.d("VideoUrl","Error")
                    Utils.showToast(activity, "Have some problem...", false)
                }
            })
        /*catch (ex: Exception) {
            ex.printStackTrace()
        }*/
    }


    fun getHashtag(hashtag: String) {
        val rootObject = JsonObject()
        try {

            rootObject.addProperty("hashtag", hashtag)
            rootObject.addProperty("pageNumber", pageNo)
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.getHashtag(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<HastagModel> {
                override fun onResponse(
                    call: Call<HastagModel>,
                    response: Response<HastagModel>
                ) {
                    if (response.code() == 200) {
                        try {
                            val hashModel = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity,hashModel.code,hashModel.message)
                            if(checkResponse) {
                                if (hashModel.code == 200) {
                                    hastagAdapter!!.setHashAdapter(activity, hashModel.result)
                                }
                            }else {
                                Utils.showToast(activity, hashModel.message, false)
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call<HastagModel>, t: Throwable) {
                    Log.e("responseError", t.message)
                    Utils.showToast(activity, "Have some problem...", false)
                }
            })

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun didReceivedNotification(id: Int, vararg args: Any?) {

        if (id == NotificationCenter.selectedItem) {
            ishashtagButton = false
            binding!!.hastagView.visibility = View.GONE
            binding!!.rvHastag.visibility = View.GONE
            val hashtagText = binding!!.etHastag.text.toString().trim()

            val hashTagName = args[0] as String

            val lastIndex: Int = hashtagText.lastIndexOf("#")
            val subString: String = hashtagText.substring(lastIndex)
            var newStringComing: String = hashtagText.substring(0, lastIndex)
            if (!TextUtils.isEmpty(subString)) {
                newStringComing = hashtagText.replace(subString, hashTagName)
            }else{
                newStringComing += hashTagName
            }

            binding!!.etHastag.setText( newStringComing + " ")
            binding!!.etHastag.setSelection( binding!!.etHastag.text.length)
        }else if(id == NotificationCenter.trimVideo){
            val trimVideo = args[0] as Uri

            videoUri = trimVideo
            if(videoUri != null){
                videoCompress()
            }
            showView()
        }

    }

    fun showView(){
        var uriVideo : Uri? = null
        if(videoUri != null){
            uriVideo = videoUri
            Glide
                .with(activity)
                .load(uriVideo!!.path)
                .into(binding!!.challengImage)
            binding!!.playVideo.visibility = View.VISIBLE
            binding!!.playVideo.setOnClickListener {
                openPreviewDialog(uriVideo,null)
            }

        }else if(videoFile != null) {
            uriVideo = Uri.fromFile(videoFile)
            Glide
                .with(activity)
                .load(uriVideo!!.path)
                .into(binding!!.challengImage)
            binding!!.playVideo.visibility = View.VISIBLE
            binding!!.playVideo.setOnClickListener {
                openPreviewDialog(uriVideo, null)
            }
        }

    }

    fun openPreviewDialog(video:Uri?,image:Bitmap?){
        val  videoBottomSheetDialog = PreviewBottomDialog.newInstance(activity,video,image, null,null)
        videoBottomSheetDialog.show(activity.supportFragmentManager, PreviewBottomDialog().TAG)
    }


    internal class createChallengeAsyncTask(var fragment: NewChallengeFragment,
                                            var awsUrl:String?,
                                            var thumbNail: String?

    ):AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params:String):String {
            // your async action

            fragment.createChallenge(awsUrl,thumbNail)
            return ""
        }
        override fun onPostExecute(aVoid:String) {

        }
    }


    fun videoCompress(){
        if(videoFile != null){
            inputPath = UtilCompress.getFilePath(activity, Uri.fromFile(videoFile))
        }else if(videoUri != null){
            inputPath =  videoUri?.path!!
        }

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
                isCompress = true
              //  Utils.showToast(activity,"Sucesss",false)
                //getLinkForVideo()
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
        Log.d("VideoUrl",url)
        Log.d("VideoUrl",thumbNail)
        //createChallengeAsyncTask( this, url,thumbNail).execute()
      //  nextShowDialog()
        createChallenge(url,thumbNail)


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
                                createChallenge(cfResposeModel.result .playback.hls,cfResposeModel.result.thumbnail)
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



