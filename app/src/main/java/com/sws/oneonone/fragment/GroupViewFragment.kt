package com.sws.oneonone.fragment

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.google.firebase.FirebaseError
import com.google.firebase.database.*
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.adapter.ChatAdapter
import com.sws.oneonone.adapter.GroupViewAdapter
import com.sws.oneonone.adapter.ProfileAllChallengersAdapter
import com.sws.oneonone.adapter.ViewPagerAdapterProfile
import com.sws.oneonone.databinding.FragmentGroupViewBinding
import com.sws.oneonone.databinding.ProfileActivityBinding
import com.sws.oneonone.firebaseModel.UserModel
import com.sws.oneonone.model.ProfileChallengeModel
import com.sws.oneonone.model.ProfileChallengeResult
import com.sws.oneonone.model.SignUpModel
import com.sws.oneonone.model.SignUpResultModel
import com.sws.oneonone.util.*
import com.sws.oneonone.viewModel.ProfileVM
import kotlinx.android.synthetic.main.group_view_contain.view.*


class GroupViewFragment: BaseFragment() {
    private var binding: FragmentGroupViewBinding? = null
    private var chatWith: String? = null
    private var chatKey: String? = null
    private var userModel: UserModel? = null

    private var mFirebaseDatabase: DatabaseReference? = null
    private var mFirebaseInstance: FirebaseDatabase? = null
    var userList: ArrayList<UserModel>? = null
    var adapter: GroupViewAdapter? = null

    fun setGroupDetails(@Nullable chatKey: String?, chatWith: String?, userModel: UserModel?) {
        this.chatKey = chatKey
        this.chatWith = chatWith
        this.userModel = userModel
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGroupViewBinding.bind(
            inflater.inflate(R.layout.fragment_group_view, container, false))
        binding!!.tvLastSeen.visibility  = View.GONE

        return binding!!.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding!!.back?.setOnClickListener {
            onBackPressed()
        }
        if(!userModel?.getUserImg().isNullOrEmpty()){
            ShowingImage?.showBannerImage(activity!!, userModel?.getUserImg(), binding!!.ivGroup)
        }
        binding!!.tvUserName?.text = userModel?.getUserName()
        // Last seen
        val lastSeen: Long? = Utils().getMilliFromDate(userModel?.getLastSeen())
        binding!!.tvLastSeen?.text = Utils().convertDate(lastSeen)

        userList = ArrayList<UserModel>()
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding!!.contains?.rvGroupUserList.layoutManager = layoutManager
        binding!!.contains?.rvGroupUserList?.isNestedScrollingEnabled = false
        adapter = GroupViewAdapter(activity, userList!!)
        binding!!.contains?.rvGroupUserList.adapter = adapter

        getDetails(userModel?.getMembers())

    }

    fun getDetails(list: ArrayList<String>?){
        for(i in 0..(list?.size!!-1)){
            // get user List data
            mFirebaseInstance = FirebaseDatabase.getInstance()
            // get reference to 'users' node
            mFirebaseDatabase = mFirebaseInstance?.getReference("Users")
            var mDatabase = FirebaseDatabase.getInstance().getReference()
            mDatabase.child("Users").child(list?.get(i))
                .addValueEventListener(object: ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val user = dataSnapshot.getValue(UserModel::class.java)
                        if(user != null) {
                            user?.setChatKey(list?.get(i))
                            userList?.add(user)
                            adapter?.notifyDataSetChanged()

                            /*val list = UserModel()
                            list?.setUserMadelData(user)
                            list?.setChatKey(chatKey)
                            list?.setWithChatUserId(userId)
                            list?.setReadable(unreadCount)
                            list?.setMsgStatus(mStatus)
                            if(!idList.contains(chatKey)) {
                                idList?.add(chatKey!!)
                                comman?.value = list
                            }*/

                        }
                    }
                    fun onCancelled(firebaseError: FirebaseError) {
                        //Error
                    }
                })

        }

    }


}
/*
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.google.firebase.FirebaseError
import com.google.firebase.database.*
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.adapter.ChatAdapter
import com.sws.oneonone.adapter.GroupViewAdapter
import com.sws.oneonone.adapter.ProfileAllChallengersAdapter
import com.sws.oneonone.adapter.ViewPagerAdapterProfile
import com.sws.oneonone.databinding.FragmentGroupViewBinding
import com.sws.oneonone.databinding.ProfileActivityBinding
import com.sws.oneonone.firebaseModel.UserModel
import com.sws.oneonone.model.ProfileChallengeModel
import com.sws.oneonone.model.ProfileChallengeResult
import com.sws.oneonone.model.SignUpModel
import com.sws.oneonone.model.SignUpResultModel
import com.sws.oneonone.util.*
import com.sws.oneonone.viewModel.ProfileVM
import kotlinx.android.synthetic.main.group_view_contain.view.*
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tourhq.sws.com.tourhq.retrofit.ApiClient
import tourhq.sws.com.tourhq.retrofit.ApiInterface


class GroupViewFragment: BaseFragment() {
    private var binding: FragmentGroupViewBinding? = null
    private var chatWith: String? = null
    private var chatKey: String? = null
    private var userModel: UserModel? = null


    private var mFirebaseDatabase: DatabaseReference? = null
    private var mFirebaseInstance: FirebaseDatabase? = null
    var userList: ArrayList<UserModel>? = null
    var adapter: GroupViewAdapter? = null

    fun setGroupDetails(@Nullable chatKey: String?, chatWith: String?, userModel: UserModel?) {
        this.chatKey = chatKey
        this.chatWith = chatWith
        this.userModel = userModel
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGroupViewBinding.bind(
            inflater.inflate(R.layout.fragment_group_view, container, false))


        return binding!!.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding!!.back?.setOnClickListener {
            activity?.finish()
        }
        if(!userModel?.getUserImg().isNullOrEmpty()){
            ShowingImage?.showBannerImage(activity!!, userModel?.getUserImg(), binding!!.ivGroup)
        }
        binding!!.tvUserName?.text = userModel?.getUserName()
        // Last seen
        val lastSeen: Long? = Utils().getMilliFromDate(userModel?.getLastSeen())
        binding!!.tvLastSeen?.text = Utils().convertDate(lastSeen)

        userList = ArrayList<UserModel>()
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding!!.contains?.rvGroupUserList.layoutManager = layoutManager
        binding!!.contains?.rvGroupUserList?.isNestedScrollingEnabled = false
        adapter = GroupViewAdapter(activity, userList!!)
        binding!!.contains?.rvGroupUserList.adapter = adapter

        getDetails(userModel?.getMembers())

    }

    fun getDetails(list: ArrayList<String>?){
        for(i in 0..(list?.size!!-1)){
            // get user List data
                mFirebaseInstance = FirebaseDatabase.getInstance()
                // get reference to 'users' node
                mFirebaseDatabase = mFirebaseInstance?.getReference("Users")
                var mDatabase = FirebaseDatabase.getInstance().getReference()
                mDatabase.child("Users").child(list?.get(i))
                    .addValueEventListener(object: ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                        }
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val user = dataSnapshot.getValue(UserModel::class.java)
                            if(user != null) {
                                userList?.add(user)
                                adapter?.notifyDataSetChanged()

                                */
/*val list = UserModel()
                                list?.setUserMadelData(user)
                                list?.setChatKey(chatKey)
                                list?.setWithChatUserId(userId)
                                list?.setReadable(unreadCount)
                                list?.setMsgStatus(mStatus)
                                if(!idList.contains(chatKey)) {
                                    idList?.add(chatKey!!)
                                    comman?.value = list
                                }*//*


                            }
                        }
                        fun onCancelled(firebaseError: FirebaseError) {
                            //Error
                        }
                    })

        }

    }

}*/