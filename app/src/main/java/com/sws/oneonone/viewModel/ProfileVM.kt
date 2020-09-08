package com.sws.oneonone.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sws.oneonone.fragment.EditProfileFragment
import com.sws.oneonone.fragment.MoreFragment
import com.sws.oneonone.util.BaseActivity

class ProfileVM: ViewModel() {
    var userName: MutableLiveData<String>? = MutableLiveData<String>()
    var fullName: MutableLiveData<String>? = MutableLiveData<String>()
    var points: MutableLiveData<String>? = MutableLiveData()
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
        comman.value = "back"
    }
    fun onMenuClick() {
        comman.value = "More"
    }

    fun onEditProfileClick() {
        comman.value = "Edit"
    }
}