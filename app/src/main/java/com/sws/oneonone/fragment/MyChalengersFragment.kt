package com.sws.oneonone.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.adapter.*
import com.sws.oneonone.databinding.FragmentMyChallengersBinding
import com.sws.oneonone.model.ProfileChallengeModel
import com.sws.oneonone.model.ProfileChallengeResult
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.util.AndroidUtilities
import com.sws.oneonone.util.AppStaticData
import com.sws.oneonone.util.BaseFragment
import com.sws.oneonone.util.Utils
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MyChalengersFragment: BaseFragment() {
    private var binding: FragmentMyChallengersBinding? = null
    var service: ApiClient? = ApiClient()
    var adapter: MyChallengersAdapter? = null
    var challengeList: ArrayList<ProfileChallengeResult> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentMyChallengersBinding.bind(inflater.inflate(R.layout.fragment_my_challengers, container, false))
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val explorelayout = GridLayoutManager(activity, 2)
        explorelayout.setAutoMeasureEnabled(true)
        binding!!.rvChallenges.layoutManager = explorelayout
        binding!!.rvChallenges.isNestedScrollingEnabled = false
        binding!!.rvChallenges.setHasFixedSize(false);

        getAllProfileChallenges(1)
    }

    // your Challengers List API
    fun getAllProfileChallenges(page: Int) {
        try {
            // Loader?.showLoader(activity!!)
            val rootObject = JsonObject()
            try {
                rootObject.addProperty("userId", AppStaticData().getModel()?.result?.id)
                rootObject.addProperty("type", 2)
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
                                        challengeList?.clear()
                                    }
                                    if (!mChallengeList?.result.isNullOrEmpty()) {
                                        for (i in 0..(mChallengeList?.result?.size!! - 1)) {
                                            val list = ProfileChallengeResult()
                                            list?.setProfileChallenge(mChallengeList?.result!![i])
                                            challengeList?.add(list)
                                        }
                                        adapter = MyChallengersAdapter(activity, challengeList)
                                        binding!!.rvChallenges.adapter = adapter
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
}