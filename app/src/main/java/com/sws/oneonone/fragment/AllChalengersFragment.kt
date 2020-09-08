package com.sws.oneonone.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.adapter.MyChallengersAdapter
import com.sws.oneonone.adapter.ProfileAllChallengersAdapter
import com.sws.oneonone.databinding.FragmentAllChallengersBinding
import com.sws.oneonone.model.*
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.util.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class AllChalengersFragment: BaseFragment() {
    private var binding: FragmentAllChallengersBinding? = null
    var service: ApiClient? = ApiClient()
    var challengeList: ArrayList<ProfileChallengeResult> = ArrayList()
    var pageNo = 1
    var isLastPagecheck: Boolean = false
    var isLoadingcheck: Boolean = false
    var allchallengeAdapter : ProfileAllChallengersAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAllChallengersBinding.bind(
            inflater.inflate(
                R.layout.fragment_all_challengers,
                container,
                false
            )
        )
        return binding!!.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val explorelayout = GridLayoutManager(activity, 2)
        explorelayout.isAutoMeasureEnabled = true
        binding!!.rvChallenges.layoutManager = explorelayout
        binding!!.rvChallenges.setHasFixedSize(false)
        allchallengeAdapter = ProfileAllChallengersAdapter()
        binding!!.rvChallenges.adapter = allchallengeAdapter
        binding!!.rvChallenges.addOnScrollListener(object : GridPaginationScrollListener(explorelayout) {
         /*   override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }
*/
            override fun loadMoreItems() {
             isLoadingcheck = true
                pageNo++
                //you have to call loadmore items to get more data
                getAllProfileChallenges(pageNo)

            }

            override val isLastPage: Boolean
                get() = isLastPagecheck
            override val isLoading: Boolean
                get() = isLoadingcheck
        })
        getAllProfileChallenges(pageNo)
    }

    // your all Challengers List API
    fun getAllProfileChallenges(page: Int) {
        try {
            // Loader?.showLoader(activity!!)
            val rootObject = JsonObject()
            try {
                rootObject.addProperty("userId", AppStaticData().getModel()?.result?.id)
                rootObject.addProperty("type", 3)
                rootObject.addProperty("pageNumber", page)
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
                        //   Loader?.hideLoader(activity!!)
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
                                } else {
                                    //Utils.showToast(activity!!, mChallengeList?.message!!, false)
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

    fun getMoreItems(list : java.util.ArrayList<ProfileChallengeResult>) {
        //after fetching your data assuming you have fetched list in your
        // recyclerview adapter assuming your recyclerview adapter is
        //rvAdapter
        //  after getting your data you have to assign false to isLoading
        isLoadingcheck = false

        if(pageNo == 1){
            allchallengeAdapter!!.setNotificationAdapter(activity,list)
        }else{
            allchallengeAdapter!!.addData(list)
        }



    }


}