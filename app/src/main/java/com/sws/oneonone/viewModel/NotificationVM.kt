package com.sws.oneonone.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.sws.oneonone.fragment.NotificationFragment
import com.sws.oneonone.model.ExploreResult
import com.sws.oneonone.model.NotificationModel
import com.sws.oneonone.model.NotificationResult
import com.sws.oneonone.util.*

import java.util.ArrayList

class NotificationVM: ViewModel() {
    var activity: BaseActivity? = null

    private var commanMLD: MutableLiveData<ArrayList<NotificationResult>>? = null


    val comman: MutableLiveData<ArrayList<NotificationResult>>
        get() {
            if (commanMLD == null)
            {
                commanMLD = MutableLiveData()
            }
            return commanMLD as MutableLiveData<ArrayList<NotificationResult>>
        }

    fun onNotificationClick() {
        activity?.replaceFragment(NotificationFragment())
    }
/*
    fun onExploreListClick(pageNo: Int) {
           if (Utils.isNetworkAvailable(activity)) {
               YourNotificationData(pageNo)
            } else {
                SnackBar().internetSnackBar(activity)
            }
    }*/
    // your Challengers List API

}