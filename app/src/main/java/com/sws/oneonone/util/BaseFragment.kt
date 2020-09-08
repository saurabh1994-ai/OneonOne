package com.sws.oneonone.util

import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.Fragment


open class BaseFragment : Fragment(){

    protected var TAG = "OneOnOne"
    protected lateinit var activity: BaseActivity


  override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = getActivity() as BaseActivity
        activity.window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        )
        TAG = javaClass.simpleName

    }




   open fun onBackPressed() {
        try {
            AndroidUtilities().hideKeyboard(view)
            fragmentManager!!.popBackStack()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
