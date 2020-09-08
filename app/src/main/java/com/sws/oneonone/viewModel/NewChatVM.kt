package com.sws.oneonone.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sws.oneonone.fragment.NewGroupFragment
import com.sws.oneonone.util.BaseActivity

class NewChatVM: ViewModel() {
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


    fun onCreateGroup() {
        activity?.onBackPressed()
        activity?.replaceFragment(NewGroupFragment())
    }


    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        Log.w("tag", "onTextChanged $s")
        if (!s.isEmpty()) {
            filterList(s?.toString())
        } else {
            filterList("")
        }
    }
    fun filterList(filterText: String?){

    }
}