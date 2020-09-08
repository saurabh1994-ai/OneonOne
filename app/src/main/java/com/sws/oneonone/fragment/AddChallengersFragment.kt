package com.sws.oneonone.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.adapter.AddChallengerdAdapter
import com.sws.oneonone.databinding.FragmentAddChallengersBinding
import com.sws.oneonone.dialog.ProfileBottomDialog
import com.sws.oneonone.model.MyFollowingModel
import com.sws.oneonone.model.MyFollowingResult
import com.sws.oneonone.model.ToolbarModel
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.util.*
import com.sws.oneonone.viewModel.ToolbarVM
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class AddChallengersFragment: BaseFragment() {

    private var binding: FragmentAddChallengersBinding? = null
    var toolbarVM: ToolbarVM? = null
    var isLastPage: Boolean = false
    var isLoading: Boolean = false
    var pageNo = 1
    var addChallengerdAdapter :AddChallengerdAdapter? = null
    var service: ApiClient? = ApiClient()
    private var followingList: ArrayList<MyFollowingResult> = ArrayList<MyFollowingResult>()
    var isFollowers = false
    var isFollowing = false
    var title = ""
    var otherUserId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // NotificationBar change backgraound/text color
        toolbarVM = ViewModelProviders.of(this).get(ToolbarVM::class.java)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAddChallengersBinding.bind(inflater.inflate(R.layout.fragment_add_challengers, container, false))
        binding!!.lifecycleOwner = this
        return binding!!.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Tool bar model
        if(isFollowers){
            title = "Followers"
        }else if(isFollowing){
            title = "Following"
        }else{
            title = "Add Challengers"
        }
        val toolbarModel = ToolbarModel(View.VISIBLE,title, View.GONE,View.VISIBLE,View.VISIBLE,View.GONE, 1, "","",View.GONE)
        binding!!.layoutToolbar.toolbarModel = toolbarModel
        binding!!.layoutToolbar.lifecycleOwner = this

        toolbarVM?.activity = activity
        binding!!.layoutToolbar.toolbarVM = toolbarVM



        // Call API function
        if (Utils.isNetworkAvailable(activity)) {
            if(isFollowing)
                myFollowings(pageNo,otherUserId)
            else if(isFollowers)
                myFollowers(pageNo,otherUserId)
            else
            setMyFollowings(pageNo, "")
        } else {
            SnackBar().internetSnackBar(activity)
        }
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding!!.rvAddChallenges.layoutManager = layoutManager
        addChallengerdAdapter = AddChallengerdAdapter()
        binding!!.rvAddChallenges.adapter = addChallengerdAdapter
        binding!!.rvAddChallenges.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

            override fun loadMoreItems() {
                isLoading = true
                pageNo++
                Log.d("pageNo+++++",pageNo.toString())
                //you have to call loadmore items to get more data
                if (Utils.isNetworkAvailable(activity)) {
                     if(isFollowing)
                         myFollowings(pageNo,otherUserId)
                    else if(isFollowers)
                      myFollowers(pageNo,otherUserId)
                    else
                    setMyFollowings(pageNo, "")
                } else {
                    SnackBar().internetSnackBar(activity)
                }

            }
        })

        binding!!.searchChallengers.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
               pageNo = 1

                if(s!!.isEmpty()){
                    isLoading = false
                    setMyFollowings( pageNo, "")
                }else{
                    setMyFollowings(pageNo, s.toString())
                }

            }

        })
        initView()
    }

    private fun initView() {
        // check has Observers
        if (!toolbarVM?.comman?.hasObservers()!!) {
            toolbarVM?.comman?.observe(this, object : Observer<String> {
                override fun onChanged(@Nullable str: String) {
                    when (str) {
                        "onBack" -> onBackPressed()
                    }
                }
            })
        }


    }

    fun getMoreItems(list : ArrayList<MyFollowingResult>?) {
        //after fetching your data assuming you have fetched list in your
        // recyclerview adapter assuming your recyclerview adapter is
        //rvAdapter
        //  after getting your data you have to assign false to isLoading


        if(pageNo == 1){
            if(list!!.count() == 10){
                isLoading = false
            }
            addChallengerdAdapter!!.setchallengerAdapter(activity,list)
        }else{
            isLoading = false
            addChallengerdAdapter!!.addData(list)
        }



    }


    // your Challengers List API
    fun setMyFollowings(page: Int, searchText: String?) {
        try {
            val rootObject = JsonObject()
            try {
                rootObject.addProperty("userId",  AppStaticData().getModel()?.result?.id)
                rootObject.addProperty("pageNumber",  page)
                rootObject.addProperty("username",  searchText)
            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.getAllUserList(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<MyFollowingModel> {
                override fun onResponse(call: Call<MyFollowingModel>, response: Response<MyFollowingModel>) {
                    if (response.code() == 200) {


                        try {
                            val mMyFollowingModel = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity!!,mMyFollowingModel.code!!,mMyFollowingModel.message)
                            if(checkResponse){
                                if (mMyFollowingModel.code == 200) {
                                    if (page == 1) {
                                        followingList.clear()
                                    }
                                    if (!mMyFollowingModel.result.isNullOrEmpty()) {
                                        for (i in 0 until mMyFollowingModel.result?.size!!) {
                                            val list = MyFollowingResult()
                                            list.setMyFollowingResult(mMyFollowingModel.result!![i])
                                            followingList.add(list)
                                        }

                                        getMoreItems(mMyFollowingModel.result)
                                    }
                                }

                            } else {
                                Utils.showToast(activity!!, mMyFollowingModel.message!!, false)
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
                override fun onFailure(call: Call<MyFollowingModel>, t: Throwable) {
                    Log.e("responseError", t.message)
                    Utils.showToast(activity!!, "Have some problem...", false)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun myFollowings(page: Int,otherUserId:String){
        try {
            val rootObject = JsonObject()
            try {
                rootObject.addProperty("userId",  AppStaticData().getModel()?.result?.id)
                rootObject.addProperty("pageNumber",  page)
                rootObject.addProperty("otherUserId",otherUserId)
            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.myFollowings(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<MyFollowingModel> {
                override fun onResponse(call: Call<MyFollowingModel>, response: Response<MyFollowingModel>) {
                    if (response.code() == 200) {


                        try {
                            val mMyFollowingModel = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity!!,mMyFollowingModel.code!!,mMyFollowingModel.message)
                            if(checkResponse){
                                if (mMyFollowingModel.code == 200) {
                                    if (page == 1) {
                                        followingList.clear()
                                    }
                                    if (!mMyFollowingModel.followings.isNullOrEmpty()) {
                                        for (i in 0 until mMyFollowingModel.followings?.size!!) {
                                            val list = MyFollowingResult()
                                            list.setMyFollowingResult(mMyFollowingModel.followings!![i])
                                            followingList.add(list)
                                        }

                                        getMoreItems(mMyFollowingModel.followings!!)
                                    }
                                }

                            } else {
                                Utils.showToast(activity!!, mMyFollowingModel.message!!, false)
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
                override fun onFailure(call: Call<MyFollowingModel>, t: Throwable) {
                    Log.e("responseError", t.message)
                    Utils.showToast(activity!!, "Have some problem...", false)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun myFollowers(page: Int,otherUserId:String){
        try {
            val rootObject = JsonObject()
            try {
                rootObject.addProperty("userId",  AppStaticData().getModel()?.result?.id)
                rootObject.addProperty("pageNumber",  page)
                rootObject.addProperty("otherUserId",otherUserId)
            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.myFollowers(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<MyFollowingModel> {
                override fun onResponse(call: Call<MyFollowingModel>, response: Response<MyFollowingModel>) {
                    if (response.code() == 200) {


                        try {
                            val mMyFollowingModel = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity!!,mMyFollowingModel.code!!,mMyFollowingModel.message)
                            if(checkResponse){
                                if (mMyFollowingModel.code == 200) {
                                    if (page == 1) {
                                        followingList.clear()
                                    }
                                    if (!mMyFollowingModel.followers.isNullOrEmpty()) {
                                        for (i in 0 until mMyFollowingModel.followers?.size!!) {
                                            val list = MyFollowingResult()
                                            list.setMyFollowingResult(mMyFollowingModel.followers!![i])
                                            followingList.add(list)
                                        }

                                        getMoreItems(mMyFollowingModel.followers!!)
                                    }
                                }

                            } else {
                                Utils.showToast(activity!!, mMyFollowingModel.message!!, false)
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
                override fun onFailure(call: Call<MyFollowingModel>, t: Throwable) {
                    Log.e("responseError", t.message)
                    Utils.showToast(activity!!, "Have some problem...", false)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    companion object {

        fun newInstance(activity: BaseActivity,isFollowing:Boolean,isFollowers : Boolean,otherUserId:String) = AddChallengersFragment().apply {
            this.activity = activity
            this.isFollowing = isFollowing
            this.isFollowers = isFollowers
            this.otherUserId = otherUserId

        }
    }
}
