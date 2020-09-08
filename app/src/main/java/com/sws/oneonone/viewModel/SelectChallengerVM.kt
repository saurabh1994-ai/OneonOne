package com.sws.oneonone.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.sws.oneonone.model.MyFollowingModel
import com.sws.oneonone.model.MyFollowingResult
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.util.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class SelectChallengerVM: ViewModel() {
    var activity: BaseActivity? = null
    var service: ApiClient? = ApiClient()
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

    fun onChallengeClick() {
           if (Utils.isNetworkAvailable(activity)) {
               setMyFollowings(1, "")
            } else {
                SnackBar().internetSnackBar(activity)
            }
    }



    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.isNotEmpty()) {
            setMyFollowings(1, s.toString())
        } else {
            setMyFollowings(1, "")
        }
    }
    // your Challengers List API
    fun setMyFollowings(page: Int, searchText: String?) {
        try {
            val rootObject = JsonObject()
            try {
                rootObject.addProperty("userId",  AppStaticData().getModel()?.result?.id)
                rootObject.addProperty("pageNumber",  page)
                rootObject.addProperty("username",searchText)
            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.getChallengersList(AppStaticData().APIToken(), rootObject)
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
                                    for (i in 0 until mMyFollowingModel.followers.size) {
                                        val list = MyFollowingResult()


                                        list.setMyFollowingResult(mMyFollowingModel.followers[i])
                                        followingList.add(list)
                                    }
                                    comman.value = followingList
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
}