package com.sws.oneonone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.sws.oneonone.R
import com.sws.oneonone.databinding.LayoutSelectedFollowingsBinding
import com.sws.oneonone.databinding.LayoutSelectedGroupBinding
import com.sws.oneonone.databinding.LayoutYourChallengesBinding
import com.sws.oneonone.model.MyFollowingResult
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.CreateChallengerData
import com.sws.oneonone.util.ShowingImage
import java.util.*

class FollowingSelectedAdapter(activity: BaseActivity):
    RecyclerView.Adapter<FollowingSelectedAdapter.ViewHolder>(){

    private var activity: BaseActivity? = activity

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val layoutSelectedFollowingsBinding = DataBindingUtil.inflate<LayoutSelectedFollowingsBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.layout_selected_followings, viewGroup, false
        )
        return ViewHolder(layoutSelectedFollowingsBinding)
    }

    override fun onBindViewHolder(ViewHolder: ViewHolder, i: Int) {
        val model= CreateChallengerData.selectList[i]
       // model.username = "@" + model.username
        if (!model.profileImg.isNullOrEmpty()){
            ShowingImage.showImage(activity!!, model.profileImg, ViewHolder.layoutSelectedFollowingsBinding.ivPic)
        }
        ViewHolder.layoutSelectedFollowingsBinding.rlRemove.setOnClickListener {
            CreateChallengerData.selectList.removeAt(i)
            notifyDataSetChanged()
        }

        ViewHolder.layoutSelectedFollowingsBinding.username.text = "@"+model.username


    }

    override fun getItemCount(): Int {
        return if(CreateChallengerData.selectList.size == 0) 0 else CreateChallengerData.selectList.size
    }

    inner class ViewHolder(val layoutSelectedFollowingsBinding: LayoutSelectedFollowingsBinding) :
        RecyclerView.ViewHolder(layoutSelectedFollowingsBinding.root)

}
