package com.sws.oneonone.util

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.sws.oneonone.R
import com.sws.oneonone.dialog.AlertDialog
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class AndroidUtilities {

    var linkPatterns = "#[/^#[A-Za-z0-9_-]*$/]+"



    fun isAndroid5(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }

    fun hideKeyboard(view: View?) {
        if (view == null) {
            return
        }
        try {
            val imm =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (!imm.isActive) {
                return
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        } catch (e: Exception) {

        }

    }

    fun getSavedBitmapPath(filename: String, context: Context, bmp: Bitmap): File? {
        try {
            val mediaStorageDir = File(context.cacheDir, "Downloads")

            if (!mediaStorageDir.exists()) {
                Log.d("check++", "checkkkk")
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("check++++", "checkkkk")
                    return null
                }
            }

            val mediaFile: File
            if (!TextUtils.isEmpty(filename)) {
                mediaFile = File(mediaStorageDir.path + File.separator + filename + ".jpeg")
            } else {
                return null
            }
            try {
                val out = FileOutputStream(mediaFile)
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                out.close()
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }

            return mediaFile
        } catch (e: Exception) {

            e.printStackTrace()
            return null
        }

    }


     fun getTime(time: String, activity:BaseActivity): String {
        @SuppressLint("SimpleDateFormat")
        val spf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        spf.timeZone = TimeZone.getTimeZone("UTC")
        try {
            val newDate = spf.parse(time)
            val currentTime = Calendar.getInstance().time
            if (currentTime.date == newDate!!.date) {
                val mills = (currentTime.time - newDate.time)
                val hours = mills / (1000 * 60 * 60)
                val mins = mills / (1000 * 60) % 60
                return if (hours > 0) {
                    hours.toString() + " " + activity.getString(R.string.hour_ago)
                } else {
                    if (mins > 0)
                        mins.toString() + " " + activity.getString(R.string.minutes_ago)
                    else
                        ""
                }
            } else if (currentTime.month == newDate.month) {
                return (currentTime.date - newDate.date).toString() + " "+ activity.getString(R.string.day_ago)
            } else if (currentTime.month != newDate.month) {

                return (currentTime.month - newDate.month).toString()+ " " + activity.getString(R.string.month_ago)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""

    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    fun createDynamicLink(activity: BaseActivity, msg: String, uid:String?, profileId:String?,forShare:String?) {
        var link = ""
        if(uid!!.isNotEmpty()){
           link = "https://oneononeapp.page.link/open?uid=$uid"
        }else if(!profileId.isNullOrEmpty()){
            link = "https://oneononeapp.page.link/open?profileId=$profileId"
        }

        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse(link))
            .setDomainUriPrefix("https://oneononeapp.page.link")
            .setIosParameters(
                DynamicLink.IosParameters.Builder("com.sws.1on1app")
                    .build() )
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder("com.sws.oneonone")
                    .setMinimumVersion(1)
                    .build())
            .setSocialMetaTagParameters(
                 DynamicLink.SocialMetaTagParameters.Builder()
                    .setTitle("1on1")
                    .setDescription(if(uid.isNotEmpty())"checkOut some cool Challenges" else "checkOut my cool profile")
                     //.setImageUrl(Uri.parse("https://onestickers.com/img/main-logo.png"))
                    .build())
            .buildShortDynamicLink()
            .addOnSuccessListener { shortDynamicLink ->
                val mInvitationUrl = shortDynamicLink.shortLink.toString()
                if(!forShare.isNullOrEmpty() && forShare == "SnapChat"){
                    try {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.setPackage("com.snapchat.android")
                        var shareMessage = "\n" + msg + "\n"
                        shareMessage =
                            shareMessage + mInvitationUrl + "\n"
                        intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                        intent.type = "text/plain"
                        activity. startActivity(intent)
                    } catch (e: Exception) {
                        openAlertDialog("Can't be shared.Snapchat isn't Installed",activity)
                    }


                }else if(!forShare.isNullOrEmpty() && forShare == "Whatsupp"){
                    try {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.setPackage("com.whatsapp")
                        var shareMessage = "\n" + msg + "\n"
                        shareMessage =
                            shareMessage + mInvitationUrl + "\n"
                        intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                        intent.type = "text/plain"
                        activity.startActivity(intent)
                    } catch (e: Exception) {
                        openAlertDialog("Can't be shared.Watsapp isn't Installed",activity)
                    }


            }else if(!forShare.isNullOrEmpty() && forShare == "Sms"){
                    try {
                        val defaultSmsPackageName: String = Telephony.Sms.getDefaultSmsPackage(activity) // Need to change the build to API 19
                        val sendIntent = Intent(Intent.ACTION_SEND)
                        sendIntent.type = "text/plain"
                        var shareMessage = "\n" + msg + "\n"
                        shareMessage =
                            shareMessage + mInvitationUrl + "\n"
                        sendIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                        if (defaultSmsPackageName.isNotEmpty())  {
                            sendIntent.setPackage(defaultSmsPackageName)
                        }
                        activity.startActivity(sendIntent)
                    } catch (e: Exception) {
                    }


                }else{

                    val intent = Intent()
                    intent.action = Intent.ACTION_SEND
                    intent.type = "text/plain"
                    var shareMessage = "\n" + msg + "\n"
                    shareMessage =
                        shareMessage + mInvitationUrl + "\n"
                    intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                    activity.startActivity(Intent.createChooser(intent, "choose one"))
                    Log.d("AAA","test1 success $mInvitationUrl")
                }

            }
            .addOnFailureListener {
                Log.d("AAA", "test1 fail")
                it.printStackTrace()
            }



    }


    fun openAlertDialog(txtmsg :String,activity: BaseActivity){
        val myDialog = AlertDialog.newInstance(activity,txtmsg)
        myDialog.show(activity.supportFragmentManager, "Hello Testing")
    }


  fun generateRandomBg() : GradientDrawable{
      val r = Random()
      val red: Int = r.nextInt(255 - 0 + 1) + 0
      val green: Int = r.nextInt(255 - 0 + 1) + 0
      val blue: Int = r.nextInt(255 - 0 + 1) + 0

      val draw = GradientDrawable()
      draw.shape = GradientDrawable.RECTANGLE
      draw.setColor(Color.rgb(red, green, blue))
      return draw
  }


    fun apisResponse(activity: BaseActivity, resultCode: Int, msg: String?): Boolean {

        when (resultCode) {

            200 -> {
                return true
            }

          /*  AppConstants.unsuccess -> {
                snackBarMessage(activity, msg!!, 2)
                return false
            }

            AppConstants.authenticationError -> {

                NotificationCenter.getInstance()
                    .postNotificationName(NotificationCenter.alertDialog, msg)
            }*/

            203-> {

                NotificationCenter.getInstance()
                    .postNotificationName(NotificationCenter.alertDialog, msg)
            }

            404 -> {
                return true
            }

            else -> {
                AndroidUtilities().openAlertDialog( msg!!, activity)
                return false
            }

        }
        return false
    }


}
