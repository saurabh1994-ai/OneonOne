package com.sws.oneonone.firebase

import android.util.Log
import com.google.firebase.FirebaseError
import com.google.firebase.database.*
import com.sws.oneonone.firebaseModel.UserModel
import com.sws.oneonone.firebase_notification.NotiModel
import com.sws.oneonone.firebase_notification.RootModel
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.util.BaseActivity
import okhttp3.ResponseBody
import retrofit2.Callback
import tourhq.sws.com.tourhq.retrofit.NotificationApiClient

class NotificationHandle {

    companion object {
        private var mFirebaseDatabase: DatabaseReference? = null
        private var mFirebaseInstance: FirebaseDatabase? = null

        fun send(title: String?, message: String?, userId: String){
                mFirebaseInstance = FirebaseDatabase.getInstance()
                // get reference to 'users' node
                mFirebaseDatabase = mFirebaseInstance?.getReference("Users")
                var mDatabase = FirebaseDatabase.getInstance().getReference()
                mDatabase.child("Users").child(userId)
                    .addValueEventListener(object: ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                        }
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val user = dataSnapshot.getValue(UserModel::class.java)
                            if(user != null) {
                                val fmc : String? = user?.getFcmToken()
                                if(!user?.getFcmToken().isNullOrEmpty()) {
                                    sendNotificationToUser(title, user?.getFcmToken(), message)
                                }
                            }
                        }
                        fun onCancelled(firebaseError: FirebaseError) {
                            //Error
                        }
                    })

        }

        private fun sendNotificationToUser(title: String?, token: String?, message: String?) {
            val service: NotificationApiClient? = NotificationApiClient()
            val rootModel = RootModel()
            //token, NotiModel("Title", "Body"), DataModel("Name", "30")
            rootModel?.setToken(token)
            val notiModel = NotiModel()
            notiModel?.setBody(message)
            notiModel?.setTitle(title)
            rootModel?.setNotification(notiModel)
            val apiService = service?.getClient()?.create(ApiInterface::class.java)
            val responseBodyCall = apiService?.chatNotification(rootModel)
            responseBodyCall?.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: retrofit2.Call<ResponseBody>,
                    response: retrofit2.Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        Log.d("TAGqwrty", "Successfully notification send by using retrofit.")
                        Log.e("qwertyuiop", response.toString())
                    } else {
                        Log.d("TAGqwrty", "Failed notification send by using retrofit.")
                        Log.e("qwertyuiop", response.toString())
                    }
                }

                override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                    Log.d("TAGqwrty_error", t.message)
                }
            })
        }
    }
}