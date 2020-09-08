package com.sws.oneonone.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.sws.oneonone.dialog.ProfileBottomDialog
import com.sws.oneonone.firebaseModel.GroupModel
import com.sws.oneonone.firebaseModel.UserModel
import com.sws.oneonone.fragment.CallFragment
import com.sws.oneonone.fragment.NewChatFragment
import com.sws.oneonone.model.SignUpModel
import com.sws.oneonone.util.AppStaticData
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.Utils
import java.util.*

class ChatUserVM: ViewModel() {
    private var msgStatus: String? = ""
   // private var commanMLD: MutableLiveData<ArrayList<ArrayList<UserModel>>>? = null
    private var commanMLD: MutableLiveData<ArrayList<UserModel>>? = null
    private var commanFilter: MutableLiveData<String>? = null
    private var chatData: MutableLiveData<ArrayList<String>>? = null
    var fragment: CallFragment? = null
    private var mFirebaseDatabase: DatabaseReference? = null
    private var mFirebaseInstance: FirebaseDatabase? = null
    var activity: BaseActivity? = null

    var chatUserList: ArrayList<UserModel> = ArrayList<UserModel>()
    var idList: ArrayList<String> = ArrayList<String>()
    var chatWithUserList: ArrayList<String> = ArrayList<String>()
    var chatKeyList: ArrayList<String> = ArrayList<String>()
    var passDataCount: Int = 0


    val comman: MutableLiveData<ArrayList<UserModel>>
        get() {
            if (commanMLD == null)
            {
                commanMLD = MutableLiveData()
            }
            return commanMLD as MutableLiveData<ArrayList<UserModel>>
        }

    val filterData: MutableLiveData<String>
        get() {
            if (commanFilter == null)
            {
                commanFilter = MutableLiveData()
            }
            return commanFilter as MutableLiveData<String>
        }


    // call profile fragment
    fun onClickProfile() {
        val  videoBottomSheetDialog = ProfileBottomDialog.newInstance(activity!!,"",null,null)
        videoBottomSheetDialog.show(activity!!.supportFragmentManager, ProfileBottomDialog().TAG)
    }

    // searching in chat user list
    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (!s.isEmpty()) {
            filterData?.value = s?.toString()
        } else {
            filterData?.value = ""
        }
    }

    // call new chat fragment to add new chat user
    fun onClickNewChat() {
        activity?.replaceFragment(NewChatFragment())
    }

    //  initialize firebase database
    // check user exist or not
    fun showingList(){
        initializeFirebase()
        //userAlreadyAxist()
        getUserChat()
    }

    //  initialize firebase database
    fun initializeFirebase() {
        mFirebaseInstance = FirebaseDatabase.getInstance()
        mFirebaseDatabase = mFirebaseInstance?.getReference("Chat")
        // check internet connection
        if (Utils.isNetworkAvailable(activity!!)) {
            val scoresRef = FirebaseDatabase.getInstance().getReference("Chat")
            scoresRef.keepSynced(false)
        } else {
            val scoresRef = FirebaseDatabase.getInstance().getReference("Chat")
            scoresRef.keepSynced(true)
        }
    }

    fun getUserChat(){
        var model: SignUpModel? = AppStaticData().getModel()
        val ids: String = model?.result?.id!!
        var mDatabase = FirebaseDatabase.getInstance().getReference()
        mDatabase.child("Chat")
            .addValueEventListener(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    chatWithUserList?.clear()
                    chatKeyList?.clear()
                    var position: Int? = 0
                    for (childDataSnapshot in dataSnapshot.getChildren()) {
                        position = position?.plus(1)
                        val firebaseId: String = childDataSnapshot.getKey()!!
                        if(firebaseId.contains(ids)){
                            //chatWithUserList?.add(firebaseId)
                            val yourArray: List<String> = firebaseId.split("_")
                            val one: String = yourArray[0]
                            val two: String = yourArray[1]
                            chatKeyList?.add(firebaseId)
                            if (yourArray.size > 2){ //is group
                                chatWithUserList?.add(firebaseId)
                            } else { //is single chat
                                if (one.equals(ids)){
                                    chatWithUserList?.add(two)
                                } else {
                                    chatWithUserList?.add(one)
                                }
                            }
                        }
                        val count: Int = dataSnapshot.childrenCount.toInt()
                        if(position == count){
                            completeFirstProcess()
                        }
                    }
                }
            })
    }


    fun completeFirstProcess(){
        chatUserList?.clear()
        if (!chatWithUserList.isNullOrEmpty()) {
            passDataCount = 0
            fragment?.clearList()
            for (i in 0..(chatWithUserList.size-1)){
                val yourArray: List<String> = chatWithUserList?.get(i).split("_")
                if (yourArray.size > 2){
                    groupUserDetails(chatWithUserList?.get(i), "0", msgStatus)
                } else {
                     existingUser(chatWithUserList?.get(i), chatKeyList?.get(i), "0", msgStatus)
                }
            }
        }
    }


    // get user List data
    fun existingUser(userId: String, chatKey: String?, unreadCount: String?, mStatus: String?) {
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
                        val list = UserModel()
                        list?.setUserMadelData(user)
                        list?.setChatKey(chatKey)
                        list?.setWithChatUserId(userId)
                        list?.setReadable(unreadCount)
                        list?.setMsgStatus(mStatus)
                        chatUserList?.add(list)

                        if (chatUserList.size == chatWithUserList.size) {
                        comman?.value = chatUserList // list
                        }
                    }
                }

            })
    }

    // get user List data
    fun groupUserDetails(chatKey: String?, unreadCount: String?, mStatus: String?) {
        mFirebaseInstance = FirebaseDatabase.getInstance()
        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance?.getReference("ChatGroup")
        var mDatabase = FirebaseDatabase.getInstance().getReference()
        mDatabase.child("ChatGroup").child(chatKey!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(GroupModel::class.java)
                    if(user != null) {

                        // create a dynamic list for shoewing group
                        /*---------------------------------------------*/
                        val mList = UserModel()
                        mList?.setFcmToken(user?.getFmcToken())
                        mList?.setIsTyping(user?.getIsTyping())
                        mList?.setLastSeen(user?.getLastSeen())
                        mList?.setNotificationStatus("1")
                        mList?.setOnChatScreen(false)
                        //mList?.setStatus(user?.getStatus())
                        mList?.setUserEmail("")
                        mList?.setUserImg(user?.getGroupImg())
                        mList?.setUserName(user?.getGroupName())
                        mList?.setMsgStatus("")
                        mList?.setMembers(user?.getMembers())
                        /*---------------------------------------------*/

                        val list = UserModel()
                        list?.setUserMadelData(mList)
                        list?.setChatKey(chatKey)
                        list?.setWithChatUserId("")
                        list?.setReadable(unreadCount)
                        list?.setMsgStatus(mStatus)

                        chatUserList.add(list)

                        if (chatUserList.size == chatWithUserList.size) {
                            comman?.value = chatUserList //list
                        }
                    }
                }
            })
    }
}