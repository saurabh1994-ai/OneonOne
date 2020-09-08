package com.sws.oneonone.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.adapter.ChatHeaderAdapter
import com.sws.oneonone.adapter.CheckChatHeaderAdapter
import com.sws.oneonone.adapter.SelectedGroupAdapter
import com.sws.oneonone.databinding.NewGroupActivityBinding
import com.sws.oneonone.model.AllMessageUserModel
import com.sws.oneonone.model.ChildModel
import com.sws.oneonone.model.SignUpResultModel
import com.sws.oneonone.model.ToolbarModel
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.util.*
import com.sws.oneonone.viewModel.NewGroupChatVM
import com.sws.oneonone.viewModel.ToolbarVM
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class NewGroupFragment: BaseFragment() {

    private var binding: NewGroupActivityBinding? = null
    var service: ApiClient? = ApiClient()
    // selected item array list
    var selectedList: ArrayList<ChildModel> =  ArrayList<ChildModel>()

    var headerList: ArrayList<SignUpResultModel> = ArrayList<SignUpResultModel>()
    var childList: ArrayList<ChildModel>? = null
    // Toolbar View Model
    var toolbarVM: ToolbarVM? = null
    var newGroupChatVM: NewGroupChatVM? = null
    //Adapter
    var adapter: CheckChatHeaderAdapter? = null
    var firstChar: String?= "A"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarVM = ViewModelProviders.of(this).get(ToolbarVM::class.java)
        newGroupChatVM = ViewModelProviders.of(this).get(NewGroupChatVM::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        NotificationBarColor?.WhiteNotificationBar(activity)
        binding = NewGroupActivityBinding.bind(inflater.inflate(R.layout.new_group_activity, container, false))
        binding!!.setLifecycleOwner(this)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Tool bar model
        val toolbarModel: ToolbarModel = ToolbarModel(View.VISIBLE,"New Group", View.GONE,View.VISIBLE,View.VISIBLE,View.VISIBLE, 1, "NEXT","",View.GONE)
        binding!!.layoutToolbar.toolbarModel = toolbarModel
        binding!!.layoutToolbar.lifecycleOwner = this
        binding!!.lifecycleOwner = this
        // ToolBar ViewModel
        toolbarVM?.activity = activity
        binding!!.layoutToolbar.toolbarVM = toolbarVM

        newGroupChatVM?.activity = activity

        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding!!.rvNewChat.layoutManager = layoutManager
        //     binding!!.rvNewChat.adapter = CheckChatHeaderAdapter(activity, headerList)

        userChatListData()
        initView()

        binding!!.etSearch?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    adapter?.filter?.filter(s)
                }
            }
        })
    }


    private fun initView() {
        // check has Observers
        newGroupChatVM?.comman?.observe(this, object : Observer<ArrayList<ChildModel>> {
            override fun onChanged(@Nullable childList: ArrayList<ChildModel>) {
                selectedList = childList
                val layoutManager1 = LinearLayoutManager(activity)
                layoutManager1.orientation = LinearLayoutManager.HORIZONTAL
                binding!!.rvChallenges.layoutManager = layoutManager1
                binding!!.rvChallenges.adapter = SelectedGroupAdapter(activity, selectedList, newGroupChatVM, adapter)

            }
        })

        // toolbar view Model
        if (!toolbarVM?.comman?.hasObservers()!!) {
            toolbarVM?.comman?.observe(this, object : Observer<String> {
                override fun onChanged(@Nullable str: String) {
                    when (str) {
                        "onBack" ->{ onBackPressed()}
                        "next" -> {
                            if (selectedList != null) {
                                if (selectedList?.size != 0) {
                                    var fragment: NewGroupNameFragment = NewGroupNameFragment()
                                    fragment.setSelectedList(selectedList)
                                    activity?.onBackPressed()
                                    activity.replaceFragment(fragment)
                                }
                            }
                        }
                    }
                }
            })
        }
        //if(newGroupChatVM?.comman?.hasObservers()!!){

    }


    fun userChatListData() {
        headerList?.clear()
        try {
            val rootObject = JsonObject()
            try {
                rootObject.addProperty("userId",  AppStaticData()?.getModel()?.result?.id)
                //   rootObject.addProperty("pageNumber",  pageNo)
            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            //var token = pref?.getStringData("token")
            val call = apiService.userMessageList(AppStaticData().APIToken(), rootObject).also {
                it.enqueue(object : Callback<AllMessageUserModel> {
                    override fun onResponse(call: Call<AllMessageUserModel>, response: Response<AllMessageUserModel>) {
                        if (response.code() == 200) {
                            try {
                                // Loader?.hideLoader(activity!!)
                                val mUserModel = response.body()!!
                                val checkResponse =  AndroidUtilities().apisResponse(activity!!,mUserModel.code!!,mUserModel.message)
                                if(checkResponse) {
                                    if (mUserModel.code == 200) {
                                        if (!mUserModel?.followings.isNullOrEmpty()) {
                                            for (i in 0..(mUserModel?.followings?.size!! - 1)) {
                                                mUserModel?.followings!![i].setIsSelected(false)
                                                headerList?.add(mUserModel?.followings!![i])
                                                /*var name: String? =
                                                    mUserModel?.followings!![i].fullName
                                                if (!name.isNullOrEmpty()) {
                                                    val first = name[0].toString()
                                                    Log.e("qwerty123", first)

                                                    if (i == 0) {
                                                        childList = ArrayList<ChildModel>()
                                                        firstChar = first
                                                    }
                                                    val child = ChildModel()
                                                    if (first.toString().capitalize()
                                                            .equals(firstChar?.capitalize())
                                                    ) {
                                                        child?.setId(mUserModel?.followings!![i]?.id)
                                                        child?.setFullName(mUserModel?.followings!![i]?.fullName)
                                                        child?.setImage(mUserModel?.followings!![i]?.profileImg)
                                                        child?.setUserName(mUserModel?.followings!![i]?.username)
                                                        childList?.add(child)

                                                    } else {
                                                        val header = SignUpResultModel()
                                                        header?.setChildList(childList)
                                                        header?.setString(firstChar)
                                                        headerList?.add(header)

                                                        //for new one
                                                        //childList?.clear()
                                                        childList = ArrayList<ChildModel>()
                                                        child?.setId(mUserModel?.followings!![i]?.id)
                                                        child?.setFullName(mUserModel?.followings!![i]?.fullName)
                                                        child?.setImage(mUserModel?.followings!![i]?.profileImg)
                                                        child?.setUserName(mUserModel?.followings!![i]?.username)
                                                        childList?.add(child)
                                                        firstChar = first
                                                    }
                                                }*/
                                            }
                                            adapter = CheckChatHeaderAdapter(
                                                activity,
                                                headerList,
                                                newGroupChatVM
                                            )
                                            binding!!.rvNewChat.adapter = adapter
                                        }
                                    }
                                }
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        }
                    }
                    override fun onFailure(call: Call<AllMessageUserModel>, t: Throwable) {
                        Log.e("responseError", t.message)
                        Utils.showToast(activity!!, "Have some problem...", false)
                        // Loader?.hideLoader(activity!!)
                    }
                })
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}