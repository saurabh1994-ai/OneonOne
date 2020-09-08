package com.sws.oneonone.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.adapter.ChatChildAdapter
import com.sws.oneonone.adapter.ChatHeaderAdapter
import com.sws.oneonone.databinding.NewChatActivityBinding
import com.sws.oneonone.model.*
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.util.*
import com.sws.oneonone.viewModel.NewChatVM
import com.sws.oneonone.viewModel.ToolbarVM
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class NewChatFragment: BaseFragment() {
    private var binding: NewChatActivityBinding? = null
    var service: ApiClient? = ApiClient()
    var headerList: ArrayList<SignUpResultModel> = ArrayList<SignUpResultModel>()

    var toolbarVM: ToolbarVM? = null
    var newChatVM: NewChatVM? = null
    var adapter: ChatHeaderAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // NotificationBar change backgraound/text color
        NotificationBarColor?.WhiteNotificationBar(activity)
        toolbarVM = ViewModelProviders.of(this).get(ToolbarVM::class.java)
        newChatVM = ViewModelProviders.of(this).get(NewChatVM::class.java)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = NewChatActivityBinding.bind(inflater.inflate(R.layout.new_chat_activity, container, false))
        binding!!.setLifecycleOwner(this)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Tool bar model
        val toolbarModel: ToolbarModel = ToolbarModel(View.VISIBLE,"New Chat", View.GONE,View.VISIBLE,View.VISIBLE,View.GONE, 1, "","",View.GONE)
        binding!!.layoutToolbar.toolbarModel = toolbarModel
        binding!!.layoutToolbar.lifecycleOwner = this

        // ToolBar View Model
        toolbarVM?.activity = activity
        binding!!.layoutToolbar.toolbarVM = toolbarVM
        //fragment view Model
        binding!!.newChatVM = newChatVM
        binding!!.newChatVM?.activity = activity

        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL

        binding!!.rvNewChat.layoutManager = layoutManager
        userChatListData()
        initView()

        binding!!.etSearch?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    adapter?.filter?.filter(s)
                }
            }
        })
    }

    private fun initView() {
        // check has Observers
        if (!toolbarVM?.comman?.hasObservers()!!) {
            toolbarVM?.comman?.observe(this, object : Observer<String> {
                override fun onChanged(@Nullable str: String) {
                    when (str) {
                        "onBack" -> onBackPressed()
                    }
                }
            })
        }
    }

    fun userChatListData() {
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
                                if(checkResponse){
                                    if (mUserModel.code == 200) {
                                        if (!mUserModel?.followings.isNullOrEmpty()) {
                                            for (i in 0..(mUserModel?.followings?.size!! - 1)) {
                                                mUserModel?.followings!![i].setIsSelected(false)
                                                headerList?.add(mUserModel?.followings!![i])
                                            }
                                            adapter = ChatHeaderAdapter(activity, headerList)
                                            binding!!.rvNewChat.adapter = adapter
                                            if (headerList.isNullOrEmpty()){
                                                binding!!.tvEmptyText?.visibility = View.VISIBLE
                                                binding!!.rvNewChat?.visibility = View.GONE
                                                binding!!.createGroup?.visibility = View.GONE
                                            } else {
                                                binding!!.tvEmptyText?.visibility = View.GONE
                                                binding!!.rvNewChat?.visibility = View.VISIBLE
                                                binding!!.createGroup?.visibility = View.VISIBLE
                                            }
                                        }
                                    }}
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

    override fun onResume() {
        super.onResume()
        NotificationBarColor?.WhiteNotificationBar(activity)
    }
}