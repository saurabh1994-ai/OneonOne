package com.sws.oneonone.firebase


import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sws.oneonone.R
import com.sws.oneonone.activity.MainActivity
import com.sws.oneonone.util.AppStaticData


import org.json.JSONObject

class MyFirebaseMessagingService : FirebaseMessagingService() {
    var message = ""
  //  val androidUtilities = AndroidUtilities()
    override fun onNewToken(mToken: String) {
        super.onNewToken(mToken)
        Log.e("TOKEN", mToken)
         AppStaticData.setFmcToken(mToken)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) { var json:JSONObject? = null
// Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.d("Notification++++++: " ,remoteMessage.notification!!.body)
            message = remoteMessage.notification!!.body!!

        }

        // Check if message contains a data payload.6
        if (remoteMessage.data.isNotEmpty()) {
            Log.d( "Notification+++++++: " , remoteMessage.data.toString())

            try {
                 json =  JSONObject(remoteMessage.data.toString())
                Log.d( "json: " , json.toString())
            } catch (e:Exception ) {
                Log.e(TAG, "Exception: " + e.message)
            }
        }




        if(json != null) {
            handleNotification(json.toString())
        } else {
            handleChatNotification(remoteMessage.notification!!.title, remoteMessage.notification!!.body)
        }

    }


    @TargetApi(26)
    private fun createChannel(
        notificationManager: NotificationManager,
        title: String,
        desc: String
    ) {
        val importance = NotificationManager.IMPORTANCE_HIGH

        val mChannel = NotificationChannel(title, title, importance)
        mChannel.description = desc
        mChannel.enableLights(true)
        mChannel.setShowBadge(true)
        notificationManager.createNotificationChannel(mChannel)
    }


    private fun handleNotification( body: String) {

        try {


            val jsonObject = JSONObject(body)
            var title = ""
            var message = ""
            var data:JSONObject? = null
            if(jsonObject.has("data1")){
                data = jsonObject.getJSONObject("data1")
            }
            if (data!!.has("title"))
                title = data.getString("title")
            if (data.has("body"))
                message = data.getString("body")

            val notifyIntent = Intent(this, MainActivity::class.java)
           notifyIntent.flags = FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            notifyIntent.putExtra("pushNotification", body)
                val notifyPendingIntent = PendingIntent.getActivity(
                this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )
            val mNotifyManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createChannel(mNotifyManager, title, body)
                // .setSmallIcon(R.mipmap.ic_launcher)
                val mBuilder = NotificationCompat.Builder(this, title)
                    .setContentTitle(title).setContentText(message)
                    .setPriority(
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    .setContentIntent(notifyPendingIntent)
                    .setAutoCancel(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                 mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                    mBuilder.color = resources.getColor(R.color.yellow)
                } else {
                    mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                }
                mNotifyManager.notify(0, mBuilder.build())
            } else {

                val mBuilder = NotificationCompat.Builder(this,title)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
                   .setContentIntent(notifyPendingIntent)
                    .setAutoCancel(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                    mBuilder.color = resources.getColor(R.color.yellow)
                } else {
                    mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                }
                mNotifyManager.notify(0, mBuilder.build())
            }

        }catch (ex:Exception){

        }




    }



    fun handleChatNotification(title: String?, body: String?){
        val notifyIntent = Intent(this, MainActivity::class.java)
        notifyIntent.flags = FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        // notifyIntent.putExtra("pushNotification", chat)
        val notifyPendingIntent = PendingIntent.getActivity(
            this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val mNotifyManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(mNotifyManager, title!!, body!!)
            // .setSmallIcon(R.mipmap.ic_launcher)
            val mBuilder = NotificationCompat.Builder(this, title)
                .setContentTitle(title).setContentText(body)
                .setPriority(
                    NotificationManager.IMPORTANCE_HIGH
                )
                .setContentIntent(notifyPendingIntent)
                .setAutoCancel(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                mBuilder.color = resources.getColor(R.color.yellow)
            } else {
                mBuilder.setSmallIcon(R.mipmap.ic_launcher)
            }
            mNotifyManager.notify(0, mBuilder.build())
        } else {

            val mBuilder = NotificationCompat.Builder(this, title!!)
                .setContentTitle(title)
                .setContentText(body)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setContentIntent(notifyPendingIntent)
                .setAutoCancel(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                mBuilder.color = resources.getColor(R.color.yellow)
            } else {
                mBuilder.setSmallIcon(R.mipmap.ic_launcher)
            }
            mNotifyManager.notify(0, mBuilder.build())
        }

    }






}
