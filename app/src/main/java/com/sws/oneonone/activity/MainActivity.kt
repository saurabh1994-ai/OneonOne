package com.sws.oneonone.activity


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.transition.Transition
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.facebook.CallbackManager
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.databinding.ActivityMainBinding
import com.sws.oneonone.dialog.ProfileBottomDialog
import com.sws.oneonone.fragment.NotificationFragment
import com.sws.oneonone.fragment.SplashFragment
import com.sws.oneonone.fragment.WelcomeFragment
import com.sws.oneonone.model.SignUpModel
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.util.*
import com.sws.oneonone.util.AppStaticData.Companion.pref
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : BaseActivity(),NotificationCenter.NotificationCenterDelegate {
    var binding : ActivityMainBinding? = null
    var pref: PreferenceStore? = null
    val service: ApiClient? = ApiClient()
    var model: SignUpModel? = null
   // private var callbackManager: CallbackManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adjustFontScale(getResources().getConfiguration());
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;//  set status text dark
        window.statusBarColor = ContextCompat.getColor(this,R.color.white);// set status background white
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        pref = PreferenceStore.getInstance()
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.alertDialog)

       val gson = Gson()
       val json = pref?.getStringData("OneOnOne")
        model = gson.fromJson(json, SignUpModel::class.java)

       Connection.instance.connectSocket(model?.result?.id)
       Connection.instance.onReceived()
        FirebaseInstanceId.getInstance().instanceId
            .addOnSuccessListener { instanceIdResult ->
                val deviceToken = instanceIdResult.token
                // Do whatever you want with your token now
                // i.e. store it on SharedPreferences or DB
                // or directly send it to server
                AppStaticData.setFmcToken(deviceToken)
            }


        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(
                this
            ) { pendingDynamicLinkData ->


                // Get deep link from result (may be null if no link is found)
           val deepLink: Uri?
                var challengeLink = ""
                var uid = ""
                var profileId = ""
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link


                    Log.v("Deeplink", deepLink.toString())
                       try {
                           challengeLink = deepLink.toString()
                      Log.d("matchLink",challengeLink)
                           if(challengeLink.contains("uid")){
                               challengeLink = challengeLink.substring(challengeLink.lastIndexOf("=") +1)
                              uid = challengeLink.substring(0)
                           }else if(challengeLink.contains("profileId")){
                               challengeLink = challengeLink.substring(challengeLink.lastIndexOf("=") +1)
                               profileId = challengeLink.substring(0)
                           }

                           replaceFragment(SplashFragment.newInstance(uid,"",profileId), false, false, false)

                  }catch (ex:Exception){

                  }
              }else{
                    if (intent == null || intent!!.hasExtra("pushNotification")) {

                            replaceFragment(SplashFragment.newInstance("",intent.getStringExtra("pushNotification"),""), false, false, false)


                    }else{
                        replaceFragment(SplashFragment.newInstance("","",""), false, false, false)
                    }


                }
                }




        binding!!.loader.setOnClickListener(null)


    }


    override fun replaceFragment(fragment: BaseFragment?, isAdd: Boolean, addtobs: Boolean, forceWithoutAnimation: Boolean, transition: Transition?) {
        if (fragment == null)
            return

        try {
            val ft = supportFragmentManager.beginTransaction()
            val tag = fragment.javaClass.simpleName
            ft.setAllowOptimization(true)
            if (!forceWithoutAnimation) {
                if (AndroidUtilities().isAndroid5()) {
                    if (transition != null) {
                        fragment.enterTransition = transition
                    } else {
                        fragment.enterTransition = TransitionUtil().slide(Gravity.END)
                    }
                } else {
                    ft.setCustomAnimations(
                        R.anim.pull_in_right,
                        R.anim.push_out_left,
                        R.anim.pull_in_left,
                        R.anim.push_out_right
                    )
                }
            }
            if (isAdd)
                ft.add(R.id.frame, fragment, tag)
            else
                ft.replace(R.id.frame, fragment, tag)
            if (addtobs)
           // if (false)
                ft.addToBackStack(tag)
            ft.commitAllowingStateLoss()

        } catch (e: Exception) {

        }

    }


    fun whiteTitleBar(){
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.white));// set status background white

    }


   /* override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (callbackManager != null) {
            callbackManager!!.onActivityResult(requestCode, resultCode, data)
        }


    }
*/

    override fun getCurrentFragment(): BaseFragment? {
        return supportFragmentManager.findFragmentById(R.id.frame) as BaseFragment?


    }
  /*  fun getCallBackManager(): CallbackManager {
        callbackManager = CallbackManager.Factory.create()
        return callbackManager!!
    }*/

    fun showLoader(){
        binding!!.loader.visibility = View.VISIBLE
    }

    fun hideLoader(){
        binding!!.loader.visibility = View.GONE
    }


    override fun onBackPressed() {
        val backEntryCount = supportFragmentManager.backStackEntryCount
        if (backEntryCount > 0) {
            super.onBackPressed()
        } else
            finish()
    }

    override fun didReceivedNotification(id: Int, vararg args: Any?) {
        if(id == NotificationCenter.alertDialog){
            AppStaticData().clearData()
            clearAllStack()
            replaceFragment(WelcomeFragment())

        }
    }

    override fun onDestroy() {
        Connection.instance.ListenerOff()
        Connection.instance.StopSocket()
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.alertDialog)
        super.onDestroy()

    }


}
