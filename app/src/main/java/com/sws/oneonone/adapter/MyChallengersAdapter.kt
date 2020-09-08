package com.sws.oneonone.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.sws.oneonone.R
import com.sws.oneonone.databinding.LayoutMyChallengerItemBinding
import com.sws.oneonone.model.ProfileChallengeResult
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.ShowingImage
import java.util.ArrayList

class MyChallengersAdapter(activity: BaseActivity, var chellenge: ArrayList<ProfileChallengeResult>):
    RecyclerView.Adapter<MyChallengersAdapter.ViewHolder>(){

    private var activity: BaseActivity? = activity

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val layoutMyChallengerItemBinding = DataBindingUtil.inflate<LayoutMyChallengerItemBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.layout_my_challenger_item, viewGroup, false
        )
        return ViewHolder(layoutMyChallengerItemBinding)
    }

    override fun onBindViewHolder(ViewHolder: ViewHolder, i: Int) {
        val model: ProfileChallengeResult? = chellenge?.get(i)?.getProfileChallenge()
        if(!model?.image.isNullOrEmpty()){
            ShowingImage?.showBannerImage(activity!!, model?.image, ViewHolder.layoutMyChallengerItemBinding!!.CardImage)
        }

        if(!model?.createdBy?.profileImg.isNullOrEmpty()){
            ShowingImage?.showImage(activity!!, model?.createdBy?.profileImg, ViewHolder.layoutMyChallengerItemBinding!!.profileImage)
        }

        ViewHolder.layoutMyChallengerItemBinding!!.profileChallenge = model
       /* when (i) {
            0 -> {
                ViewHolder.layoutMyChallengerItemBinding?.ivCardImage?.setBackgroundResource(R.drawable.img1)
            }
            1 -> {
                ViewHolder.layoutMyChallengerItemBinding?.ivCardImage?.setBackgroundResource(R.drawable.img2)
            }
            2 -> {
                ViewHolder.layoutMyChallengerItemBinding?.ivCardImage?.setBackgroundResource(R.drawable.img3)
            }
            3 -> {
                ViewHolder.layoutMyChallengerItemBinding?.ivCardImage?.setBackgroundResource(R.drawable.img1)
            }
        }*/

    }

    override fun getItemCount(): Int {
        return chellenge?.size
    }

    inner class ViewHolder(val layoutMyChallengerItemBinding: LayoutMyChallengerItemBinding) :
        RecyclerView.ViewHolder(layoutMyChallengerItemBinding.root)
}