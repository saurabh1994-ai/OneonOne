package com.sws.oneonone.fragment

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.databinding.FragmentSignupBinding
import com.sws.oneonone.dialog.AlertDialog
import com.sws.oneonone.firebase.FirebaseUserHandle
import com.sws.oneonone.firebaseModel.UserModel
import com.sws.oneonone.model.SignUpModel
import com.sws.oneonone.model.SuccessModel
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.retrofit.Validation
import com.sws.oneonone.util.*
import com.sws.oneonone.viewModel.SignUpVM
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import java.util.*

class SignupFragment: BaseFragment(), View.OnFocusChangeListener{
    private var binding: FragmentSignupBinding? = null
    var signUpVM: SignUpVM? = null
    val service: ApiClient? = ApiClient()
    var shake: Animation? = null
    var isFromSocial = false
    var displayName: String? = ""
    var externalId: String? = ""
    var displayEmail: String?= ""
    var pref: PreferenceStore? = null
    var avatar: String? = ""
    var socialType : String? = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        signUpVM = ViewModelProviders.of(this).get(SignUpVM::class.java)
        binding = FragmentSignupBinding.bind(inflater.inflate(R.layout.fragment_signup, container, false))
        binding!!.lifecycleOwner = this

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ToolBar View Model
        signUpVM?.activity = activity
        binding!!.signUpVM = signUpVM

        binding!!.userName.onFocusChangeListener = this
        binding!!.email.onFocusChangeListener = this
        shake =
            AnimationUtils.loadAnimation(activity, R.anim.shake)

        if(isFromSocial){
            binding!!.rlPassword.visibility = View.GONE
            binding!!.rlConfirmPassword.visibility = View.GONE
            signUpVM?.fullName?.value = displayName
            signUpVM?.email?.value = displayEmail
            // binding!!.fullName.setText(displayName)
            binding!!.fullName.isFocusable = false
            binding!!.fullName.isFocusableInTouchMode = false // user touches widget on phone with touch screen
            binding!!.fullName.isClickable = false
        }else {
            binding!!.rlPassword.visibility = View.VISIBLE
            binding!!.rlConfirmPassword.visibility = View.VISIBLE
            binding!!.fullName.isFocusable = true
            binding!!.fullName.isFocusableInTouchMode = true
            binding!!.fullName.isClickable = true
        }

        binding!!.userName.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s!!.isEmpty()){
                    binding!!.ivCheck.visibility = View.GONE
                }
            }

        })

        binding!!.email.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s!!.isEmpty()){
                    binding!!.ivEmailCheck.visibility = View.GONE
                }
            }

        })



        initView()
    }

    private fun initView() {

        // check has Observers
        if (!signUpVM?.comman?.hasObservers()!!) {
            signUpVM?.comman?.observe(this, Observer { str ->
                when (str) {
                    "sign_in" -> activity.replaceFragment(LoginWithEmailFragment())
                    "next_ui" ->  {
                        //activity?.clearAllStack()
                        onBackPressed()
                        activity.replaceFragment(HomeFragment())
                    }

                    "signUp" ->{
                        if (!Validation().checkString(signUpVM!!.fullName?.value)) {
                            binding!!.fullName.startAnimation(shake)
                            binding!!.fullName.setHintTextColor(ContextCompat.getColor(activity,R.color.pink))
                            Utils.showToast(activity, "Please enter full name!", false)
                        } else if (!Validation().checkString(signUpVM!!.userName?.value)) {
                            binding!!.userName.startAnimation(shake)
                            binding!!.userName.setHintTextColor(ContextCompat.getColor(activity,R.color.pink))
                            Utils.showToast(activity, "Please enter username!", false)
                        } else if (!Validation().checkEmail(signUpVM!!.email?.value)){
                            binding!!.email.startAnimation(shake)
                            binding!!.email.text!!.clear()
                            binding!!.email.setHintTextColor(ContextCompat.getColor(activity,R.color.pink))
                            Utils.showToast(activity, "Please enter valid email!", false)
                        } else if(!isFromSocial){
                            if (!Validation().checkPassword(signUpVM!!.password?.value)){
                                binding!!.password.startAnimation(shake)
                                binding!!.password.setHintTextColor(ContextCompat.getColor(activity,R.color.pink))
                                Utils.showToast(activity, "Please enter minimum 6 character password!", false)
                            }else if (signUpVM!!.password?.value.equals(signUpVM!!.confirmPassword?.value)){
                                if (Utils.isNetworkAvailable(activity)) {
                                    signUpVM!!.userSignUp()
                                } else {
                                    SnackBar().internetSnackBar(activity)
                                }
                            }else{
                                binding!!.confirmPassword.startAnimation(shake)
                                binding!!.confirmPassword.text!!.clear()
                                binding!!.confirmPassword.setHintTextColor(ContextCompat.getColor(activity,R.color.pink))
                                Utils.showToast(activity, "Confirm password not matched!", false)
                            }

                        }  else {
                            if(isFromSocial){
                                if (Utils.isNetworkAvailable(activity)) {
                                    userSocialLogin ()
                                } else {
                                    SnackBar().internetSnackBar(activity)
                                }
                            }else{
                                binding!!.confirmPassword.startAnimation(shake)
                                binding!!.confirmPassword.text!!.clear()
                                binding!!.confirmPassword.setHintTextColor(ContextCompat.getColor(activity,R.color.pink))
                                Utils.showToast(activity, "Confirm password not matched!", false)
                            }

                        }
                    }
                }
            })
        }
    }

    fun checkIfUserExist(checktxt:String , isFromEmail:Boolean)  {
        try {
            val rootObject = JsonObject()
            try {
                if(isFromEmail){
                    rootObject.addProperty("email",checktxt)
                    rootObject.addProperty("username", binding!!.email.text.toString().trim())
                }else{
                    rootObject.addProperty("email","")
                    rootObject.addProperty("username", checktxt)
                }


            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.checkIfUserExist(rootObject)
            call.enqueue(object : Callback<SuccessModel> {
                override fun onResponse(call: Call<SuccessModel>, response: Response<SuccessModel>) {
                    if (response.code() == 200) {
                        try {
                            val signUpModel = response.body()!!
                            val checkResponse = AndroidUtilities().apisResponse(
                                activity,
                                signUpModel.code,
                                signUpModel.message
                            )
                            if (checkResponse) {
                                if (signUpModel.code == 200) {
                                    if (isFromEmail) {
                                        binding!!.ivEmailCheck.visibility = View.VISIBLE
                                        (binding!!.ivEmailCheck.drawable as Animatable).start()
                                    } else {
                                        binding!!.ivCheck.visibility = View.VISIBLE
                                        (binding!!.ivCheck.drawable as Animatable).start()
                                    }


                                } else {
                                    if (isFromEmail) {
                                        binding!!.email.startAnimation(shake)
                                        binding!!.email.setHintTextColor(
                                            ContextCompat.getColor(
                                                activity,
                                                R.color.pink
                                            )
                                        )
                                        binding!!.email.text!!.clear()
                                        Utils.showToast(activity, signUpModel.message, false)
                                    } else {
                                        binding!!.userName.startAnimation(shake)
                                        binding!!.userName.setHintTextColor(
                                            ContextCompat.getColor(
                                                activity,
                                                R.color.pink
                                            )
                                        )
                                        binding!!.userName.text!!.clear()
                                        Utils.showToast(activity, signUpModel.message, false)
                                    }

                                }
                            }
                        }catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
                override fun onFailure(call: Call<SuccessModel>, t: Throwable) {
                    Log.e("responseError", t.message)
                    Utils.showToast(activity, "Have some problem...", false)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        when(v!!.id){
            R.id.userName -> {
                if (!hasFocus) {
                    // code to execute when EditText loses focus
                    val afterTextChanged = binding!!.userName.text.toString()
                    checkIfUserExist(afterTextChanged,false)

                }

            }

            R.id.email ->{
                if (!hasFocus) {
                    if (!Validation().checkEmail(signUpVM!!.email?.value)){
                        binding!!.email.startAnimation(shake)
                        binding!!.email.text!!.clear()
                        binding!!.email.setHintTextColor(ContextCompat.getColor(activity,R.color.pink))
                        Utils.showToast(activity, "Please enter valid email!", false)
                    }else{
                        val afterTextChanged = binding!!.email.text.toString()
                        checkIfUserExist(afterTextChanged, true)
                    }

                }
            }
        }

    }

    companion object {

        fun newInstance(isFromSocial :Boolean, displayName : String?, email: String?, externalId: String?, avatar:String?, socialType : String?) = SignupFragment().apply {
            this.isFromSocial = isFromSocial
            this.displayName = displayName
            this.displayEmail = email
            this.externalId = externalId
            this.avatar = avatar
            this.socialType = socialType


        }
    }

    fun userSocialLogin()  {
        try {
            val deviceId = UUID.randomUUID().toString()
            val rootObject = JsonObject()
            val devices = JsonArray()
            val deviceObject = JsonObject()
            try {
                deviceObject.addProperty("deviceId",deviceId)
                deviceObject.addProperty("deviceType","A")
                deviceObject.addProperty("deviceToken", AppStaticData.getFmcToken())
                devices.add(deviceObject)
                rootObject.add("devices",devices)
                rootObject.addProperty("socialId", externalId )
                rootObject.addProperty("socialType",  socialType)
                rootObject.addProperty("email", signUpVM?.email?.value )
                rootObject.addProperty("username",  signUpVM?.userName?.value )
                rootObject.addProperty("fullName",signUpVM?.fullName?.value)
                rootObject.addProperty("profileImg",avatar)


            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.socialLogin(rootObject)
            call.enqueue(object : Callback<SignUpModel> {
                override fun onResponse(call: Call<SignUpModel>, response: Response<SignUpModel>) {
                    if (response.code() == 200) {
                        try {
                            val signUpModel = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity,signUpModel.code!!,signUpModel.message)
                            if(checkResponse) {
                                if (signUpModel.code == 200) {
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
                                    FirebaseUserHandle().initializeFirebase(activity!!)
                                    FirebaseUserHandle().createUser(activity!!, signUpModel.result?.id, userModel)

                                    activity.replaceFragment(HomeFragment())


                                }
                            }else {
                                Utils.showToast(activity, signUpModel.message!!, false)
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