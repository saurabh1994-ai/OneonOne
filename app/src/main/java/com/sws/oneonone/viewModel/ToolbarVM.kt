package com.sws.oneonone.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sws.oneonone.util.BaseActivity

class ToolbarVM: ViewModel() {
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
        comman.value = "onBack"
    }

    fun onClickText() {
        comman.value = "next"
    }

}