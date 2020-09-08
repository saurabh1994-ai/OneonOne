package com.sws.oneonone.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sws.oneonone.firebase.FirebaseUserHandle
import com.sws.oneonone.fragment.ChangePasswordFragment
import com.sws.oneonone.model.SignUpModel
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.util.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MoreOptionVM: ViewModel() {
    val service: ApiClient? = ApiClient()

    private  var commanMLD: MutableLiveData<String>? = null
    var activity: BaseActivity? = null
    val comman: MutableLiveData<String>
        get() {
            if (commanMLD == null)
            {
                commanMLD = MutableLiveData()
            }
            return commanMLD as MutableLiveData<String>
        }

    fun onBackClick() {
        activity?.finish()
    }
    fun onChangePasswordClick() {
        activity?.replaceFragment(ChangePasswordFragment())
    }

    fun onUserLogoutClick()  {
        try {
            Loader?.showLoader(activity!!)
            val rootObject = JsonObject()
            try {
                rootObject.addProperty("userId",  AppStaticData()?.getModel()?.result?.id)

            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            //var token = pref?.getStringData("token")
            val call = apiService.userLogout(AppStaticData()?.APIToken(),rootObject)
            call.enqueue(object : Callback<SignUpModel> {
                override fun onResponse(call: Call<SignUpModel>, response: Response<SignUpModel>) {
                    if (response.code() == 200) {
                        try {

                            Loader?.hideLoader(activity!!)
                            val signUpModel = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity!!,signUpModel.code!!,signUpModel.message)
                            if(checkResponse){
                                if (signUpModel.code == 200) {
                                    AppStaticData().clearData()
                                    FirebaseUserHandle().updateUserActiveStatus(activity, signUpModel?.result?.id, false)
                                    comman.value = "back"

                                } else {
                                    Utils.showToast(activity!!, signUpModel?.message!!, false)
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
                    Loader?.hideLoader(activity!!)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

}