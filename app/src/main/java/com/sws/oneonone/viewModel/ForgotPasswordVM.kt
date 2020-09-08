package com.sws.oneonone.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.sws.oneonone.model.SignUpModel
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.retrofit.Validation
import com.sws.oneonone.util.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ForgotPasswordVM: ViewModel() {
    val service: ApiClient? = ApiClient()
    var pref: PreferenceStore? = null

    var email: MutableLiveData<String>? = MutableLiveData<String>()
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
        activity?.onBackPressed()
    }


    fun onClickSend() {
        if (!Validation().checkEmail(email?.value)){
            Utils.showToast(activity!!, "Please enter valid email!", false)
        } else {
            if (Utils.isNetworkAvailable(activity)) {
                userForgotPasswordAPI()
            } else {
                SnackBar().internetSnackBar(activity)
            }
        }
    }
    fun userForgotPasswordAPI() {
        try {
            val rootObject = JsonObject()
            try {
                rootObject.addProperty("email", email?.value)
            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.forgotPassword(rootObject)
            call.enqueue(object : Callback<SignUpModel> {
                override fun onResponse(call: Call<SignUpModel>, response: Response<SignUpModel>) {
                    if (response.code() == 200) {
                        try {

                            val signUpModel = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity!!,signUpModel.code!!,signUpModel.message)
                            if(checkResponse) {
                                if (signUpModel.code == 200) {
                                   onBackClick()
                                    Utils.showToast(activity!!, signUpModel.message!!, false)
                                }
                            }else {
                                Utils.showToast(activity!!, signUpModel.message!!, false)
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