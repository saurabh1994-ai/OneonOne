package com.sws.oneonone.dialog

import android.animation.ObjectAnimator
import android.app.Dialog
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.activity.DetailActivity
import com.sws.oneonone.adapter.ProfileAllChallengersAdapter
import com.sws.oneonone.databinding.ProfileActivityBinding
import com.sws.oneonone.fragment.AddChallengersFragment
import com.sws.oneonone.fragment.EditProfileFragment
import com.sws.oneonone.fragment.MoreFragment
import com.sws.oneonone.fragment.ViewExploreFragment
import com.sws.oneonone.model.ProfileChallengeModel
import com.sws.oneonone.model.ProfileChallengeResult
import com.sws.oneonone.model.SignUpModel
import com.sws.oneonone.model.SignUpResultModel
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.util.*
import com.sws.oneonone.viewModel.ProfileVM
import de.hdodenhof.circleimageview.CircleImageView
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import okhttp3.internal.Util
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ProfileBottomDialog  : BottomSheetDialogFragment(){

    private var binding: ProfileActivityBinding? = null
    var profileVM: ProfileVM? = null
    private val handler: Handler = Handler()
    var service: ApiClient? = ApiClient()
    var Otherid = ""
    var allchallengeAdapter : ProfileAllChallengersAdapter? = null
    var challengeList: ArrayList<ProfileChallengeResult> = ArrayList()
    var pageNo = 1
    var type = "3"
    var loading = true
    var activity:BaseActivity? = null
    var userProfile:SignUpResultModel? = null
    val TAG = "ActionBottomDialog"
    var pos:Int? = null
    var followbtn :TextView? = null
    var model: SignUpModel? = null



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
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


        return bottomSheetDialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileVM = ViewModelProviders.of(this).get(ProfileVM::class.java)
     //   NotificationBarColor.profileNotificationBar(activity!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ProfileActivityBinding.bind(
            inflater.inflate(
                R.layout.profile_activity,
                container,
                false
            )
        )
        binding!!.lifecycleOwner = this

        //set data in UI
        if(Otherid.isEmpty()){
             model = AppStaticData().getModel()
            binding!!.profileChellenge = model?.result
            ShowingImage.showImage(activity!!, model?.result?.profileImg, binding!!.profileImage)
            profileVM?.fullName?.value = model?.result?.fullName
            profileVM?.userName?.value = model?.result?.username
            profileVM?.points?.value = model?.result?.points.toString()
            binding!!.tvedit.text = getText(R.string.edit)
            binding!!.tvedit.setTextColor(ContextCompat.getColor(activity!!,R.color.pink))
            binding!!.tvedit.setCompoundDrawablesWithIntrinsicBounds(R.drawable.edit_icon, 0, 0, 0)

        }else{
            binding!!.ivMeun.visibility = View.GONE
        }
        // set Activity
        profileVM?.activity = activity
        // Atteched view Model data to xml object
        binding!!.profileVM = profileVM
        initView()
        return binding!!.root
    }
    private fun initView() {
        // check has Observers
        if (!profileVM?.comman?.hasObservers()!!) {
            profileVM?.comman?.observe(this, Observer { str ->
                when (str) {
                    "back" -> {
                       // onBackPressed()
                        dismiss()
                    }

                    "Edit" ->{
                        if(Otherid.isEmpty()){
                            activity?.replaceFragment(EditProfileFragment())
                            dismiss()
                        }else{
                            if (Utils.isNetworkAvailable(activity)) {
                                val flip: ObjectAnimator =
                                    ObjectAnimator.ofFloat(binding!!.rlfollowbtn, "rotationY", 0f, 360f)
                                flip.duration = 3000
                                flip.start()
                                if(userProfile != null){
                                    if (userProfile?.isFollow!!) {
                                        followUnfollowAsyncTask(
                                            this,
                                            "2",
                                            userProfile?.id
                                        ).execute()
                                    } else {
                                        followUnfollowAsyncTask(
                                            this,
                                            "1",
                                            userProfile?.id
                                        ).execute()
                                    }

                                }

                            } else {
                                SnackBar().internetSnackBar(activity)
                            }
                        }

                    }

                    "More" -> {
                        activity?.replaceFragment(MoreFragment())
                        dismiss()
                    }
                }
            })
        }

        pageNo = 1
        getAllProfileChallenges(pageNo)

        binding!!.profileImage.setOnClickListener {
            if(!model?.result?.profileImg.isNullOrEmpty()) {
                goToDetail(binding!!.profileImage, arrayListOf(model?.result?.profileImg!!), 0)
            } else {
                Utils.showToast(activity!!, "profile is not available", false)
            }
        }

    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding!!.konfettiView.build()
            .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
            .setDirection(0.0, 359.0)
            .setSpeed(1f, 3f)
            .setFadeOutEnabled(true)
            .setTimeToLive(2000L)
            .addShapes(Shape.Square, Shape.Circle)
            .addSizes(Size(15))
            .setPosition(-50f, binding!!.konfettiView.width + 50f, -50f, -50f)
            .streamFor(300, 500L)
        handler.postDelayed(runnable, 5000)

        binding!!.bottomNavigationProfile.itemIconTintList = null
        binding!!.bottomNavigationProfile.menu.getItem(0).isChecked = true
        binding!!.bottomNavigationProfile.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_my_challengers -> {
                    type = "3"
                    pageNo = 1
                    getAllProfileChallenges(pageNo)
                    getMoreItems(null)
                    binding!!.bottomNavigationProfile.menu.getItem(0).isChecked = true

                } //binding!!.viewpagerProfile.currentItem = 0
                R.id.action_all_challengers -> {
                    type = "2"
                    pageNo = 1
                    getMoreItems(null)
                    getAllProfileChallenges(pageNo)
                    // binding!!.viewpagerProfile.currentItem = 1
                    binding!!.bottomNavigationProfile.menu.getItem(1).isChecked = true
                }
                R.id.action_won_challengers -> {
                    type = "1"
                    pageNo = 1
                    getMoreItems(null)
                    getAllProfileChallenges(pageNo)
                    binding!!.bottomNavigationProfile.menu.getItem(2).isChecked = true

                }//binding!!.viewpagerProfile.currentItem = 2
            }
            false
        }

        val explorelayout = GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
        binding!!.rvChallenges.layoutManager = explorelayout
        explorelayout.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                // 2 column size for first row
                return 1
            }
        }
        allchallengeAdapter = ProfileAllChallengersAdapter()
        binding!!.rvChallenges.adapter = allchallengeAdapter

        allchallengeAdapter!!.setListerner(object  : ProfileAllChallengersAdapter.ClickListerner{
            override fun onSuccess(uid: String?) {
                activity!!.replaceFragment(ViewExploreFragment.newInstance(uid))
                dismiss()

            }

        })

        binding!!.profileScroll.setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                // here where the trick is going
                if (loading) {
                    pageNo++
                    getAllProfileChallenges(pageNo)
                    loading = false
                }
            }
        }

        binding!!.llfollowers.setOnClickListener {
            dismiss()
            activity?.replaceFragment(AddChallengersFragment.newInstance(activity!!,false,true,Otherid))

        }

        binding!!.llfollowings.setOnClickListener {
            dismiss()
            activity?.replaceFragment(AddChallengersFragment.newInstance(activity!!,true,false,Otherid))
        }
    }


    private val runnable: Runnable = object : Runnable {
        override fun run() {
            binding!!.dancer.visibility = View.GONE

        }

    }



    fun getAllProfileChallenges(page: Int) {
        try {
            val rootObject = JsonObject()
            try {
                rootObject.addProperty("userId", AppStaticData().getModel()!!.result?.id)
                rootObject.addProperty("type", type)
                rootObject.addProperty("otherUserId",Otherid)
                rootObject.addProperty("pageNumber", pageNo)
            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.getUserProfileChallenge(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<ProfileChallengeModel> {
                override fun onResponse(
                    call: Call<ProfileChallengeModel>,
                    response: Response<ProfileChallengeModel>
                ) {
                    if (response.code() == 200) {
                        try {
                            val mChallengeList = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity!!,mChallengeList.code!!,"")
                            if(checkResponse) {
                                if (mChallengeList.code == 200) {

                                    if (page == 1) {
                                        challengeList.clear()
                                    }
                                    if (Otherid.isNotEmpty()) {
                                        userProfile = mChallengeList.userProfile
                                        setDataViews(mChallengeList.userProfile!!)
                                    }
                                    if (!mChallengeList.result.isNullOrEmpty()) {
                                        binding!!.llNoPost.visibility = View.GONE
                                        for (i in 0 until mChallengeList.result?.size!!) {
                                            val list = ProfileChallengeResult()
                                            list.setProfileChallenge(mChallengeList.result!![i])
                                            challengeList.add(list)
                                        }


                                        if(Otherid.isNotEmpty()){
                                            if(userProfile?.isPrivate!!){
                                                if( userProfile?.isFollow == false && userProfile?.isOtherFollow == false) {
                                                    binding!!.llNoPost.visibility = View.VISIBLE
                                                    binding!!.tvpostMessage.text = "This account is private"
                                                } else if (userProfile?.isFollow == false && userProfile?.isOtherFollow == true) {
                                                    binding!!.llNoPost.visibility = View.VISIBLE
                                                    binding!!.tvpostMessage.text = "This account is private"
                                                }  else if (userProfile?.isFollow == true && userProfile?.isOtherFollow == false) {
                                                    binding!!.llNoPost.visibility = View.VISIBLE
                                                    binding!!.tvpostMessage.text = "This account is private"
                                                }else{
                                                    getMoreItems(mChallengeList.result)
                                                }
                                            }else{
                                                getMoreItems(mChallengeList.result)
                                            }

                                        }else{
                                            getMoreItems(mChallengeList.result)
                                        }


                                    } else {
                                        if (pageNo == 1) {
                                            binding!!.llNoPost.visibility = View.VISIBLE
                                        }

                                    }


                                }
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call<ProfileChallengeModel>, t: Throwable) {
                    //Loader?.hideLoader(activity!!)
                    Log.e("responseError", t.message)
                    Utils.showToast(activity!!, "Have some problem...", false)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    companion object {

        fun newInstance(activity: BaseActivity,Otherid: String?,pos:Int?,followbtn:TextView?) = ProfileBottomDialog().apply {
            this.activity = activity
            this.Otherid = Otherid!!
            this.pos = pos
            this.followbtn = followbtn

        }
    }

    fun setDataViews(model : SignUpResultModel){
        profileVM?.userName?.value = model.username
        ShowingImage.showImage(activity!!, model.profileImg, binding!!.profileImage)
        profileVM?.fullName?.value = model.fullName
        profileVM?.points?.value = model.points.toString()
        binding!!.profileChellenge = model
        if(model.isFollow!!){
            binding!!.tvedit.setTextColor(ContextCompat.getColor(activity!!,R.color.pink))
            binding!!.tvedit.text =  getText(R.string.following)
            binding!!.tvedit.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sub_icon, 0, 0, 0)
        }else{
            binding!!.tvedit.text = getText(R.string.follow)
            binding!!.tvedit.setTextColor(ContextCompat.getColor(activity!!,R.color.black))
            binding!!.tvedit.setCompoundDrawablesWithIntrinsicBounds(R.drawable.add_sub_icon, 0, 0, 0)
        }
    }

    fun getMoreItems(list : java.util.ArrayList<ProfileChallengeResult>?) {
        //after fetching your data assuming you have fetched list in your
        // recyclerview adapter assuming your recyclerview adapter is
        //rvAdapter
        //  after getting your data you have to assign false to isLoading
        loading = true
        if(pageNo == 1){
            allchallengeAdapter!!.setNotificationAdapter(activity!!,list)
        }else{
            allchallengeAdapter!!.addData(list)
        }



    }



    fun followUnFollow( type: String?, followId: String?) {
        try {
            val rootObject = JsonObject()
            try {
                rootObject.addProperty("userId",  AppStaticData().getModel()?.result?.id)
                rootObject.addProperty("followId",  followId)
                rootObject.addProperty("type",  type)
            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.setFollowUnFollow(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<SignUpModel> {
                override fun onResponse(call: Call<SignUpModel>, response: Response<SignUpModel>) {
                    if (response.code() == 200) {
                        try {
                            val mSignUpModel = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity!!,mSignUpModel.code!!,mSignUpModel.message)
                            if(checkResponse) {
                                if (mSignUpModel.code == 200) {
                                    if (pos != null) {
                                        NotificationCenter.getInstance()
                                            .postNotificationName(
                                                NotificationCenter.isFollow,
                                                type,
                                                pos,
                                                followbtn
                                            )
                                    }

                                    if (type.equals("1")) {
                                        userProfile?.isFollow = true
                                        binding!!.tvedit.text = getText(R.string.following)
                                        binding!!.tvedit.setTextColor(ContextCompat.getColor(activity!!, R.color.pink))
                                        binding!!.tvedit.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sub_icon, 0, 0, 0)
                                    } else {
                                        userProfile?.isFollow = false
                                        binding!!.tvedit.text = getText(R.string.follow)
                                        binding!!.tvedit.setTextColor(ContextCompat.getColor(activity!!, R.color.black))
                                        binding!!.tvedit.setCompoundDrawablesWithIntrinsicBounds(R.drawable.add_sub_icon, 0, 0, 0)
                                    }
                                }
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
                override fun onFailure(call: Call<SignUpModel>, t: Throwable) {
                    Log.e("responseError", t.message)
                    Utils.showToast(activity!!, "Have some problem...", false)
                    Loader.hideLoader(activity!!)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }



    internal class followUnfollowAsyncTask(var fragment: ProfileBottomDialog,
                                            var type:String?,
                                            var id: String?

    ): AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params:String):String {
            // your async action

            fragment.followUnFollow( type, id)
            return ""
        }
        override fun onPostExecute(aVoid:String) {

        }
    }


    private fun goToDetail(
        sharedElement: CircleImageView,
        urls: ArrayList<String>,
        initialPos: Int
    ) {
        if (Pref.useSharedElements) {



            activity!!.startActivity(
                DetailActivity.createIntent(
                    activity!!,
                    urls,
                    initialPos
                )

            )
        } else {
            startActivity(
                DetailActivity.createIntent(
                    activity!!,
                    urls,
                    initialPos
                )
            )
            activity!!.overridePendingTransition(R.anim.fade_in_fast, 0)
        }
    }



}