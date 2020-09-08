package com.sws.oneonone.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sws.oneonone.fragment.NewGroupFragment
import com.sws.oneonone.model.ChildModel
import com.sws.oneonone.util.BaseActivity
import java.util.ArrayList

class NewGroupChatVM: ViewModel() {
    private  var commanMLD: MutableLiveData<ArrayList<ChildModel>>? = null
    var activity: BaseActivity? = null
    var selectedList: ArrayList<ChildModel> =  ArrayList<ChildModel>()

    val comman: MutableLiveData<ArrayList<ChildModel>>
        get() {
            if (commanMLD == null)
            {
                commanMLD = MutableLiveData()
            }
            return commanMLD as MutableLiveData<ArrayList<ChildModel>>
        }


    fun onCreateGroup() {
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

    fun selectedItemFunction(selectedItem: ChildModel, check: Boolean){
        if(check){
            selectedList?.add(selectedItem)
            comman?.value = selectedList

        } else {
            for(i in 0..(selectedList?.size-1)){
                if(selectedList?.get(i)?.getId().equals(selectedItem?.getId())){
                    selectedList?.removeAt(i)
                    comman?.value = selectedList
                    break
                }
            }
        }
    }

    fun removeFromSelectedList(selectedItem: ChildModel?) {
        for (i in 0..(selectedList?.size - 1)) {
            if (selectedList?.get(i)?.getId().equals(selectedItem?.getId())) {
                selectedList?.removeAt(i)
                comman?.value = selectedList
                break
            }
        }
    }
}