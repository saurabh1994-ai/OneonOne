package com.sws.oneonone.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewExplorerVM : ViewModel(){
    var viewCount: MutableLiveData<String>? = MutableLiveData<String>()
    var timer: MutableLiveData<String>? = MutableLiveData<String>()
    var isOpenChallenge: MutableLiveData<Boolean>? = MutableLiveData<Boolean>()
    var btnText: MutableLiveData<String>? = MutableLiveData<String>()
    var likesCount: MutableLiveData<String>? = MutableLiveData<String>()
    var commentCount: MutableLiveData<String>? = MutableLiveData<String>()
    var votesCount: MutableLiveData<String>? = MutableLiveData<String>()
    var hashtag: MutableLiveData<String>? = MutableLiveData<String>()
    var comment:MutableLiveData<String>? = MutableLiveData<String>()
    private  var commentMutable: MutableLiveData<String>? = null


    val commentData: MutableLiveData<String>
        get() {
            if (commentMutable == null)
            {
                commentMutable = MutableLiveData()
            }
            return commentMutable as MutableLiveData<String>
        }

    fun onSendClick() {
        commentData.value = comment!!.value
    }
}