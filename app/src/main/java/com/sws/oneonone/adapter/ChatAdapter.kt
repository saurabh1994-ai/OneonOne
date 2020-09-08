package com.sws.oneonone.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseError
import com.google.firebase.database.*
import com.sws.oneonone.R
import com.sws.oneonone.databinding.LayoutChatUserBinding
import com.sws.oneonone.dialog.ProfileBottomDialog
import com.sws.oneonone.firebaseModel.ChatUserModel
import com.sws.oneonone.firebaseModel.UserModel
import com.sws.oneonone.fragment.ChatMessageFragment
import com.sws.oneonone.model.UserDataModel
import com.sws.oneonone.util.AppStaticData
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.ShowingImage
import com.sws.oneonone.util.Utils
import java.util.*

class ChatAdapter(activity: BaseActivity, var userList: ArrayList<UserModel>):
    RecyclerView.Adapter<ChatAdapter.ViewHolder>(), Filterable {
    private var filter = CustomFilter()
    private var activity: BaseActivity? = activity
    private var mFirebaseDatabase: DatabaseReference? = null
    private var mFirebaseInstance: FirebaseDatabase? = null

    var OrginalUserList: ArrayList<UserModel>? = userList
    var SuggestionUserList: ArrayList<UserModel>? = ArrayList<UserModel>()

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val yourChatUserBinding = DataBindingUtil.inflate<LayoutChatUserBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.layout_chat_user, viewGroup, false
        )
        initializeFirebase()
        return ViewHolder(yourChatUserBinding)
    }

    override fun onBindViewHolder(ViewHolder: ViewHolder, i: Int) {
        val model: UserModel? = userList?.get(i)?.getUserMadelData()
        if(!model?.getUserImg().isNullOrEmpty()){
            ShowingImage?.showImage(activity!!, model?.getUserImg(), ViewHolder.layoutChatUserBinding!!.profileImage)
        } else {
            ViewHolder.layoutChatUserBinding!!.profileImage.setImageResource(R.drawable.avtar_icon)
        }
        Log.e("getMsgStatus", "status : "+ model?.getMsgStatus())

        if (!userList?.get(i).getChatKey().isNullOrEmpty()) {
            chatMessageStatus(userList?.get(i).getChatKey(), ViewHolder.layoutChatUserBinding!!.tvTypes)
            val yourArray: List<String> = userList?.get(i).getChatKey()!!.split("_")
            if ( yourArray.size > 2){
                getGroupUnread(userList?.get(i).getChatKey()!!, ViewHolder!!.layoutChatUserBinding?.tvUnReadMessage)
            } else {
                getSingleUserUnread(userList?.get(i).getChatKey()!!, ViewHolder!!.layoutChatUserBinding?.tvUnReadMessage)
            }
        }

        ViewHolder.layoutChatUserBinding!!.modelUser = model

        ViewHolder.layoutChatUserBinding?.profileImage?.setOnClickListener{
            val yourArray: List<String> = userList?.get(i).getChatKey()!!.split("_")
            if ( yourArray.size > 2){ } else {
                val chatWithId: String? = userList?.get(i)?.getWithChatUserId()
                val videoBottomSheetDialog =
                    ProfileBottomDialog.newInstance(activity!!, chatWithId, null, null)
                videoBottomSheetDialog.show(
                    activity!!.supportFragmentManager,
                    ProfileBottomDialog().TAG
                )
            }
        }
        ViewHolder.layoutChatUserBinding?.layoutMain?.setOnClickListener {
            val model1: UserModel? = userList?.get(i)?.getUserMadelData()
            val fragment = ChatMessageFragment()
            fragment?.setChatWithUserId(userList?.get(i)?.getChatKey(), userList?.get(i)?.getWithChatUserId(), model1)
            activity!!.replaceFragment(fragment)
        }
    }

    override fun getItemCount(): Int {
        return userList?.size
    }

    inner class ViewHolder(val layoutChatUserBinding: LayoutChatUserBinding) :
        RecyclerView.ViewHolder(layoutChatUserBinding.root)


    override fun getFilter(): Filter {
        return filter
    }

    private inner class CustomFilter: Filter() {
        protected override fun performFiltering(constraint:CharSequence): FilterResults {
            SuggestionUserList?.clear()
            // Check if the Original List and Constraint aren't null.
            if (OrginalUserList != null && constraint != null) {
                for (i in 0 until OrginalUserList!!.size) {
                    val list: UserModel? = OrginalUserList?.get(i)?.getUserMadelData()
                    if (list?.getUserName()?.toLowerCase()?.contains(constraint)!!){//!! && nameOrginal?.get(i)?.toLowerCase()?.contains(constraint)!!) {
                        SuggestionUserList?.add(OrginalUserList?.get(i)!!)
                    }
                }
            }
            // Create new Filter Results and return this to publishResults;
            val results = FilterResults()
            results.values = SuggestionUserList
            results.count = SuggestionUserList!!.size
            return results
        }
        protected override fun publishResults(constraint:CharSequence, results: FilterResults) {
            userList = SuggestionUserList!!
            notifyDataSetChanged()
        }
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

    // Group unread message
    fun getGroupUnread(chatKey: String, textView: TextView){
        mFirebaseInstance = FirebaseDatabase.getInstance()
        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance?.getReference("Chat")
        var mDatabase = FirebaseDatabase.getInstance().getReference()

        mDatabase.child("Chat").child(chatKey).orderByChild("member/"+AppStaticData().getModel()?.result?.id!!+"/read").equalTo("0")
            .addValueEventListener(object :
                ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var count: Long? = 0
                    textView.visibility = View.GONE
                    if (dataSnapshot != null) {
                        count = dataSnapshot.childrenCount
                        if(count > 0) {
                            textView.visibility = View.VISIBLE
                            textView.text = count?.toString()
                        }
                    }
                }

            })
    }

    // get Single user chat unread message count
    fun getSingleUserUnread(chatKey: String, textView: TextView){
        mFirebaseInstance = FirebaseDatabase.getInstance()
        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance?.getReference("Chat")
        var mDatabase = FirebaseDatabase.getInstance().getReference()

        mDatabase.child("Chat").child(chatKey)
            .orderByChild("read").equalTo("0")
            .addValueEventListener(object :
                ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var count: Int? = 0
                    if (dataSnapshot != null) {
                        for (childSnapshot in dataSnapshot.children) {
                            val mModel: ChatUserModel? = childSnapshot.getValue(ChatUserModel::class.java)
                            if (mModel?.getRecieverId().equals(AppStaticData().getModel()?.result?.id)) {
                                // Here is your desired location
                                count = count?.plus(1)
                            }
                        }
                        // check empty or not
                        if (count == 0) {
                            textView?.visibility = View.GONE
                        } else {
                            textView?.text = count?.toString()
                            textView?.visibility = View.VISIBLE
                        }
                    }
                }

            })

    }

    // user message status
    fun chatMessageStatus(chatKey: String?, textView: TextView){
        mFirebaseInstance = FirebaseDatabase.getInstance()
        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance?.getReference("Chat")

        var mDatabase = FirebaseDatabase.getInstance().getReference()
        mDatabase.child("Chat").child(chatKey!!)?.orderByKey()?.limitToLast(1)
            .addValueEventListener(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (childDataSnapshot in dataSnapshot.getChildren()) {
                        val user = childDataSnapshot.getValue(ChatUserModel::class.java)
                        if (user != null) {
                            val status_message = user?.getMsgStatus()
                            if (!status_message.isNullOrEmpty()) {
                                when (status_message) {
                                    "sent" -> { // tap to chat //"unread"
                                        textView?.text = "Send"
                                        //ViewHolder.layoutChatUserBinding!!.username?.setTextColor(activity?.getResources()!!.getColor(R.color.black));
                                        textView?.setCompoundDrawablesWithIntrinsicBounds(
                                            R.drawable.tap_chat,
                                            0,
                                            0,
                                            0
                                        )
                                    }
                                    "delivered" -> {
                                        textView?.text = "Delivered"
                                        //ViewHolder.layoutChatUserBinding!!.username?.setTextColor(activity?.getResources()!!.getColor(R.color.black));
                                        textView?.setCompoundDrawablesWithIntrinsicBounds(
                                            R.drawable.deliver_icon,
                                            0,
                                            0,
                                            0
                                        )
                                    }
                                    "read" -> { //
                                        textView?.text = "Read"
                                        //ViewHolder.layoutChatUserBinding!!.username?.setTextColor(activity?.getResources()!!.getColor(R.color.black));
                                        textView?.setCompoundDrawablesWithIntrinsicBounds(
                                            R.drawable.opend_chat,
                                            0,
                                            0,
                                            0
                                        )
                                    }
                                    "faild" -> { //
                                        textView?.text = "Faild - Tap to retry"
                                        //ViewHolder.layoutChatUserBinding!!.username?.setTextColor(activity?.getResources()!!.getColor(R.color.black));
                                        textView?.setCompoundDrawablesWithIntrinsicBounds(
                                            R.drawable.failed_icon,
                                            0,
                                            0,
                                            0
                                        )
                                    }
                                }
                            } else {
                                textView?.text = "Tap to chat"
                                //ViewHolder.layoutChatUserBinding!!.username?.setTextColor( activity?.getResources()!!.getColor(R.color.black) );
                                textView?.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.tap_chat,
                                    0,
                                    0,
                                    0
                                )
                            }

                        }
                    }
                }
            })
    }

}