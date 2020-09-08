package com.sws.oneonone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.sws.oneonone.R
import com.sws.oneonone.databinding.LayoutChatChildBinding
import com.sws.oneonone.model.ChildModel
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.ShowingImage
import java.util.*

class CheckChatChildAdapter(activity: BaseActivity, var mList: ArrayList<ChildModel>,  val visi : Boolean, check : Boolean):
    RecyclerView.Adapter<CheckChatChildAdapter.ViewHolder>(){

    private var check: Boolean = check
    private var list: ArrayList<String>? = null
    private var activity: BaseActivity? = activity

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val layoutChatChildBinding = DataBindingUtil.inflate<LayoutChatChildBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.layout_chat_child, viewGroup, false
        )
        return ViewHolder(layoutChatChildBinding)
    }

    override fun onBindViewHolder(ViewHolder: ViewHolder, i: Int) {
        val model: ChildModel = mList?.get(i)
        if(!model?.getImage().isNullOrEmpty()){
            ShowingImage?.showImage(activity!!, model?.getImage(), ViewHolder.layoutChatChildBinding!!.profileImage)
        }
        if (visi){
            ViewHolder.layoutChatChildBinding!!.cbAction?.visibility = View.VISIBLE
        } else {
            ViewHolder.layoutChatChildBinding!!.cbAction?.visibility = View.GONE
        }

        ViewHolder.layoutChatChildBinding!!.childModel = model

    }

    override fun getItemCount(): Int {
        return mList?.size
    }


    inner class ViewHolder(val layoutChatChildBinding: LayoutChatChildBinding) :
        RecyclerView.ViewHolder(layoutChatChildBinding.root)


}