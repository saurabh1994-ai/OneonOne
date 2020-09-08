package com.sws.oneonone.fragment

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager.widget.ViewPager
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.adapter.ProfileAllChallengersAdapter
import com.sws.oneonone.adapter.ViewPagerAdapterProfile
import com.sws.oneonone.databinding.ProfileActivityBinding
import com.sws.oneonone.model.ProfileChallengeModel
import com.sws.oneonone.model.ProfileChallengeResult
import com.sws.oneonone.model.SignUpModel
import com.sws.oneonone.model.SignUpResultModel
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.util.*
import com.sws.oneonone.viewModel.ProfileVM
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class ProfileFragment: BaseFragment() {
    private var binding: ProfileActivityBinding? = null
    var profileVM: ProfileVM? = null

    var allChalengersFragment: AllChalengersFragment? = null
    var myChalengersFragment: MyChalengersFragment? = null
    var wonChalengersFragment: WonChalengersFragment? = null
    var prevMenuItem: MenuItem? = null
    private val handler: Handler = Handler()
    var service: ApiClient? = ApiClient()
    var Otherid = ""
    var allchallengeAdapter : ProfileAllChallengersAdapter? = null
    var challengeList: ArrayList<ProfileChallengeResult> = ArrayList()
    var pageNo = 1
    var type = "3"
    var loading = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileVM = ViewModelProviders.of(this).get(ProfileVM::class.java)
        NotificationBarColor.profileNotificationBar(activity)
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
            val model: SignUpModel? = AppStaticData().getModel()
            binding!!.profileChellenge = model?.result
            ShowingImage.showImage(activity, model?.result?.profileImg, binding!!.profileImage)
            profileVM?.fullName?.value = model?.result?.fullName
            profileVM?.userName?.value = model?.result?.username
            profileVM?.points?.value = model?.result?.points.toString()
        }else{
            binding!!.ivMeun.visibility = View.GONE
            binding!!.llEdit.visibility = View.GONE
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
                        onBackPressed()
                    }
                }
            })
        }

             pageNo = 1
        getAllProfileChallenges(pageNo)

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
                R.id.action_my_challengers ->{
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

       /* // Profile view pager Listener
        binding!!.viewpagerProfile.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (prevMenuItem != null) {
                    prevMenuItem!!.isChecked = false
                } else {
                    binding!!.bottomNavigationProfile.menu.getItem(0).isChecked = false
                }
                Log.d("page", "onPageSelected: $position")
                binding!!.bottomNavigationProfile.menu.getItem(position).isChecked = true
                prevMenuItem = binding!!.bottomNavigationProfile.menu.getItem(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        setupViewPager(binding!!.viewpagerProfile)*/

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
      /*  binding!!.profileScroll.viewTreeObserver
            .addOnScrollChangedListener {
                val views =
                    binding!!.profileScroll.getChildAt(binding!!.profileScroll.childCount - 1) as View
                val diff: Int = views.bottom - (binding!!.profileScroll.height + binding!!.profileScroll
                    .scrollY)
                if (diff == 0) {
                    // your pagination code
                    pageNo++
                    getAllProfileChallenges(pageNo)
                }
            }*/

        binding!!.profileScroll.setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                Log.i(TAG, "BOTTOM SCROLL")
                // here where the trick is going
                if (loading) {
                    pageNo++
                    getAllProfileChallenges(pageNo)
                    loading = false
                }
            }
        }

    }

    // setup view pager (fragment)
    private fun setupViewPager(viewPager: ViewPager?) {
        val adapter = ViewPagerAdapterProfile(childFragmentManager)
        allChalengersFragment = AllChalengersFragment()
        myChalengersFragment = MyChalengersFragment()
        wonChalengersFragment = WonChalengersFragment()
        adapter.addFragment(allChalengersFragment!!)
        adapter.addFragment(myChalengersFragment!!)
        adapter.addFragment(wonChalengersFragment!!)
        viewPager!!.adapter = adapter
        viewPager.currentItem = 0
    }

    override fun onResume() {
        super.onResume()
        NotificationBarColor.profileNotificationBar(activity)
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
                                    if (!mChallengeList.result.isNullOrEmpty()) {
                                        for (i in 0 until mChallengeList.result?.size!!) {
                                            val list = ProfileChallengeResult()
                                            list.setProfileChallenge(mChallengeList.result!![i])
                                            challengeList.add(list)
                                        }

                                        getMoreItems(challengeList)

                                    }
                                    if (Otherid.isNotEmpty()) {
                                        setDataViews(mChallengeList.userProfile!!)
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
                    Utils.showToast(activity, "Have some problem...", false)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    companion object {

        fun newInstance(Otherid: String?) = ProfileFragment().apply {
            this.Otherid = Otherid!!

        }
    }

    fun setDataViews(model : SignUpResultModel){
        profileVM?.userName?.value = model.username
        ShowingImage.showImage(activity, model.profileImg, binding!!.profileImage)
        profileVM?.fullName?.value = model.fullName
        profileVM?.points?.value = model.points.toString()
        binding!!.profileChellenge = model
    }

    fun getMoreItems(list : java.util.ArrayList<ProfileChallengeResult>?) {
        //after fetching your data assuming you have fetched list in your
        // recyclerview adapter assuming your recyclerview adapter is
        //rvAdapter
        //  after getting your data you have to assign false to isLoading
        loading = true
        if(pageNo == 1){
            allchallengeAdapter!!.setNotificationAdapter(activity,list)
        }else{
            allchallengeAdapter!!.addData(list)
        }

    }
}