package com.sws.oneonone.viewModel

import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseError
import com.google.firebase.database.*
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.sws.oneonone.firebase.NotificationHandle
import com.sws.oneonone.firebaseModel.ChatUserModel
import com.sws.oneonone.firebaseModel.UserModel
import com.sws.oneonone.firebase_notification.NotiModel
import com.sws.oneonone.firebase_notification.RootModel
import com.sws.oneonone.fragment.NewChatFragment
import com.sws.oneonone.fragment.ProfileFragment
import com.sws.oneonone.model.SignUpModel
import com.sws.oneonone.util.AppStaticData
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.Utils
import kotlin.collections.ArrayList

class ChatMessageVM: ViewModel() {
    var chatWith: MutableLiveData<String>? = MutableLiveData<String>()
    var chatKey: MutableLiveData<String>? = MutableLiveData<String>()
    var chatName: MutableLiveData<String>? = MutableLiveData<String>()
    var isGroup: MutableLiveData<Boolean>? = MutableLiveData<Boolean>()
    var chatLastSeen: MutableLiveData<String>? = MutableLiveData<String>()
    var groupMembers: ArrayList<String>? = ArrayList<String>()
    var message: MutableLiveData<String>? = MutableLiveData<String>()

    private var commanMLD: MutableLiveData<ArrayList<ChatUserModel>>? = null
    private var mFirebaseDatabase: DatabaseReference? = null
    private var mFirebaseInstance: FirebaseDatabase? = null
    var activity: BaseActivity? = null

    var chatUserList: ArrayList<ChatUserModel> = ArrayList<ChatUserModel>()

    val comman: MutableLiveData<ArrayList<ChatUserModel>>
        get() {
            if (commanMLD == null)
            {
                commanMLD = MutableLiveData()
            }
            return commanMLD as MutableLiveData<ArrayList<ChatUserModel>>
        }

   /* fun onClickProfile() {
        activity?.replaceFragment(ProfileFragment())
    }

    fun onClickNewChat() {
        activity?.replaceFragment(NewChatFragment())
    }
*/
    fun showingList(){
        initializeFirebase()
        getAxistUserChat()
    }

    fun sendImage(url: String?) {
        val model = ChatUserModel()
        model?.setImage(url)
        model?.setMessage("")
        model?.setIsGroup(isGroup?.value!!)
        if (!isGroup?.value!!){
            model?.setMsgStatus("unread")
            model?.setRead("0")
        }
        model?.setRecieverDelete("no")
        model?.setRecieverId(chatWith?.value)
        model?.setSenderDelete("no")
        model?.setSenderId(AppStaticData().getModel()?.result?.id)
        model?.setTime(Utils?.currentTime())
        model?.setVideo("")
        model?.setVideoThumb("")
        getMsgStatus(chatKey?.value, model)
       // sendMessage(chatKey?.value, model)
    }
    fun sendVideo(videoUrl: String?, thumbNail: String?){
        val model = ChatUserModel()
        model?.setImage("")
        model?.setMessage("")
        model?.setIsGroup(isGroup?.value)
        if (!isGroup?.value!!) {
            model?.setMsgStatus("unread")
            model?.setRead("0")
        }
        model?.setRecieverDelete("no")
        model?.setRecieverId(chatWith?.value)
        model?.setSenderDelete("no")
        model?.setSenderId(AppStaticData().getModel()?.result?.id)
        model?.setTime(Utils?.currentTime())
        model?.setVideo(videoUrl)
        model?.setVideoThumb(thumbNail)
        getMsgStatus(chatKey?.value, model)
       // sendMessage(chatKey?.value, model)
    }
    fun sendMessage(){
        if(!message?.value.isNullOrEmpty()){
            val model = ChatUserModel()
            model?.setImage("")
            model?.setMessage(message?.value)
            model?.setIsGroup(isGroup?.value)
            if (!isGroup?.value!!) {
                model?.setMsgStatus("unread")
                model?.setRead("0")
            }
            model?.setRecieverDelete("no")
            model?.setRecieverId(chatWith?.value)
            model?.setSenderDelete("no")
            model?.setSenderId(AppStaticData().getModel()?.result?.id)
            model?.setTime(Utils?.currentTime())
            model?.setVideo("")
            model?.setVideoThumb("")

            getMsgStatus(chatKey?.value, model)
          //  sendMessage(chatKey?.value, model)
        }
    }

    fun initializeFirebase() {
        mFirebaseInstance = FirebaseDatabase.getInstance()
        mFirebaseDatabase = mFirebaseInstance?.getReference("Chat")
        if (Utils.isNetworkAvailable(activity!!)) {
            val scoresRef = FirebaseDatabase.getInstance().getReference("Chat")
            scoresRef.keepSynced(false)
        } else {
            val scoresRef = FirebaseDatabase.getInstance().getReference("Chat")
            scoresRef.keepSynced(true)
        }
    }

    fun getAxistUserChat() {
        if(chatKey?.value.isNullOrEmpty()){
            val userIds: String? = AppStaticData().getModel()?.result?.id
            val chatKeys: String? = chatWith?.value+"_"+userIds
            chatKey?.value = chatKeys
            getChatData()
        } else {
            getChatData()
        }
    }
    fun getChatData(){
        chatUserList?.clear()
         mFirebaseInstance = FirebaseDatabase.getInstance()
        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance?.getReference("Chat")
        var mDatabase = FirebaseDatabase.getInstance().getReference()
        mDatabase.child("Chat").child(chatKey?.value!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    chatUserList?.clear()
                    if(dataSnapshot == null){
                        val userIds: String? = AppStaticData().getModel()?.result?.id
                        val chatKeys: String? = userIds+"_"+chatWith?.value
                        chatKey?.value = chatKeys
                        getChatDataSecond()
                    }
                    chatUserList?.clear()
                    for (childDataSnapshot in dataSnapshot.getChildren()) {
                        val user = childDataSnapshot.getValue(ChatUserModel::class.java)
                        val msgKey: String? = childDataSnapshot?.key
                        val list = ChatUserModel()
                        list.setChatUserModel(user)
                        list.setUserId(AppStaticData().getModel()?.result?.id)
                        list.setMsgKey(msgKey)
                        list.setChatKey(chatKey?.value)
                        chatUserList?.add(list!!)
                        Log.e("childDataSnapshot", user.toString())
                    }
                    comman?.value = chatUserList
                }
            })
    }


   /* // get user List data
    fun existingUser(userId: String) {
        mFirebaseInstance = FirebaseDatabase.getInstance()
        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance?.getReference("Users")
        var mDatabase = FirebaseDatabase.getInstance().getReference()
        mDatabase.child("Users").child(userId)
            .addValueEventListener(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(ChatUserModel::class.java)
                    if(user != null) {
                        val list = ChatUserModel()
                        list?.setChatUserModel(user)
                        chatUserList?.add(list)
                        comman?.value = chatUserList
                    }
                }
                fun onCancelled(firebaseError: FirebaseError) {
                    //Error
                }
            })
    }
*/
    // For msgStatus
    fun getMsgStatus(chatId: String?, chatUserModel: ChatUserModel?){
       if(!isGroup?.value!!) {

           mFirebaseInstance = FirebaseDatabase.getInstance()
           // get reference to 'users' node
           mFirebaseDatabase = mFirebaseInstance?.getReference("Users")
           var mDatabase = FirebaseDatabase.getInstance().getReference()

           mDatabase.child("Users").child(chatWith?.value!!)
               .addValueEventListener(object : ValueEventListener {
                   override fun onCancelled(p0: DatabaseError) {
                       Log.e("errorMessage", p0.message)
                   }

                   override fun onDataChange(dataSnapshot: DataSnapshot) {
                       val user = dataSnapshot.getValue(UserModel::class.java)
                       if (user != null) {
                           val status: Boolean = user?.getStatus()!!
                           if (status) {
                               chatUserModel?.setMsgStatus("delivered")
                           } else {
                               chatUserModel?.setMsgStatus("sent")
                           }
                           if (!chatUserModel?.getRecieverId().isNullOrEmpty()) {
                               sendMessage(chatKey?.value, chatUserModel)
                               //chatUserModel?.setRecieverId("")
                           }
                       }
                   }

               })
       }else {
           checkGroupUserStatus(chatKey?.value!!, chatUserModel)
        }
    }

    fun checkGroupUserStatus(chatKey: String, chatUserModel: ChatUserModel?){
        if(!groupMembers.isNullOrEmpty()) {
            var isSent: Boolean = true
            for (i in 0..(groupMembers?.size!!-1)) {
                mFirebaseInstance = FirebaseDatabase.getInstance()
                // get reference to 'users' node
                mFirebaseDatabase = mFirebaseInstance?.getReference("Users")
                var mDatabase = FirebaseDatabase.getInstance().getReference()

               // mDatabase.child("Users").child(chatWith?.value!!)
                mDatabase.child("Users").child(groupMembers?.get(i)!!)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                            Log.e("errorMessage", p0.message)
                        }

                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val user = dataSnapshot.getValue(UserModel::class.java)
                            if (user != null) {
                                val status: Boolean = user?.getStatus()!!
                                if(status){
                                    if (isSent) {
                                        isSent = status
                                    }
                                } else {
                                    isSent = status
                                }
                            }
                            if(i == (groupMembers?.size!!-1)) {
                             //   if (!chatUserModel?.getRecieverId().isNullOrEmpty()) {
                                    if (isSent) {
                                        chatUserModel?.setMsgStatus("delivered")
                                    } else {
                                        chatUserModel?.setMsgStatus("sent")
                                    }
                                    sendMessage(chatKey, chatUserModel)
                                   // chatUserModel?.setRecieverId("")
                                    isSent = true
                                }
                           // }
                        }

                    })

            }
        }
    }



    fun sendMessage(chatId: String?, chatUserModel: ChatUserModel?) {
        mFirebaseInstance = FirebaseDatabase.getInstance()
        mFirebaseDatabase = mFirebaseInstance?.getReference("Chat")
        if (Utils.isNetworkAvailable(activity!!)) {
            val scoresRef = FirebaseDatabase.getInstance().getReference("Chat")
            scoresRef.keepSynced(false)
        } else {
            val scoresRef = FirebaseDatabase.getInstance().getReference("Chat")
            scoresRef.keepSynced(true)
        }

        val key: String? =  mFirebaseInstance?.getReference("Chat")?.child(chatId!!)?.push()?.key
        mFirebaseDatabase?.child(chatId!!)?.child(key!!)?.setValue(chatUserModel)
        var senderName = AppStaticData().getModel()?.result?.fullName
        if(isGroup?.value!!) {
            if(!groupMembers.isNullOrEmpty()) {
                for (i in 0..(groupMembers?.size!! - 1)) {
                    if (AppStaticData().getModel()?.result?.id.equals(groupMembers?.get(i))) {
                        mFirebaseDatabase?.child(chatId!!)?.child(key!!)?.child("member")?.child(groupMembers?.get(i)!!)?.child("read")?.setValue("1")
                        mFirebaseDatabase?.child(chatId!!)?.child(key!!)?.child("member")?.child(groupMembers?.get(i)!!)?.child("msgStatus")?.setValue("read")
                    } else {
                        mFirebaseDatabase?.child(chatId!!)?.child(key!!)?.child("member")?.child(groupMembers?.get(i)!!)?.child("read")?.setValue("0")
                        mFirebaseDatabase?.child(chatId!!)?.child(key!!)?.child("member")?.child(groupMembers?.get(i)!!)?.child("msgStatus")?.setValue("sent")

                        // send notification
                        if (!chatUserModel?.getMessage().isNullOrEmpty()){
                            NotificationHandle?.send(senderName, chatUserModel?.getMessage(), groupMembers?.get(i)!!)
                        } else if (!chatUserModel?.getImage().isNullOrEmpty()) {
                            NotificationHandle?.send(senderName, "You got a image!", groupMembers?.get(i)!!)
                        } else if(!chatUserModel?.getVideo().isNullOrEmpty()){
                            NotificationHandle?.send(senderName, "You got a video!", groupMembers?.get(i)!!)
                        } else {

                        }

                    }
                }
            }
        } else {
            if (!chatUserModel?.getMessage().isNullOrEmpty()){
                NotificationHandle?.send(senderName, chatUserModel?.getMessage(), chatWith?.value!!)
            } else if (!chatUserModel?.getImage().isNullOrEmpty()) {
                NotificationHandle?.send(senderName, "You got a image!", chatWith?.value!!)
            } else if(!chatUserModel?.getVideo().isNullOrEmpty()){
                NotificationHandle?.send(senderName, "You got a video!", chatWith?.value!!)
            } else {

            }
        }
        message?.value = ""
    }

    fun getChatDataSecond() {
        chatUserList?.clear()
        mFirebaseInstance = FirebaseDatabase.getInstance()
        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance?.getReference("Chat")
        var mDatabase = FirebaseDatabase.getInstance().getReference()
        var mDatabase1 = FirebaseDatabase.getInstance().getReference()
        mDatabase.child("Chat").child(chatKey?.value!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    chatUserList?.clear()
                    for (childDataSnapshot in dataSnapshot.getChildren()) {
                        val user = childDataSnapshot.getValue(ChatUserModel::class.java)
                        val list = ChatUserModel()
                        list.setChatUserModel(user)
                        list.setUserId(AppStaticData().getModel()?.result?.id)
                        chatUserList?.add(list!!)
                    }
                    comman?.value = chatUserList
                }

            })

    }

}