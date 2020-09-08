package com.sws.oneonone.util

import android.content.Context
import android.os.Build
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import com.sws.oneonone.R
import com.sws.oneonone.fragment.WaterChallengeFragment

class DiscardPopup {

    fun discardDialog(rootLayout: RelativeLayout, activity: BaseActivity) {
        // Initialize a new layout inflater instance
        val inflater: LayoutInflater =
            activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // Inflate a custom view using layout inflater
        val view = inflater.inflate(R.layout.layout_popup_discard, null)
        // Initialize a new instance of popup window
        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
            LinearLayout.LayoutParams.MATCH_PARENT // Window height
        )
        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10.0F
        }

        // Get the widgets reference from custom view
        val btnDiscard: TextView = view.findViewById<TextView>(R.id.btnDiscard)
        val btnKeep: TextView = view.findViewById<TextView>(R.id.btnKeep)
        // Set a click listener for popup's button widget
        btnDiscard.setOnClickListener {
            // Dismiss the popup window
            popupWindow.dismiss()
            activity.onBackPressed()
        }

        btnKeep.setOnClickListener {
            // Dismiss the popup window
            popupWindow.dismiss()
        }


        // Finally, show the popup window on app
        TransitionManager.beginDelayedTransition(rootLayout)
        popupWindow.showAtLocation(
            rootLayout, // Location to display popup window
            Gravity.CENTER, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )

    }

}