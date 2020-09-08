package com.sws.oneonone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.sws.oneonone.R
import com.sws.oneonone.databinding.LayoutLikesAdapterBinding
import com.sws.oneonone.model.*
import com.sws.oneonone.util.AndroidUtilities
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.ShowingImage

class LikesAdapter: RecyclerView.Adapter<LikesAdapter.ViewHolder>(){

     var LikesList: ArrayList<LikesDataModel>? = null
     var activity:BaseActivity? = null
    var isFromLike = false

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val layoutLikesAdapterBinding = DataBindingUtil.inflate<LayoutLikesAdapterBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.layout_likes_adapter, viewGroup, false
        )
        return ViewHolder(layoutLikesAdapterBinding)
    }

    override fun onBindViewHolder(ViewHolder: ViewHolder, i: Int) {
        if(isFromLike)
            ViewHolder.layoutLikesAdapterBinding.tvtime.visibility = View.VISIBLE
        else
            ViewHolder.layoutLikesAdapterBinding.tvtime.visibility = View.GONE

        if(LikesList!![i].userId != null){
            val dataModel = LikesList!![i].userId
            dataModel!!.name = "@" + dataModel.username
            val userDataModel = UserDataModel(dataModel.username, dataModel.profileImg,dataModel._id, if(isFromLike)AndroidUtilities().getTime(LikesList!![i].createdAt,activity!!)else "",dataModel.name)
            ViewHolder.layoutLikesAdapterBinding.userDataModel = userDataModel
            ShowingImage.showImage(
                activity!!,
                LikesList!![i].userId!!.profileImg,
                ViewHolder.layoutLikesAdapterBinding.ciView
            )

        }



    }

    override fun getItemCount(): Int {
        return if (LikesList == null) 0 else LikesList!!.size
    }

    fun setLikesAdapter(activity: BaseActivity, LikesList: ArrayList<LikesDataModel>, isFromLike:Boolean) {
        this.LikesList = LikesList
        this.activity = activity
        this.isFromLike = isFromLike
        notifyDataSetChanged()
    }

    fun addData(listItems: java.util.ArrayList<LikesDataModel>) {
        val size = this.LikesList!!.size
        this.LikesList!!.addAll(listItems)
        val sizeNew = this.LikesList!!.size
        notifyItemRangeChanged(size, sizeNew)
    }


    inner class ViewHolder(val layoutLikesAdapterBinding: LayoutLikesAdapterBinding) :
        RecyclerView.ViewHolder(layoutLikesAdapterBinding.root)
}