package com.sws.oneonone.viewModel

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.fragment.CameraFragment
import com.sws.oneonone.fragment.ChangePasswordFragment
import com.sws.oneonone.model.SignUpModel
import com.sws.oneonone.retrofit.Validation
import com.sws.oneonone.util.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AcceptDeclineChallengeVM: ViewModel() {

    private  var commanMLD: MutableLiveData<String>? = null
    var hashtag: MutableLiveData<String>? = MutableLiveData<String>()
    var timer: MutableLiveData<String>? = MutableLiveData<String>()
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
        comman.value = "onBack"
    }

    fun onDeclineClick(){
        comman.value = "acceptDeclineDialog"
    }

    fun onAcceptClick() {
        comman.value = "acceptChallenge"


    }
}