package com.sws.oneonone.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.adapter.ExploreAdapter
import com.sws.oneonone.adapter.YourChallengesAdapter
import com.sws.oneonone.databinding.FragmentExploreBinding
import com.sws.oneonone.dialog.ChallengeChooseBottomDialog
import com.sws.oneonone.dialog.PreviewBottomDialog
import com.sws.oneonone.model.*
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.util.*
import com.sws.oneonone.viewModel.ExploreVM
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class ExploreFragment: BaseFragment(),NotificationCenter.NotificationCenterDelegate {
    private var binding: FragmentExploreBinding? = null
    var exploreVM: ExploreVM? = null
    var explorelayout: GridLayoutManager? = null
    var uId = ""
    var isFromChallenge = false
    var exploreAdapter: ExploreAdapter? = null
    var loading = true
    var pageNo = 1
    var callFunction: Boolean = true

    var service: ApiClient? = ApiClient()

    var ForVideo = false
    var yourChallengesAdapter:YourChallengesAdapter? = null
    var challengerList: ArrayList<ArrayList<ExploreResult>>? = null
    var challengerListAdapter: ArrayList<ArrayList<ExploreResult>>? = null
    private var exploreList: ArrayList<ExploreResult> = ArrayList<ExploreResult>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exploreVM = ViewModelProviders.of(this).get(ExploreVM::class.java)
        callFunction = true
        if(uId.isNotBlank() || uId.isNotEmpty()){
            activity.replaceFragment(ViewExploreFragment.newInstance(uId))
        }


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        NotificationBarColor.WhiteNotificationBar(activity)
        binding = FragmentExploreBinding.bind(inflater.inflate(R.layout.fragment_explore, container, false))
        binding!!.lifecycleOwner = this

        NotificationCenter.getInstance().addObserver(this, NotificationCenter.viewChallenge)

        binding!!.BtnAllChallenges?.setOnClickListener {
            if (exploreVM?.IsOpenChallenge?.value!!){
                exploreVM?.onAllChallengesClick()
                pageNo = 1
                ExploreListData("",pageNo)
            }
        }
        binding!!.BtnOpenChallenges?.setOnClickListener {
            if (!exploreVM?.IsOpenChallenge?.value!!){
                exploreVM?.onOpenChallengesClick()
                pageNo = 1
                ExploreListData("",pageNo)
            }
        }

        return binding!!.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        NotificationBarColor.WhiteNotificationBar(activity)
        //get app data
        val model: SignUpModel? = AppStaticData().getModel()
        //set Image in imageView
        ShowingImage.showImage(activity, model?.result?.profileImg, binding!!.userProfile)
        ShowingImage.showImage(activity, model?.result?.profileImg, binding!!.ivAdd)

        // set Activity
        exploreVM?.activity = activity
        exploreVM?.IsOpenChallenge?.value = false
        // Atteched view Model data to xml object
        binding!!.exploreVM = exploreVM
        initView()

        if (Utils.isNetworkAvailable(activity)) {
            YourChallengersData()
        } else {
            SnackBar().internetSnackBar(activity)
        }
        pageNo = 1
        ExploreListData("", pageNo)
        /* if(callFunction) {

             callFunction = false
         }*/
/*
        if(isFromChallenge && !ForVideo){
            binding!!.uploadingProgressBar.visibility = View.VISIBLE
        }else{
            binding!!.uploadingProgressBar.visibility = View.GONE
        }*/

        binding!!.etExplor.addTextChangedListener(object  :TextWatcher{
            override fun afterTextChanged(s: Editable?) {



            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                pageNo = 1
                ExploreListData(s.toString(),pageNo)
            }

        })

        binding!!.llAddChallenge.setOnClickListener {
            val  challengeChooseBottomDialog = ChallengeChooseBottomDialog.newInstance(activity,false,null)
            challengeChooseBottomDialog.show(activity.supportFragmentManager, ChallengeChooseBottomDialog().TAG)
            // activity.replaceFragment(CameraFragment())
        }

        binding!!.swipeRefresh.setOnRefreshListener {
            binding!!.swipeRefresh.isRefreshing = false
            pageNo = 1
            if (Utils.isNetworkAvailable(activity)) {
                YourChallengersData()
            } else {
                SnackBar().internetSnackBar(activity)
            }
            ExploreListData("",pageNo)

        }

        explorelayout = GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
        explorelayout?.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                // 2 column size for first row
                return if (position%3 == 0) 2 else 1
            }
        }
        binding!!.rvexplore.layoutManager = explorelayout
        exploreAdapter = ExploreAdapter()
        binding!!.rvexplore.adapter = exploreAdapter

        binding!!.scroll.setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                Log.i(TAG, "BOTTOM SCROLL")
                // here where the trick is going
                if (loading) {
                    pageNo++
                    ExploreListData("",pageNo)
                    loading = false
                }
            }
        }

        binding!!.llMyChallenge.setOnClickListener {

            val  videoBottomSheetDialog = PreviewBottomDialog.newInstance(activity,null,null,  challengerList!![0],null)
            videoBottomSheetDialog.show(activity.supportFragmentManager, PreviewBottomDialog().TAG)
        }



        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        binding!!.rvChallenges.layoutManager = layoutManager
        binding!!.rvChallenges.isNestedScrollingEnabled = false
        yourChallengesAdapter = YourChallengesAdapter()
        binding!!.rvChallenges.adapter = yourChallengesAdapter
    }


    private fun initView() {
        // check has Observers
        if (!exploreVM?.comman?.hasObservers()!!) {
            exploreVM?.comman?.observe(this, object : Observer<ArrayList<ExploreResult>> {
                override fun onChanged(@Nullable exploreList: ArrayList<ExploreResult>) {



                }
            })
        }

        /*  if(!exploreVM?.challengerMLD?.hasObservers()!!){
              exploreVM?.challengerMLD?.observe(this,
                  Observer { challengersList ->
                      // Explore List code manage images

                      setChallengersList(challengersList)

                  })
          }*/
    }


    companion object {



        fun newInstance(uid: String?,isFromChallenge : Boolean,ForVideo:Boolean) = ExploreFragment().apply {
            this.uId = uid!!
            this.isFromChallenge = isFromChallenge
            this.ForVideo = ForVideo

        }
    }

    override fun didReceivedNotification(id: Int, vararg args: Any?) {
    /*    if(id == NotificationCenter.challengeSucceed){
            binding!!.uploadingProgressBar.visibility = View.GONE
            pageNo = 1
            if (Utils.isNetworkAvailable(activity)) {
                YourChallengersData()
            } else {
                SnackBar().internetSnackBar(activity)
            }
            ExploreListData("",pageNo)
            isFromChallenge = false
        }else if(id == NotificationCenter.challengeFailed){
            val errorMessage = args[0] as String
            AndroidUtilities().openAlertDialog(errorMessage,activity)
            binding!!.uploadingProgressBar.visibility = View.GONE
        }else*/ if(id == NotificationCenter.viewChallenge){
            if (Utils.isNetworkAvailable(activity)) {
                YourChallengersData()
            } else {
                SnackBar().internetSnackBar(activity)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.viewChallenge)
    }


    fun getMoreItems(exploreList: ArrayList<ExploreResult>) {


        if(pageNo == 1){
            if(exploreList.count() == 10){
                loading = true
            }
            exploreAdapter!!.setExploreList(activity, exploreList)
        }

        else {
            loading = true
            exploreAdapter!!.addData(exploreList)
        }




    }

    fun setChallengersList(){
        if(challengerListAdapter!![0][0].createdBy.equals(AppStaticData().getModel()?.result?.id)){
            binding!!.llAddChallenge.visibility = View.GONE
            binding!!.llMyChallenge.visibility = View.VISIBLE
            if(challengerListAdapter!![0][0].video!!.isNotEmpty()){
                ShowingImage.showImage(
                    activity,
                    challengerListAdapter!![0][0].thumbnails,
                    binding!!.ivPic
                )
            }else{
                ShowingImage.showImage(
                    activity,
                    challengerListAdapter!![0][0].image,
                    binding!!.ivPic
                )
            }
            if(challengerListAdapter!![0][0].isView!!){
                binding!!.ivPic.setBackgroundResource(R.drawable.image_border_gray)
            }

            challengerListAdapter!!.removeAt(0)
        }


        yourChallengesAdapter!!.setChallengList(activity,challengerListAdapter!!)
    }


    // your Challengers List API
    fun YourChallengersData() {
        try {
            val rootObject = JsonObject()
            try {
                rootObject.addProperty("userId",  AppStaticData().getModel()?.result?.id)


            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.getCreatedChallenges(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<CreatedChallengeDataModel> {
                override fun onResponse(call: Call<CreatedChallengeDataModel>, response: Response<CreatedChallengeDataModel>) {
                    if (response.code() == 200) {
                        challengerList = ArrayList()
                        challengerListAdapter = ArrayList()
                        challengerList!!.clear()
                        challengerListAdapter!!.clear()
                        try {
                            val mExploreModel = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity,mExploreModel.code!!,"")
                            if(checkResponse){
                                if (mExploreModel.code == 200) {
                                    val list: ArrayList<ArrayList<ExploreResult>> = ArrayList()
                                    if (!mExploreModel.result.isNullOrEmpty()) {
                                        for (i in 0 until mExploreModel.result?.size!!) {
                                            challengerList!!.add(mExploreModel.result!![i])

                                        }
                                    }
                                    challengerListAdapter = mExploreModel.result!!
                                    setChallengersList()

                                }
                            }

                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
                override fun onFailure(call: Call<CreatedChallengeDataModel>, t: Throwable) {
                    Log.e("responseError", t.message)
                    Utils.showToast(activity!!, "Have some problem...", false)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun ExploreListData(hashtag: String,pageNo: Int) {
        try {
            val rootObject = JsonObject()
            try {
                rootObject.addProperty("userId",  AppStaticData().getModel()?.result?.id)
                rootObject.addProperty("pageNumber",  pageNo)
                rootObject.addProperty("hashtag",hashtag)
                if (exploreVM?.IsOpenChallenge?.value!!){
                    rootObject.addProperty("type", "1")
                } else {
                    rootObject.addProperty("type", "0")
                }
            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.getAllChallenges(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<ExploreModel> {
                override fun onResponse(call: Call<ExploreModel>, response: Response<ExploreModel>) {
                    if (response.code() == 200) {
                        try {
                            val mExploreModel = response.body()!!
                            val response =  AndroidUtilities().apisResponse(activity!!,mExploreModel.code!!,"")
                            if(response){
                                if (mExploreModel.code == 200) {
                                    if(pageNo == 1){
                                        exploreList.clear()
                                    }
                                    if (!mExploreModel.result.isNullOrEmpty()) {
                                        for (i in 0 until mExploreModel.result?.size!!) {
                                            val list = ExploreResult()
                                            list.setExploreData(mExploreModel.result!![i])
                                            exploreList.add(list)
                                        }
                                        // comman.value = exploreList


                                        getMoreItems(mExploreModel.result!!)
                                    }
                                }
                            }

                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
                override fun onFailure(call: Call<ExploreModel>, t: Throwable) {
                    Log.e("responseError", t.message)
                    Utils.showToast(activity!!, "Have some problem...", false)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


}