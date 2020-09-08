package com.sws.oneonone.adapter

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
import com.sws.oneonone.databinding.GroupViewAdapterBinding
import com.sws.oneonone.firebaseModel.ChatUserModel
import com.sws.oneonone.firebaseModel.UserModel
import com.sws.oneonone.fragment.ChatMessageFragment
import com.sws.oneonone.model.UserDataModel
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.ShowingImage
import com.sws.oneonone.util.Utils
import java.util.*

class GroupViewAdapter(activity: BaseActivity, var userList: ArrayList<UserModel>):
    RecyclerView.Adapter<GroupViewAdapter.ViewHolder>() {
    private var activity: BaseActivity? = activity
    private var mFirebaseDatabase: DatabaseReference? = null
    private var mFirebaseInstance: FirebaseDatabase? = null


    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val yourBinding = DataBindingUtil.inflate<GroupViewAdapterBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.group_view_adapter, viewGroup, false
        )
        initializeFirebase()
        return ViewHolder(yourBinding)
    }

    override fun onBindViewHolder(ViewHolder: ViewHolder, i: Int) {
        val model: UserModel? = userList?.get(i)

        if(!model?.getUserImg().isNullOrEmpty()){
            ShowingImage?.showImage(activity!!, model?.getUserImg(), ViewHolder.yourBinding!!.profileImage)
        }

        if(!model?.getChatKey().isNullOrEmpty()) {
            chatMessageStatus(model?.getChatKey(), ViewHolder.yourBinding!!.tvLastSeen)
        }

        ViewHolder.yourBinding!!.modelUser = model

    }

    override fun getItemCount(): Int {
        return userList?.size
    }


    inner class ViewHolder(val yourBinding: GroupViewAdapterBinding) :
        RecyclerView.ViewHolder(yourBinding.root)


    //  initialize firebase database
    fun initializeFirebase() {
        mFirebaseInstance = FirebaseDatabase.getInstance()
        mFirebaseDatabase = mFirebaseInstance?.getReference("Users")
        // check internet connection
        if (Utils.isNetworkAvailable(activity!!)) {
            val scoresRef = FirebaseDatabase.getInstance().getReference("Users")
            scoresRef.keepSynced(false)
        } else {
            val scoresRef = FirebaseDatabase.getInstance().getReference("Users")
            scoresRef.keepSynced(true)
        }
    }

    fun chatMessageStatus(chatKey: String?, textView: TextView){
        mFirebaseInstance = FirebaseDatabase.getInstance()
        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance?.getReference("Users")

        var mDatabase = FirebaseDatabase.getInstance().getReference()
        mDatabase.child("Users").child(chatKey!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(UserModel::class.java)
                    //val user = childDataSnapshot.getValue(ChatM::class.java)
                    if (user != null) {
                        // val last_seen = user?.getLastSeen()
                        val lastSeen: Long? = Utils().getMilliFromDate(user?.getLastSeen())
                        textView?.text = Utils().convertDate(lastSeen)

                    }
                }
                fun onCancelled(firebaseError: FirebaseError) {
                    //Error
                }
                //}
            })
    }

}