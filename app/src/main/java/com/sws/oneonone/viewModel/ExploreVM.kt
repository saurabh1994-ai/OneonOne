package com.sws.oneonone.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.sws.oneonone.dialog.PreviewBottomDialog
import com.sws.oneonone.dialog.ProfileBottomDialog
import com.sws.oneonone.fragment.AddChallengersFragment
import com.sws.oneonone.fragment.ExploreFragment
import com.sws.oneonone.fragment.NotificationFragment
import com.sws.oneonone.fragment.ProfileFragment
import com.sws.oneonone.model.ExploreModel
import com.sws.oneonone.model.ExploreResult
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.util.*

import java.util.ArrayList


class ExploreVM: ViewModel() {
    var activity: BaseActivity? = null
    var service: ApiClient? = ApiClient()
    var IsOpenChallenge: MutableLiveData<Boolean>? = MutableLiveData<Boolean>()
    private var commanMLD: MutableLiveData<ArrayList<ExploreResult>>? = null
    private var commanMLD2: MutableLiveData<ArrayList<ExploreResult>>? = null

    private var challengerList: ArrayList<ExploreResult> = ArrayList<ExploreResult>()

    val comman: MutableLiveData<ArrayList<ExploreResult>>
        get() {
            if (commanMLD == null)
            {
                commanMLD = MutableLiveData()
            }
            return commanMLD as MutableLiveData<ArrayList<ExploreResult>>
        }

    val challengerMLD: MutableLiveData<ArrayList<ExploreResult>>
        get() {
            if (commanMLD2 == null)
            {
                commanMLD2 = MutableLiveData()
            }
            return commanMLD2 as MutableLiveData<ArrayList<ExploreResult>>
        }

    fun onAddChallengersClick() {
        activity?.replaceFragment(AddChallengersFragment())
    }

    fun onAllChallengesClick(){
        IsOpenChallenge?.value = false
    }
    fun onOpenChallengesClick(){
        IsOpenChallenge?.value = true
    }
    fun onProfileClick() {
        val  videoBottomSheetDialog = ProfileBottomDialog.newInstance(activity!!,"",null,null)
        videoBottomSheetDialog.show(activity!!.supportFragmentManager, ProfileBottomDialog().TAG)

        //  activity?.replaceFragment(ProfileFragment())
    }

    fun onNotificationClick() {
        activity?.replaceFragment(NotificationFragment())
    }







}