package com.sws.oneonone.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sws.oneonone.firebase.FirebaseUserHandle
import com.sws.oneonone.firebaseModel.UserModel
import com.sws.oneonone.fragment.EditProfileFragment
import com.sws.oneonone.model.SignUpModel
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.retrofit.Validation
import com.sws.oneonone.util.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class EditProfileVM: ViewModel() {
    val service: ApiClient? = ApiClient()
    var pref: PreferenceStore? = null

    var fullName: MutableLiveData<String>? = MutableLiveData<String>()
    var email: MutableLiveData<String>? = MutableLiveData<String>()
    var userName: MutableLiveData<String>? = MutableLiveData<String>()
    var imageUrl: MutableLiveData<String>? = MutableLiveData<String>()
    var gender: MutableLiveData<String>? = MutableLiveData<String>()

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
        comman?.value = "back"
    }

    fun onOpenCameraClick() {
        comman?.value = "open_camera"
      //  OpenCameraOrGallery?.openPermission(activity!!)
    }

    fun onEditProfileClick() {
        activity?.replaceFragment(EditProfileFragment())
    }

    fun onUpdateClick() {
        if (!Validation().checkString(fullName?.value)) {
            Utils?.showToast(activity!!, "Please enter fullName!", false)
        } else if (!Validation().checkString(userName?.value)) {
            Utils?.showToast(activity!!, "Please enter username!", false)
        } else if  (!Validation().checkEmail(email?.value)){
            Utils?.showToast(activity!!, "Please enter valid email!", false)
        } else {
            if (Utils.isNetworkAvailable(activity)) {
                val genType = Validation().checkGender(gender?.value)
                if (!genType?.isNullOrEmpty()) {
                    updateData(genType!!)
                } else {
                    Utils?.showToast(activity!!, "Please enter valid gender!", false)
                }
            } else {
                SnackBar().internetSnackBar(activity)
            }
        }
    }

    fun updateData(GENDER: String) {
        try {
            var USER_NAME: String? = ""
            var USER_EMAIL: String? = ""
            var USER_IMAGE: String? = ""
            var USER_GENDER: String? = ""
            var USER_FULL_NAME: String? = ""
            val model: SignUpModel? = AppStaticData().getModel()
            if(!userName?.value.equals(model?.result?.username))
                USER_NAME = userName?.value
            if(!email?.value.equals(model?.result?.email))
                USER_EMAIL = email?.value
            if(!imageUrl?.value.equals(model?.result?.profileImg))
                USER_IMAGE = imageUrl?.value
            if(!GENDER.equals(model?.result?.gender))
                USER_GENDER = GENDER
            if(!fullName?.value.equals(model?.result?.fullName))
                USER_FULL_NAME = fullName?.value

            Loader?.showLoader(activity!!)
            val rootObject = JsonObject()
            try {
                rootObject.addProperty("userId",  model?.result?.id)
                rootObject.addProperty("username",  USER_NAME)
                rootObject.addProperty("email",  USER_EMAIL)
                rootObject.addProperty("profileImg",  USER_IMAGE)
                rootObject.addProperty("gender",  USER_GENDER)
                rootObject.addProperty("fullName",  USER_FULL_NAME)

            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            //var token = pref?.getStringData("token")
            val call = apiService.updateProfile(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<SignUpModel> {
                override fun onResponse(call: Call<SignUpModel>, response: Response<SignUpModel>) {

                    if (response.code() == 200) {
                        try {

                            Loader?.hideLoader(activity!!)
                            val signUpModel = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity!!,signUpModel.code!!,signUpModel.message)
                            if(checkResponse){
                                if (signUpModel.code == 200) {
                                    val gson = Gson()
                                    val json = gson.toJson(signUpModel)
                                    pref = PreferenceStore?.getInstance()
                                    pref?.saveStringData("OneOnOne", json)

                                    val userModel = UserModel()
                                    userModel.setFcmToken(AppStaticData.getFmcToken())// FMC token
                                    userModel.setIsTyping(false)//is Typing
                                    userModel.setLastSeen(Utils.currentTime())//last seen
                                    userModel.setNotificationStatus("1") //Notification status
                                    userModel.setOnChatScreen(false)// onChatScreen
                                    userModel.setStatus(false)//status
                                    userModel.setUserEmail(signUpModel.result?.email)// userEmail
                                    userModel.setUserImg(signUpModel.result?.profileImg) //user Image
                                    userModel.setUserName( signUpModel.result?.fullName) // userName
                                    FirebaseUserHandle().initializeFirebase(activity!!)
                                    FirebaseUserHandle().userAlreadyAxist(activity!!, signUpModel.result?.id, userModel)

                                    comman.value = "back"

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
                    Loader?.hideLoader(activity!!)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}