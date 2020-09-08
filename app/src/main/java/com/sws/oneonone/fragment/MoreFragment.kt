package com.sws.oneonone.fragment

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.R.layout.fragment_more_option
import com.sws.oneonone.databinding.FragmentMoreOptionBinding
import com.sws.oneonone.model.SignUpModel
import com.sws.oneonone.model.ToolbarModel
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.util.*
import com.sws.oneonone.viewModel.MoreOptionVM
import com.sws.oneonone.viewModel.ToolbarVM
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MoreFragment: BaseFragment() {

    private var binding: FragmentMoreOptionBinding? = null
    var toolbarVM: ToolbarVM? = null
   private  var moreVM: MoreOptionVM? = null
    val service: ApiClient? = ApiClient()
    var pref: PreferenceStore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarVM = ViewModelProviders.of(this).get(ToolbarVM::class.java)
        moreVM = ViewModelProviders.of(this).get(MoreOptionVM::class.java)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;//  set status text dark
        activity.window.statusBarColor = ContextCompat.getColor(activity,R.color.white)// set status background white
        binding = FragmentMoreOptionBinding.bind(inflater.inflate(fragment_more_option, container, false))
        binding!!.lifecycleOwner = this
        return binding!!.root
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Tool bar model
        val toolbarModel = ToolbarModel(View.VISIBLE,"More Option", View.GONE,View.VISIBLE,View.VISIBLE,View.GONE, 1, "","",View.GONE)
        binding!!.layoutToolbar.toolbarModel = toolbarModel

        binding!!.layoutToolbar.lifecycleOwner = this
        // ToolBar View Model
        toolbarVM?.activity = activity
        binding!!.layoutToolbar.toolbarVM = toolbarVM

        // More Option View Model
        moreVM?.activity = activity
        binding!!.moreVM = moreVM


        binding!!.rlShareProfille.setOnClickListener {
            val model: SignUpModel? = AppStaticData().getModel()
            AndroidUtilities().createDynamicLink(activity, "Let's checkout this awsome profile","",model?.result?.id,"")
        }

        initView()
    }


    private fun initView() {
        // check has Observers
        if (!toolbarVM?.comman?.hasObservers()!!) {
            toolbarVM?.comman?.observe(this, Observer { str ->
                when (str) {
                    "onBack" -> onBackPressed()
                }
            })
        } // check has Observers
        if (!moreVM?.comman?.hasObservers()!!) {
            moreVM?.comman?.observe(this, Observer { str ->
                when (str) {
                    "back" -> {
                        activity.clearAllStack()
                        activity.replaceFragment(WelcomeFragment())
                    }
                }
            })
        }

        val model: SignUpModel? = AppStaticData().getModel()
        binding!!.privateAccountOn.isChecked = model?.result?.isPrivate!!
        binding!!.notificationSwitch.isChecked = model.result?.notificationStatus == "1"


        binding!!.privateAccountOn.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                updateAccountPrivacy("1")
            }else{
                updateAccountPrivacy("0")
            }
        }

        binding!!.notificationSwitch.setOnCheckedChangeListener{ buttonView, isChecked ->
            if(isChecked){
                updatePushNotification("1")
            }else{
                updatePushNotification("0")
            }
        }

        binding!!.rlContactus.setOnClickListener {
            val recepientEmail = "info@tuurnt.com" // either set to destination email or leave empty

            val intent =
                Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:$recepientEmail")
           activity.startActivity(intent)
        }
    }


    fun updateAccountPrivacy(type : String)  {
        try {

            val rootObject = JsonObject()

            try {

                rootObject.addProperty("userId",AppStaticData().getModel()?.result?.id)
                rootObject.addProperty("type", type )



            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.updateAccountPrivacy(AppStaticData().APIToken(),rootObject)
            call.enqueue(object : Callback<SignUpModel> {
                override fun onResponse(call: Call<SignUpModel>, response: Response<SignUpModel>) {
                    if (response.code() == 200) {
                        try {
                            val signUpModel = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity,signUpModel.code!!,signUpModel.message)
                            if(checkResponse) {
                                if (signUpModel.code == 200) {
                                    binding!!.privateAccountOn.isChecked = type != "0"
                                    if(type == "0"){
                                        val model: SignUpModel? = AppStaticData().getModel()
                                       model?.result?.isPrivate = false
                                        val gson = Gson()
                                        val json = gson.toJson(model)
                                        pref = PreferenceStore.getInstance()
                                        pref?.saveStringData("OneOnOne", json)
                                    }else if(type == "1"){
                                        val model: SignUpModel? = AppStaticData().getModel()
                                        model?.result?.isPrivate = true
                                        val gson = Gson()
                                        val json = gson.toJson(model)
                                        pref = PreferenceStore.getInstance()
                                        pref?.saveStringData("OneOnOne", json)
                                    }



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

    fun updatePushNotification(type : String)  {
        try {

            val rootObject = JsonObject()

            try {

                rootObject.addProperty("userId",AppStaticData().getModel()?.result?.id)
                rootObject.addProperty("type", type )



            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.onOffPushNoti(AppStaticData().APIToken(),rootObject)
            call.enqueue(object : Callback<SignUpModel> {
                override fun onResponse(call: Call<SignUpModel>, response: Response<SignUpModel>) {
                    if (response.code() == 200) {
                        try {
                            val signUpModel = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity,signUpModel.code!!,signUpModel.message)
                            if(checkResponse) {
                                if (signUpModel.code == 200) {
                                    binding!!.notificationSwitch.isChecked = type != "0"
                                    if(type == "0"){
                                        val model: SignUpModel? = AppStaticData().getModel()
                                        model?.result?.notificationStatus = "0"
                                        val gson = Gson()
                                        val json = gson.toJson(model)
                                        pref = PreferenceStore.getInstance()
                                        pref?.saveStringData("OneOnOne", json)
                                    }else if(type == "1"){
                                        val model: SignUpModel? = AppStaticData().getModel()
                                        model?.result?.notificationStatus = "1"
                                        val gson = Gson()
                                        val json = gson.toJson(model)
                                        pref = PreferenceStore.getInstance()
                                        pref?.saveStringData("OneOnOne", json)
                                    }



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