package com.sws.oneonone.util

import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.sws.oneonone.R

class NotificationBarColor {


    fun fullScreen(activity: BaseActivity){
        activity?.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        activity?.getWindow().setStatusBarColor(ContextCompat.getColor(activity,R.color.black));// set status background white
    }
    companion object {
        fun WhiteNotificationBar(activity: BaseActivity) {
            activity?.getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
            activity?.getWindow().setStatusBarColor(
                ContextCompat.getColor(
                    activity,
                    R.color.white
                )
            );// set status background white
        }

        fun blackNotificationBar(activity: BaseActivity){
            activity?.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
            activity?.getWindow().setStatusBarColor(ContextCompat.getColor(activity,R.color.black));// set status background white
        }

        fun profileNotificationBar(activity: BaseActivity){
            activity?.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
            activity?.getWindow().setStatusBarColor(ContextCompat.getColor(activity,R.color.profile_status_bar_color));// set status background white
        }



    }
}