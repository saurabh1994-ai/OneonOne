package com.sws.oneonone.firebase

import android.app.Activity
import android.text.TextUtils
import android.util.Log
import com.google.firebase.FirebaseError
import com.google.firebase.database.*
import com.google.firebase.database.FirebaseDatabase
import com.sws.oneonone.firebaseModel.UserModel
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.Utils

class FirebaseUserHandle {

    private var uId: String? = null
    // var pref: PreferenceStore? = null
    private var mContext: Activity? = null
    private var mFirebaseDatabase: DatabaseReference? = null
    private var mFirebaseInstance: FirebaseDatabase? = null

    fun initializeFirebase(activity: BaseActivity?) {
        mFirebaseInstance = FirebaseDatabase.getInstance()
        mFirebaseDatabase = mFirebaseInstance?.getReference("Users")
        if (Utils.isNetworkAvailable(activity!!)) {
            val scoresRef = FirebaseDatabase.getInstance().getReference("Users")
            scoresRef.keepSynced(false)
        } else {
            val scoresRef = FirebaseDatabase.getInstance().getReference("Users")
            scoresRef.keepSynced(true)
        }
    }


    fun userAlreadyAxist(activity: BaseActivity?, userId: String?, userModel: UserModel?) {
        mFirebaseInstance = FirebaseDatabase.getInstance()
        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance?.getReference("Users")
        var mDatabase = FirebaseDatabase.getInstance().getReference()
        //mDatabase.child("Users").orderByChild("lastSeen").equalTo("2020-08-06 17:08:42")
        mDatabase.child("Users").orderByChild(userId!!)//("userEmail").equalTo(userModel?.getUserEmail())
       /* mDatabase.child("Users").orderByChild("userEmail").equalTo(userModel?.getUserEmail())
            .addValueEventListener(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        updateUser(activity!!, userId, userModel)
                    } else {
                        createUser(activity!!, userId, userModel) ///padding
                    }
                }
            });*/
            .addListenerForSingleValueEvent(object :ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    if (dataSnapshot.exists()) {
                        updateUser(activity!!, userId, userModel)
                    } else {
                        createUser(activity!!, userId, userModel) ///padding
                    }
                }
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }
    fun createUser(activity: BaseActivity, userId: String?, userModel: UserModel?) {
        mFirebaseInstance = FirebaseDatabase.getInstance()
        mFirebaseDatabase = mFirebaseInstance?.getReference("Users")
        if (Utils.isNetworkAvailable(activity!!)) {
            val scoresRef = FirebaseDatabase.getInstance().getReference("Users")
            scoresRef.keepSynced(false)
        } else {
            val scoresRef = FirebaseDatabase.getInstance().getReference("Users")
            scoresRef.keepSynced(true)
        }

        uId = userId
        // val user = Users(name, email, image, coins)
        mFirebaseDatabase?.child(userId!!)?.setValue(userModel)
        addUserChangeListener(userId)
    }


    fun addUserChangeListener(userId: String?) {
        uId = userId
        mFirebaseDatabase?.child(userId!!)?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(UserModel::class.java)
                if (user != null) {
                    // pref?.saveIntegerData("coin", user?.getTotalCoins()!!.toLong())
                    return
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.e("firebase_detail", "Failed to read user", error.toException())
            }
        })
    }


    fun updateUser(activity: BaseActivity?, userId: String?, userModel: UserModel?) {
        // updating the user via child nodes
        uId = userId
        if (!TextUtils.isEmpty(userModel?.getFcmToken()))
            mFirebaseDatabase?.child(userId!!)?.child("fcmToken")?.setValue(userModel?.getFcmToken())

        //if (!TextUtils.isEmpty(userModel?.getIsTyping()))
        mFirebaseDatabase?.child(userId!!)?.child("isTyping")?.setValue(userModel?.getIsTyping())

        if (!TextUtils.isEmpty(userModel?.getLastSeen()))
            mFirebaseDatabase?.child(userId!!)?.child("lastSeen")?.setValue(userModel?.getLastSeen())

        if (!TextUtils.isEmpty(userModel?.getNotificationStatus()))
            mFirebaseDatabase?.child(userId!!)?.child("notificationStatus")?.setValue(userModel?.getNotificationStatus())

        // if (!TextUtils.isEmpty(userModel?.getOnChatScreen()))
        mFirebaseDatabase?.child(userId!!)?.child("onChatScreen")?.setValue(userModel?.getOnChatScreen())

        //   if (!TextUtils.isEmpty(userModel?.getStatus()))
        mFirebaseDatabase?.child(userId!!)?.child("status")?.setValue(userModel?.getStatus())

        if (!TextUtils.isEmpty(userModel?.getUserEmail()))
            mFirebaseDatabase?.child(userId!!)?.child("userEmail")?.setValue(userModel?.getUserEmail())

        if (!TextUtils.isEmpty(userModel?.getUserImg()))
            mFirebaseDatabase?.child(userId!!)?.child("userImg")?.setValue(userModel?.getUserImg())

        if (!TextUtils.isEmpty(userModel?.getUserName()))
            mFirebaseDatabase?.child(userId!!)?.child("userName")?.setValue(userModel?.getUserName())

        mFirebaseDatabase?.addChildEventListener(childEventListener)
    }

    // change User active and unActive status
    fun updateUserActiveStatus(activity: BaseActivity?, userId: String?, status: Boolean) {
        initializeFirebase(activity)
        mFirebaseDatabase?.child(userId!!)?.child("status")?.setValue(status)

    }


    val childEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
            val comment = dataSnapshot.getValue(UserModel::class.java)
            if (!uId.isNullOrEmpty()) {
                addUserChangeListener(uId)
            }
            //  Log.e("updated_data_coin", comment?.getTotalCoins().toString())
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
            Log.e("updated_data", "onChildChanged: ${dataSnapshot.key}")

            // A comment has changed, use the key to determine if we are displaying this
            // comment and if so displayed the changed comment.
            val newComment = dataSnapshot.getValue(UserModel::class.java)
            val commentKey = dataSnapshot.key
            // Log.e("updated_data_coin2", newComment?.getTotalCoins().toString())


            // ...
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            Log.e("updated_data", "onChildRemoved:" + dataSnapshot.key!!)

            // A comment has changed, use the key to determine if we are displaying this
            // comment and if so remove it.
            val commentKey = dataSnapshot.key

            // ...
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
            Log.e("updated_data", "onChildMoved:" + dataSnapshot.key!!)

            // A comment has changed position, use the key to determine if we are
            // displaying this comment and if so move it.
            val movedComment = dataSnapshot.getValue(UserModel::class.java)
            val commentKey = dataSnapshot.key

            // ...
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.e("updated_data", "postComments:onCancelled", databaseError.toException())
            // Toast.makeText(context, "Failed to load comments.",
            //   Toast.LENGTH_SHORT).show()
        }
    }

}