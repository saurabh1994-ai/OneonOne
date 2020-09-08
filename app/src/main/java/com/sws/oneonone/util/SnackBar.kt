package com.sws.oneonone.util

import android.graphics.Color
import android.widget.TextView
import com.androidadvance.topsnackbar.TSnackbar

class SnackBar {

    fun internetSnackBar(activity: BaseActivity?){
        val snackbar = TSnackbar.make(activity!!.findViewById(android.R.id.content), "Please check your internet connection...", TSnackbar.LENGTH_LONG)
        snackbar.setActionTextColor(Color.parseColor("#50FF5733"))
        val snackbarView = snackbar.getView()
        snackbarView.setBackgroundColor(Color.parseColor("#50FF5733"))
        val textView = snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.WHITE)
        snackbar.show()
    }

    fun internetSnackBar(activity: BaseActivity?, message: String?){
        if (!message.isNullOrEmpty()) {
            val snackbar = TSnackbar.make(activity!!.findViewById(android.R.id.content), message, TSnackbar.LENGTH_LONG)
            snackbar.setActionTextColor(Color.parseColor("#50FF5733"))
            val snackbarView = snackbar.getView()
            snackbarView.setBackgroundColor(Color.parseColor("#50FF5733"))
            val textView = snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text) as TextView
            textView.setTextColor(Color.WHITE)
            snackbar.show()
        }
    }
}