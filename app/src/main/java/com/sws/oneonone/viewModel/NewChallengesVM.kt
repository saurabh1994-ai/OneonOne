package com.sws.oneonone.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.sws.oneonone.fragment.SelectChallengersFragment
import com.sws.oneonone.model.MyFollowingModel
import com.sws.oneonone.model.MyFollowingResult
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.util.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class NewChallengesVM: ViewModel() {
    var activity: BaseActivity? = null
    var service: ApiClient? = ApiClient()

    var openChallenge: MutableLiveData<Boolean>? = MutableLiveData<Boolean>()
    var videoPath: MutableLiveData<String>? = MutableLiveData<String>()
    private var commanMLD: MutableLiveData<ArrayList<MyFollowingResult>>? = null
    private var followingList: ArrayList<MyFollowingResult> = ArrayList<MyFollowingResult>()

    val comman: MutableLiveData<ArrayList<MyFollowingResult>>
        get() {
            if (commanMLD == null)
            {
                commanMLD = MutableLiveData()
            }
            return commanMLD as MutableLiveData<ArrayList<MyFollowingResult>>
        }

    fun challengersClick(){
        activity?.replaceFragment(SelectChallengersFragment())
    }

    fun onChallengeClick() {
           if (Utils.isNetworkAvailable(activity)) {
               setMyFollowings()
            } else {
                SnackBar().internetSnackBar(activity)
            }
    }

    // your Challengers List API
    fun setMyFollowings() {
        try {
            Loader.showLoader(activity!!)
            val rootObject = JsonObject()
            try {
                rootObject.addProperty("userId",  AppStaticData()?.getModel()?.result?.id)
            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.getAllUserList(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<MyFollowingModel> {
                override fun onResponse(call: Call<MyFollowingModel>, response: Response<MyFollowingModel>) {
                    if (response.code() == 200) {
                        Loader.hideLoader(activity!!)
                        try {
                            val mMyFollowingModel = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity!!,mMyFollowingModel.code!!,mMyFollowingModel.message)
                            if(checkResponse){
                            if (mMyFollowingModel.code == 200) {
                                if (!mMyFollowingModel?.result.isNullOrEmpty()) {
                                    for (i in 0..(mMyFollowingModel?.result?.size!! - 1)) {
                                        val list = MyFollowingResult()
                                        list?.setMyFollowingResult(mMyFollowingModel?.result!![i])
                                        followingList?.add(list)
                                    }
                                    comman?.value = followingList
                                }
                            }
                            } else {
                                Utils.showToast(activity!!, mMyFollowingModel?.message!!, false)
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
                override fun onFailure(call: Call<MyFollowingModel>, t: Throwable) {
                    Loader?.hideLoader(activity!!)
                    Log.e("responseError", t.message)
                    Utils.showToast(activity!!, "Have some problem...", false)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}