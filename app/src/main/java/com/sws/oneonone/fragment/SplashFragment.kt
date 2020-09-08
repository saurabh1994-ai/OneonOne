package com.sws.oneonone.fragment

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.sws.oneonone.R
import com.sws.oneonone.databinding.FragmentSplashBinding
import com.sws.oneonone.dialog.ProfileBottomDialog
import com.sws.oneonone.firebase.FirebaseUserHandle
import com.sws.oneonone.firebaseModel.UserModel
import com.sws.oneonone.model.SignUpModel
import com.sws.oneonone.util.AppStaticData
import com.sws.oneonone.util.BaseFragment
import com.sws.oneonone.util.PreferenceStore
import com.sws.oneonone.util.Utils
import org.json.JSONObject


class SplashFragment: BaseFragment(){
    private var binding: FragmentSplashBinding? = null
    var pref: PreferenceStore? = null
    var uid = ""
    var notiData = ""
    var profileId = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSplashBinding.bind(inflater.inflate(R.layout.fragment_splash, container, false))

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pref = PreferenceStore.getInstance()

        Handler().postDelayed({

            val  json: String? = pref?.getStringData("OneOnOne")

            activity.clearAllStack()
            if (json.isNullOrEmpty())
            {
                activity.replaceFragment(WelcomeFragment())
            } else {
                val gson = Gson()
                val model = gson.fromJson(json, SignUpModel::class.java)
                val userModel = UserModel()
                userModel.setFcmToken(AppStaticData.getFmcToken())// FMC token
                userModel.setIsTyping(false)//is Typing
                userModel.setLastSeen(Utils.currentTime())//last seen
                userModel.setNotificationStatus("1") //Notification status
                userModel.setOnChatScreen(false)// onChatScreen
                userModel.setStatus(true)//status
                userModel.setUserEmail(model?.result?.email)// userEmail
                userModel.setUserImg(model?.result?.profileImg) //user Image
                userModel.setUserName(model?.result?.fullName) // userName
                FirebaseUserHandle().initializeFirebase(activity)
               // FirebaseUserHandle().updateUser(activity, model?.result?.id, userModel)
                FirebaseUserHandle().userAlreadyAxist(activity, model?.result?.id, userModel)

                Log.d("profileId++", profileId)
                activity.replaceFragment(HomeFragment.newInstance(uid,notiData,profileId,false,false),false,false)



            }

        }, 3000)




    }


    companion object {

        fun newInstance(uid: String?,notiData: String?,profileId: String?) = SplashFragment().apply {
            this.uid = uid!!
            this.notiData = notiData!!
            this.profileId = profileId!!

        }
    }


}