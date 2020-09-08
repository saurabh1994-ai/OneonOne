package com.sws.oneonone.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.sws.oneonone.R
import com.sws.oneonone.adapter.ChatAdapter
import com.sws.oneonone.databinding.FragmentCallsBinding
import com.sws.oneonone.firebaseModel.UserModel
import com.sws.oneonone.model.SignUpModel
import com.sws.oneonone.util.AppStaticData
import com.sws.oneonone.util.BaseFragment
import com.sws.oneonone.util.ShowingImage
import com.sws.oneonone.viewModel.ChatUserVM
import kotlinx.android.synthetic.main.layout_toolbar.*

class CallFragment: BaseFragment() {
    private var binding: FragmentCallsBinding? = null
    var chatUserVM: ChatUserVM? = null
    var adapter: ChatAdapter? = null
    var mergedSet: Set<UserModel>? = null
    var chatUserList: ArrayList<UserModel>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        chatUserVM = ViewModelProviders.of(this).get(ChatUserVM::class.java)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity?.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        activity?.getWindow().setStatusBarColor(ContextCompat.getColor(activity,R.color.white));// set status background white
        binding = FragmentCallsBinding.bind(inflater.inflate(R.layout.fragment_calls, container, false))
        binding!!.setLifecycleOwner(this)

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //get app data
        val model: SignUpModel? = AppStaticData().getModel()

        //set Image in imageView
        ShowingImage.showImage(activity, model?.result?.profileImg, binding!!.userProfile)

        binding!!.lifecycleOwner = activity
        // Chat User ViewModel
        chatUserVM?.activity = activity
        chatUserVM?.fragment = this

        binding!!.chatUserVM = chatUserVM
        binding!!.chatUserVM?.showingList()

        chatUserList = ArrayList<UserModel>()
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding!!.rvChallenges.layoutManager = layoutManager
        adapter = ChatAdapter(activity, chatUserList!!)
        binding!!.rvChallenges.adapter = adapter
        initView()
    }


    private fun initView() {
        // check has Observers
        if (!chatUserVM?.comman?.hasObservers()!!) {
            chatUserVM?.comman?.observe(this, object : Observer<ArrayList<UserModel>> {
                override fun onChanged(@Nullable modelList: ArrayList<UserModel>) {
                   // chatUserList?.clear()
                    if (adapter?.itemCount != 0) {
                        for (i in 0..(modelList?.size!! - 1)) {
                            if (chatUserList?.size!! <= adapter?.itemCount!!) {
                                chatUserList?.removeAt(i)
                                chatUserList?.add(i, modelList?.get(i))
                              //  chatUserList?.add(modelList?.get(i))
                                adapter?.notifyItemChanged(i, modelList?.get(i))
                            } else {
                                chatUserList?.add(modelList?.get(i))
                                adapter?.notifyDataSetChanged()
                            }
                        }
                    } else {
                        chatUserList?.addAll(modelList)
                        adapter?.notifyDataSetChanged()
                    }

                   // chatUserList?.add(modelList)
                    //adapter?.notifyDataSetChanged()

                    if (chatUserList.isNullOrEmpty()){
                        binding!!.tvEmptyText.visibility = View.VISIBLE
                        //binding!!.rvChallenges.visibility = View.GONE
                    } else {
                        binding!!.tvEmptyText.visibility = View.GONE
                       // binding!!.rvChallenges.visibility = View.VISIBLE
                    }
                }
            })
        }


        if (!chatUserVM?.filterData?.hasObservers()!!) {
            chatUserVM?.filterData?.observe(this, object : Observer<String> {
                override fun onChanged(@Nullable filterText: String?) {
                    adapter?.filter?.filter(filterText)
                }
            })
        }
    }

    fun clearList(){
        chatUserList?.clear()
    }
}