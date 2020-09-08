package com.sws.oneonone.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.sws.oneonone.fragment.ChangePasswordFragment
import com.sws.oneonone.model.SignUpModel
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.retrofit.Validation
import com.sws.oneonone.util.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePassVM: ViewModel() {
    val service: ApiClient? = ApiClient()

    var oldPassword: MutableLiveData<String>? = MutableLiveData<String>()
    var newPassword: MutableLiveData<String>? = MutableLiveData<String>()
    var confirmPassword: MutableLiveData<String>? = MutableLiveData<String>()
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
        if (!Validation().checkString(oldPassword?.value)) {
            Utils.showToast(activity!!, "Please enter old password!", false)
        } else if (!Validation().checkPassword(newPassword?.value)) {
            Utils.showToast(activity!!, "Please enter minimum 6 character new password!", false)
        } else if (newPassword?.value.equals(confirmPassword?.value)) {
            if (Utils.isNetworkAvailable(activity)) {
                changePasswordAPI()
            } else {
                SnackBar().internetSnackBar(activity)
            }
        } else {
            Utils.showToast(activity!!, "Confirm password not matched!", false)
        }
    }

    fun changePasswordAPI() {
        try {
            val rootObject = JsonObject()
            try {
                rootObject.addProperty("userId",  AppStaticData().getModel()?.result?.id)
                rootObject.addProperty("oldPassword",  oldPassword?.value)
                rootObject.addProperty("newPassword", confirmPassword?.value)
            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            //var token = pref?.getStringData("token")
            val call = apiService.changePassword(AppStaticData().APIToken(),rootObject)
            call.enqueue(object : Callback<SignUpModel> {
                override fun onResponse(call: Call<SignUpModel>, response: Response<SignUpModel>) {
                    if (response.code() == 200) {
                        try {
                            val signUpModel = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity!!,signUpModel.code!!,signUpModel.message)
                            if(checkResponse){
                                if (signUpModel.code == 200) {
                                  //  AndroidUtilities().openAlertDialog(signUpModel.message!!,activity!!)
                                    comman.value = "onBack"

                                }else{
                                    AndroidUtilities().openAlertDialog(signUpModel.message!!,activity!!)
                                }
                            } else {
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
                    Loader.hideLoader(activity!!)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}