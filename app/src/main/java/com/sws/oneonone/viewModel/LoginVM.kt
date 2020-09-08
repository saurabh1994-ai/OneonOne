package com.sws.oneonone.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.sws.oneonone.firebase.FirebaseUserHandle
import com.sws.oneonone.firebaseModel.UserModel
import com.sws.oneonone.model.SignUpModel
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.retrofit.Validation
import com.sws.oneonone.util.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class LoginVM: ViewModel() {

    val service: ApiClient? = ApiClient()
    var pref: PreferenceStore? = null
    var userName: MutableLiveData<String>? = MutableLiveData<String>()
    var password: MutableLiveData<String>? = MutableLiveData<String>()
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

    fun onClickSingIn() {
        comman.value = "sign_up"
    }
    fun onBackClick() {
        comman.value = "back"
    }
    fun onClickForgotPassword() {
        comman.value = "forgot_password"
    }


    fun onClickLogin() {
        if (!Validation().checkString(userName?.value)) {
            Utils.showToast(activity!!, "Please enter username!", false)
        } else if (!Validation().checkPassword(password?.value)){
            Utils.showToast(activity!!, "Please enter password!", false)
        } else {
            if (Utils.isNetworkAvailable(activity)) {
                userLogin()
            } else {
                SnackBar().internetSnackBar(activity)
            }
        }
    }

    fun userLogin()  {
        try {
            val deviceId = UUID.randomUUID().toString()
            val rootObject = JsonObject()
            val devices = JsonArray()
            val deviceObject = JsonObject()

            try {
                deviceObject.addProperty("deviceId",deviceId)
                deviceObject.addProperty("deviceType","A")
                deviceObject.addProperty("deviceToken",AppStaticData.getFmcToken())
                devices.add(deviceObject)
                rootObject.add("devices",devices)
                rootObject.addProperty("username",  userName?.value)
                rootObject.addProperty("password",  password?.value)
                Log.d("tagg", rootObject.toString())
            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.userLogin(rootObject)
            call.enqueue(object : Callback<SignUpModel> {
                override fun onResponse(call: Call<SignUpModel>, response: Response<SignUpModel>) {
                    if (response.code() == 200) {
                        try {
                            val signUpModel = response.body()!!
                            if (signUpModel.code == 200) {

                              //  FirebaseUserHandle().updateUserActiveStatus(activity, signUpModel.result?.id, true)
                                val gson = Gson()
                                val json = gson.toJson(signUpModel)
                                pref = PreferenceStore.getInstance()
                                pref?.saveStringData("OneOnOne", json)

                                val userModel = UserModel()
                                userModel.setFcmToken(AppStaticData.getFmcToken())// FMC token
                                userModel.setIsTyping(false)//is Typing
                                userModel.setLastSeen(Utils.currentTime())//last seen
                                userModel.setNotificationStatus("1") //Notification status
                                userModel.setOnChatScreen(false)// onChatScreen
                                userModel.setStatus(true)//status
                                userModel.setUserEmail(signUpModel.result?.email)// userEmail
                                userModel.setUserImg(signUpModel.result?.profileImg) //user Image
                                userModel.setUserName( signUpModel.result?.fullName) // userName
                                FirebaseUserHandle().initializeFirebase(activity)
                                FirebaseUserHandle().userAlreadyAxist(activity!!, signUpModel.result?.id, userModel)
                                comman.value = "next_ui"

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
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}