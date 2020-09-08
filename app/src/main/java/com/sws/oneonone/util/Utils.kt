package com.sws.oneonone.util

import android.content.Context
import android.net.ConnectivityManager
import android.widget.Toast
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class Utils {

    companion object {
        fun isNetworkAvailable(context: BaseActivity?): Boolean {
            val connectivityManager =
                (context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
            return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo()
                .isConnected()
        }


        fun showToast(mContext: Context, content: String, important: Boolean) {
            val length: Int
            if (important) {
                length = Toast.LENGTH_LONG
            } else {
                length = Toast.LENGTH_SHORT
            }
            Toast.makeText(mContext, content, length).show()
        }

        fun currentTime(): String? {
            val currentDate =
                SimpleDateFormat("yyyy-MM-dd HH:MM:SS", Locale.getDefault()).format(Date())
            return currentDate
        }

    }


    fun getMilliFromDate(dateFormat:String?): Long {
        if(!dateFormat.isNullOrEmpty()) {
            var date = Date()
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:MM:ss")
            try {
                date = formatter.parse(dateFormat)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            //  println("Today is " + date)
            return date.getTime()
        } else {
            val date = Calendar.getInstance().time
            return date.time
        }
    }

    // change yyyy-MM-dd HH:MM:SS to
    fun convertDate(neededTimeMilis: Long?): String? {
        val nowTime = Calendar.getInstance()
        val neededTime = Calendar.getInstance()
        neededTime.timeInMillis = neededTimeMilis!!
        return if (neededTime[Calendar.YEAR] == nowTime[Calendar.YEAR]) {
            if (neededTime[Calendar.MONTH] == nowTime[Calendar.MONTH]) {
                if (neededTime[Calendar.DATE] - nowTime[Calendar.DATE] == 1) {
                    //here return like "Tomorrow at 12:00"
                    val formatter: SimpleDateFormat  = SimpleDateFormat("hh:mm aa")
                    "Last Seen Tomorrow at " + formatter.format(neededTimeMilis)
                } else if (nowTime[Calendar.DATE] == neededTime[Calendar.DATE]) {
                    //here return like "Today at 12:00"
                    val formatter: SimpleDateFormat  = SimpleDateFormat("hh:mm aa")
                    "Last Seen Today at " + formatter.format(neededTimeMilis)
                } else if (nowTime[Calendar.DATE] - neededTime[Calendar.DATE] == 1) {
                    //here return like "Yesterday at 12:00"
                    val formatter: SimpleDateFormat  = SimpleDateFormat("hh:mm aa")
                    "Last Seen Yesterday at " + formatter.format(neededTimeMilis)
                } else {
                    //here return like "May 31, 12:00"
                    val formatter: SimpleDateFormat  = SimpleDateFormat("MMM dd, hh:mm aa")
                    "Last Seen " +formatter.format(neededTimeMilis)
                }
            } else {
                //here return like "May 31, 12:00"
                val formatter: SimpleDateFormat  = SimpleDateFormat("MMMM dd, hh:mm aa")
                "Last Seen " +formatter.format(neededTimeMilis)
            }
        } else {
            //here return like "May 31 2010, 12:00" - it's a different year we need to show it
            val formatter: SimpleDateFormat  = SimpleDateFormat("MMMM dd YYYY, hh:mm aa")
            "Last Seen " +formatter.format(neededTimeMilis)
        }
    } // change yyyy-MM-dd HH:MM:SS to
    fun convertChatData(neededTimeMilis: Long?): String? {
        val nowTime = Calendar.getInstance()
        val neededTime = Calendar.getInstance()
        neededTime.timeInMillis = neededTimeMilis!!
        return if (neededTime[Calendar.YEAR] == nowTime[Calendar.YEAR]) {
            if (neededTime[Calendar.MONTH] == nowTime[Calendar.MONTH]) {
                if (neededTime[Calendar.DATE] - nowTime[Calendar.DATE] == 1) {
                    //here return like "Tomorrow at 12:00"
                    val formatter: SimpleDateFormat  = SimpleDateFormat("hh:mm aa")
                    "Tomorrow at " + formatter.format(neededTimeMilis)
                } else if (nowTime[Calendar.DATE] == neededTime[Calendar.DATE]) {
                    //here return like "Today at 12:00"
                    val formatter: SimpleDateFormat  = SimpleDateFormat("hh:mm aa")
                    "Today at " + formatter.format(neededTimeMilis)
                } else if (nowTime[Calendar.DATE] - neededTime[Calendar.DATE] == 1) {
                    //here return like "Yesterday at 12:00"
                    val formatter: SimpleDateFormat  = SimpleDateFormat("hh:mm aa")
                    "Yesterday at " + formatter.format(neededTimeMilis)
                } else {
                    //here return like "May 31, 12:00"
                    val formatter: SimpleDateFormat  = SimpleDateFormat("MMM dd, hh:mm aa")
                    "" +formatter.format(neededTimeMilis)
                }
            } else {
                //here return like "May 31, 12:00"
                val formatter: SimpleDateFormat  = SimpleDateFormat("MMMM dd, hh:mm aa")
                "" +formatter.format(neededTimeMilis)
            }
        } else {
            //here return like "May 31 2010, 12:00" - it's a different year we need to show it
            val formatter: SimpleDateFormat  = SimpleDateFormat("MMMM dd YYYY, hh:mm aa")
            "" +formatter.format(neededTimeMilis)
        }
    }
}