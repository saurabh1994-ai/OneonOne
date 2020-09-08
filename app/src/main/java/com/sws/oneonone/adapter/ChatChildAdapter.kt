package com.sws.oneonone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.sws.oneonone.R
import com.sws.oneonone.databinding.LayoutChatChildBinding
import com.sws.oneonone.firebaseModel.UserModel
import com.sws.oneonone.fragment.ChatMessageFragment
import com.sws.oneonone.fragment.NewGroupFragment
import com.sws.oneonone.model.ChildModel
import com.sws.oneonone.model.SignUpResultModel
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.ShowingImage
import com.sws.oneonone.viewModel.NewGroupChatVM
import java.util.*

class ChatChildAdapter(activity: BaseActivity, var childList: ArrayList<ChildModel>,  var check: Boolean, newGroupChatVM: NewGroupChatVM?):
    RecyclerView.Adapter<ChatChildAdapter.ViewHolder>(){
    private var newGroupVM: NewGroupChatVM? = newGroupChatVM
    var selectedList: ArrayList<ChildModel> = ArrayList<ChildModel>()
    private var activity: BaseActivity? = activity

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val layoutChatChildBinding = DataBindingUtil.inflate<LayoutChatChildBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.layout_chat_child, viewGroup, false
        )
        return ViewHolder(layoutChatChildBinding)
    }

    override fun onBindViewHolder(ViewHolder: ViewHolder, i: Int) {
        // ViewHolder.layoutChatChildBinding!!.tvTypes?.text = "@UmeshSir"
        val model: ChildModel? = childList?.get(i)
        if(!model?.getImage().isNullOrEmpty()){
            ShowingImage?.showImage(activity!!, model?.getImage(), ViewHolder?.layoutChatChildBinding!!.profileImage)
        }
        if (check){
            ViewHolder.layoutChatChildBinding!!.cbAction?.visibility = View.VISIBLE
        }

        ViewHolder.layoutChatChildBinding!!.cbAction?.setOnClickListener {
            if (ViewHolder.layoutChatChildBinding!!.cbAction?.isChecked) {
                newGroupVM?.selectedItemFunction(childList?.get(i), true)
            } else {
                newGroupVM?.selectedItemFunction(childList?.get(i), false)
            }
        }


        ViewHolder.layoutChatChildBinding!!.newChat?.setOnClickListener {
            if(!check) {
                val model1: ChildModel? = childList?.get(i)
                val userModel: UserModel? = UserModel()
                userModel?.setWithChatUserId(childList?.get(i)?.getId())
                userModel?.setUserImg(childList?.get(i)?.getImage())
                userModel?.setUserName(childList?.get(i)?.getFullName())
                //userModel?.setNotificationStatus()
                val fragment = ChatMessageFragment()
                fragment?.setChatWithUserId("", childList?.get(i)?.getId(), userModel)
                activity!!.replaceFragment(fragment)
            }
        }

        ViewHolder.layoutChatChildBinding!!.childModel = model

    }

    override fun getItemCount(): Int {
        return childList?.size
    }

    inner class ViewHolder(val layoutChatChildBinding: LayoutChatChildBinding) :
        RecyclerView.ViewHolder(layoutChatChildBinding.root)


}