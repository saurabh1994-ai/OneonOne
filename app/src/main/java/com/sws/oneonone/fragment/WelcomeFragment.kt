package com.sws.oneonone.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.Nullable
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.snapchat.kit.sdk.SnapLogin
import com.snapchat.kit.sdk.core.controller.LoginStateController.OnLoginStateChangedListener
import com.snapchat.kit.sdk.login.models.UserDataResponse
import com.snapchat.kit.sdk.login.networking.FetchUserDataCallback
import com.sws.oneonone.R
import com.sws.oneonone.databinding.FragmentWelcomeBinding
import com.sws.oneonone.model.SignUpModel
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.util.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class WelcomeFragment : BaseFragment(){
    private val EMAIL = "email"
    private val USER_POSTS = "user_posts"
    private val AUTH_TYPE = "rerequest"
    private var binding: FragmentWelcomeBinding? = null
    private var mLoginStateChangedListener: OnLoginStateChangedListener? = null
    val service: ApiClient? = ApiClient()
    var pref: PreferenceStore? = null

    // facebook data
    var fbId: String? = ""
    private var mCallbackManager: CallbackManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //   mCallbackManager = CallbackManager.Factory.create()
    }

    /* override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mCallbackManager!!.onActivityResult(requestCode, resultCode, data)
    }*/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentWelcomeBinding.bind(inflater.inflate(R.layout.fragment_welcome, container, false))
        return binding!!.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val size = getScreenResolution(activity)
        Log.d("size++",size)

        binding!!.loginwithemail.setOnClickListener {
            activity.replaceFragment(LoginWithEmailFragment())
        }

        binding!!.loginwithSnapchat.setOnClickListener {
            val isUserLoggedIn = SnapLogin.isUserLoggedIn(activity)
            if (isUserLoggedIn) {
                getSnapUserData()
            } else {
                //getSnapUserData()
                SnapLogin.getAuthTokenManager(activity).startTokenGrant()
            }

        }

        binding!!.fbLogin.setOnClickListener {
            LoginManager.getInstance()
                .logInWithReadPermissions(this, Arrays.asList("public_profile"));
        }
        // Facebook login code
        val EMAIL = "email"
        mCallbackManager = CallbackManager.Factory.create()
        // val loginButton = findViewById(R.id.login_button) as LoginButton
        mCallbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(mCallbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    //loginResult?.accessToken?.userId
                    val request: GraphRequest = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        GraphRequest.GraphJSONObjectCallback() { jsonObject: JSONObject, graphResponse: GraphResponse ->
                            val id: String? = jsonObject.getString("id")
                            val name: String? = jsonObject.getString("name")
                            var email: String? = ""

                            if (jsonObject.length() >= 3) {
                                email = jsonObject.getString("email")
                            }


                            userSocialLogin(id, //Social Id
                                "F", // Social Type
                                email,   // user Email
                                name, // user name
                                "http://graph.facebook.com/"+id+"/picture?type=square"
                            )
                            /*Toast.makeText(
                                activity,
                                "fb ID : " + id + "\n" + "User Name : " + name + "\n" + "Email : " + email,
                                Toast.LENGTH_SHORT
                            ).show()*/

                        })
                    var parameters: Bundle = Bundle()
                    parameters.putString("fields", "id, name, email, gender, birthday");
                    request.setParameters(parameters);
                    request.executeAsync();
                    // App code
                }

                override fun onCancel() {
                    Toast.makeText(activity, "Cancel", Toast.LENGTH_SHORT).show()
                }

                override fun onError(exception: FacebookException) {
                    Toast.makeText(activity, exception?.message, Toast.LENGTH_SHORT).show()
                }
            })

        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mCallbackManager?.onActivityResult(requestCode, resultCode, data)
    }




    /* // the LoginStateListener tells us when a user has signed in/out
     mLoginStateChangedListener = object : OnLoginStateChangedListener {
         override fun onLoginSucceeded() {
             Log.d("SnapkitLogin", "Login was successful")


             getSnapUserData()
         }

         override fun onLoginFailed() {
             Log.d("SnapkitLogin", "Login was unsuccessful")
            // mDisplayName.setText(R.string.not_logged_in)
         }

         override fun onLogout() {
             // when the user unlinks their account we reset the fields and make the login button visible
             Log.d("SnapkitLogin", "User logged out")
           //  resetUserInfo()
         }
     }

     SnapLogin.getLoginStateController(activity)
         .addOnLoginStateChangedListener(mLoginStateChangedListener)

 }*/

    override fun onBackPressed() {
        super.onBackPressed()
        activity.finish()
    }

    fun getSnapUserData(){
        val query = "{me{bitmoji{avatar},displayName}}"
        val variables: String? = null
        SnapLogin.fetchUserData(activity, query, null, object : FetchUserDataCallback {
            override fun onSuccess(@Nullable userDataResponse: UserDataResponse?) {
                if (userDataResponse == null || userDataResponse.data == null) {
                    Utils.showToast(activity,"null",false)
                    return
                }
                val meData = userDataResponse.data.me ?: return

                /*  mNameTextView.setText(userDataResponse.data.me.displayName)
                  if (meData.bitmojiData != null) {
                      Glide.with(context!!)
                          .load(meData.bitmojiData.avatar)
                          .into(
                              mBitmojiImageView
                          )
                  }*/
                getSnapExternalId(userDataResponse.data.me.displayName,userDataResponse.data.me.bitmojiData.avatar)
            }

            override fun onFailure(isNetworkError: Boolean, statusCode: Int) {
                Utils.showToast(activity,"failes"+statusCode,false)
            }
        })
    }

    fun getSnapExternalId(displayName : String, avatar: String){
        val query = "{me{externalId}}"
        SnapLogin.fetchUserData(activity, query, null, object : FetchUserDataCallback {
            override fun onSuccess(userDataResponse: UserDataResponse?) {
                if (userDataResponse == null || userDataResponse.data == null) {
                    return
                }
                val meData = userDataResponse.data.me ?: return
                Utils.showToast(activity,"check"+meData.externalId,false)
                userSocialLogin(meData.externalId, // Social Id
                    "S",  // Social Type
                    "", //User Email
                    displayName, // user name
                    avatar)// user image
                //mBackend.save(userDataResponse.data.me.externalId)
            }

            override fun onFailure(isNetworkError: Boolean, statusCode: Int) {
                Utils.showToast(activity,"check"+statusCode,false)
            }
        })

    }

    fun userSocialLogin(externalId : String?,
                        socialType: String?,
                        email: String?,
                        displayName : String?,
                        avatar:String?)  {
        Loader?.showLoader(activity!!)
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
                rootObject.addProperty("email",  email)
                rootObject.addProperty("username",  "")
                rootObject.addProperty("fullName",displayName)
                rootObject.addProperty("profileImg",avatar)

            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.socialLogin(rootObject)
            call.enqueue(object : Callback<SignUpModel> {
                override fun onResponse(call: Call<SignUpModel>, response: Response<SignUpModel>) {
                    if (response.code() == 200) {
                        Loader?.hideLoader(activity!!)
                        try {
                            val signUpModel = response.body()!!
                            if (signUpModel.code == 201) {
                                activity.replaceFragment(SignupFragment.newInstance(true, displayName, email, externalId, avatar,socialType))

                            } else if(signUpModel.code == 200){

                                val gson = Gson()
                                val json = gson.toJson(signUpModel)
                                pref = PreferenceStore.getInstance()
                                pref?.saveStringData("OneOnOne", json)
                                activity.replaceFragment(HomeFragment())

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
                    Loader?.hideLoader(activity!!)
                    Utils.showToast(activity!!, "Have some problem...", false)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

  //  fun signInWithFb() {

        // Set the initial permissions to request from the user while logging in

        // Set the initial permissions to request from the user while logging in
        /*binding!!.loginwithFb.setPermissions(
            listOf(
               EMAIL,
                USER_POSTS
            )
        )*/

        //  binding!!.loginwithFb.authType = AUTH_TYPE

        // Register a callback to respond to the user

        // Register a callback to respond to the user
        /* binding!!.loginwithFb.registerCallback(
            mCallbackManager,
             object : FacebookCallback<LoginResult?> {
                 override fun onSuccess(loginResult: LoginResult?) {
                    Utils.showToast(activity,"Success",false)

                 }

                 override fun onCancel() {
                     Utils.showToast(activity,"fail",false)
                 }

                 override fun onError(e: FacebookException) {
                     // Handle exception
                     Utils.showToast(activity,"exception",false)
                 }
             })*/


        /* FacebookSdk.sdkInitialize(activity)
         val facebookUtils = FacebookUtil(activity, false)
         val facebookUtil = facebookUtils.getInstance(activity, false)
         facebookUtil.setListener(object : FacebookUtil.LoginFaceBookListener {
             override fun loginWithFaceBook() {
                 requestFacebookLogin()
             }

         })
         if (facebookUtil.getAccessToken() == null || facebookUtil.getAccessToken()?.isExpired!! ) {
             if (activity is MainActivity)
                 Log.d("facebookToken=====",facebookUtil.getAccessToken().toString())
             facebookUtil.onAuth((activity as MainActivity).getCallBackManager())
         } else{
             requestFacebookLogin()
             Log.d("facebookToken",facebookUtil.getAccessToken().toString())
         }*/

   // }

 /*   fun requestFacebookLogin() {

        if (TextUtils.isEmpty(pref!!.getFbEmail())) {

            Utils.showToast(activity, "Please Update Your email on facebook",false)
            return
        }
        val facebookId = pref!!.getFbUserId()
        val email = pref!!.getFbEmail()
        val username = pref!!.getFbName()
        val firstname = pref!!.getFbFirstName()
        val lastname = pref!!.getFbLastName()
        val fbImage = pref!!.getFbImageUrl()
        Utils.showToast(activity, "Done",false)
        //  androidUtilities.progreessLoading(activity)
        //socialLogin(facebookId!!, email!!, firstname!!, lastname!!, username!!, fbImage!!, "F")

    }*/


    private fun getScreenResolution(context: Context): String? {
        val wm: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.getDefaultDisplay()
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        val width: Int = metrics.widthPixels
        val height: Int = metrics.heightPixels
        return "{$width,$height}"
    }

}