package com.sws.oneonone.util

import com.sws.oneonone.activity.MainActivity

class Loader{
    companion object{
        fun showLoader(activity: BaseActivity){
            (activity as MainActivity).showLoader()
        }
        fun hideLoader(activity: BaseActivity){
            (activity as MainActivity).hideLoader()
        }
    }
}