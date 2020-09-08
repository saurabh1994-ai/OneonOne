package com.sws.oneonone.util

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import android.transition.Transition
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.sws.oneonone.R


open class BaseActivity : AppCompatActivity() {

    @TargetApi(Build.VERSION_CODES.N)
    private val ORDERED_DENSITY_DP_N = intArrayOf(
        DisplayMetrics.DENSITY_LOW,
        DisplayMetrics.DENSITY_MEDIUM,
        DisplayMetrics.DENSITY_TV,
        DisplayMetrics.DENSITY_HIGH,
        DisplayMetrics.DENSITY_280,
        DisplayMetrics.DENSITY_XHIGH,
        DisplayMetrics.DENSITY_360,
        DisplayMetrics.DENSITY_400,
        DisplayMetrics.DENSITY_420,
        DisplayMetrics.DENSITY_XXHIGH,
        DisplayMetrics.DENSITY_560,
        DisplayMetrics.DENSITY_XXXHIGH
    )

    @TargetApi(Build.VERSION_CODES.N_MR1)
    private val ORDERED_DENSITY_DP_N_MR1 = intArrayOf(
        DisplayMetrics.DENSITY_LOW,
        DisplayMetrics.DENSITY_MEDIUM,
        DisplayMetrics.DENSITY_TV,
        DisplayMetrics.DENSITY_HIGH,
        DisplayMetrics.DENSITY_260,
        DisplayMetrics.DENSITY_280,
        DisplayMetrics.DENSITY_XHIGH,
        DisplayMetrics.DENSITY_340,
        DisplayMetrics.DENSITY_360,
        DisplayMetrics.DENSITY_400,
        DisplayMetrics.DENSITY_420,
        DisplayMetrics.DENSITY_XXHIGH,
        DisplayMetrics.DENSITY_560,
        DisplayMetrics.DENSITY_XXXHIGH
    )

    @TargetApi(Build.VERSION_CODES.P)
    private val ORDERED_DENSITY_DP_P = intArrayOf(
        DisplayMetrics.DENSITY_LOW,
        DisplayMetrics.DENSITY_MEDIUM,
        DisplayMetrics.DENSITY_TV,
        DisplayMetrics.DENSITY_HIGH,
        DisplayMetrics.DENSITY_260,
        DisplayMetrics.DENSITY_280,
        DisplayMetrics.DENSITY_XHIGH,
        DisplayMetrics.DENSITY_340,
        DisplayMetrics.DENSITY_360,
        DisplayMetrics.DENSITY_400,
        DisplayMetrics.DENSITY_420,
        DisplayMetrics.DENSITY_440,
        DisplayMetrics.DENSITY_XXHIGH,
        DisplayMetrics.DENSITY_560,
        DisplayMetrics.DENSITY_XXXHIGH
    )

    fun replaceFragment(fragment: BaseFragment) {
        Log.e("frag_ji1", "one")
        replaceFragment(fragment, false, true, false)
    }

    fun replaceFragment(fragment: BaseFragment, isAdd: Boolean) {
        Log.e("frag_ji1", "two")
        replaceFragment(fragment, isAdd, true, false)
    }


    fun replaceFragment(fragment: BaseFragment, isAdd: Boolean, addtobs: Boolean) {
        Log.e("frag_ji1", "three")
        replaceFragment(fragment, isAdd, addtobs, false)
    }

    fun replaceFragment(
        fragment: BaseFragment,
        isAdd: Boolean,
        addtobs: Boolean,
        forceWithoutAnimation: Boolean
    ) {
        Log.e("frag_ji1", "four")
        replaceFragment(fragment, isAdd, addtobs, forceWithoutAnimation, null)
    }




   open fun replaceFragment(
        fragment: BaseFragment?,
        isAdd: Boolean,
        addtobs: Boolean,
        forceWithoutAnimation: Boolean,
        transition: Transition?
    ) {
        //to do in child activity


        if (fragment == null)
            return
        val ft = supportFragmentManager.beginTransaction()
        val tag = fragment.javaClass.simpleName
        ft.setAllowOptimization(true)
        if (!forceWithoutAnimation) {
            if (AndroidUtilities().isAndroid5()) {
                if (transition != null) {
                    fragment.enterTransition = transition
                } else {
                    fragment.enterTransition = TransitionUtil().slide(Gravity.RIGHT)
                }
            } else {
                ft.setCustomAnimations(
                    R.anim.pull_in_right,
                    R.anim.push_out_left,
                    R.anim.pull_in_left,
                    R.anim.push_out_right
                )
            }
        }
        if (isAdd)
            ft.add(android.R.id.content, fragment, tag)
        else
            ft.replace(android.R.id.content, fragment, tag)
        if (addtobs)
            ft.addToBackStack(tag)
        ft.commitAllowingStateLoss()

    }

   open override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            getCurrentFragment()!!.onBackPressed()
        } else
            finish()

    }


    open fun getCurrentFragment(): BaseFragment? {
        return supportFragmentManager.findFragmentById(android.R.id.content) as BaseFragment
    }

    fun removeFragments(no: Int) {
        try {

            val fragmentManager = supportFragmentManager
            fragmentManager.popBackStack(fragmentManager.getBackStackEntryAt(
                fragmentManager.backStackEntryCount - no).id, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    fun clearAllStack()
    {
        val fm = supportFragmentManager
        for (i in 0 until fm.getBackStackEntryCount()) {
            fm.popBackStack()
        }
    }

    open fun adjustFontScale(configuration: Configuration) {
        configuration.fontScale = 1.0.toFloat()
        val metrics: DisplayMetrics = resources.displayMetrics
        val wm: WindowManager =
            getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.getDefaultDisplay().getMetrics(metrics)
        metrics.scaledDensity = configuration.fontScale * metrics.density
        baseContext.resources.updateConfiguration(configuration, metrics)
    }


   @TargetApi(Build.VERSION_CODES.N)
    private fun findDensityDpCanFitScreen(densityDp:Int):Int {
        val orderedDensityDp:IntArray
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        {
            orderedDensityDp = ORDERED_DENSITY_DP_P
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
        {
            orderedDensityDp = ORDERED_DENSITY_DP_N_MR1
        }
        else
        {
            orderedDensityDp = ORDERED_DENSITY_DP_N
        }
        var index = 0
        while (densityDp >= orderedDensityDp[index])
        {
            index++
        }
        return orderedDensityDp[index]
    }
}
