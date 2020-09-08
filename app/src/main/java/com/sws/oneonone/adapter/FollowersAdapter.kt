package com.sws.oneonone.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.databinding.LayoutFollowersAdapterBinding
import com.sws.oneonone.model.ExploreModel
import com.sws.oneonone.model.ExploreResult
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.util.AppStaticData
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.Loader
import com.sws.oneonone.util.Utils
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import java.util.ArrayList

class FollowersAdapter:
    RecyclerView.Adapter<FollowersAdapter.ViewHolder>(){
    var service: ApiClient? = ApiClient()
    private var list: ArrayList<String>? = null
    private var activity: BaseActivity? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val followListItemBinding = DataBindingUtil.inflate<LayoutFollowersAdapterBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.layout_followers_adapter, viewGroup, false
        )
        return ViewHolder(followListItemBinding)
    }

    override fun onBindViewHolder(ViewHolder: ViewHolder, i: Int) {

     if(i == 2){
         ViewHolder.layoutFollowersAdapterBinding.tvfollowing.visibility = View.VISIBLE
         ViewHolder.layoutFollowersAdapterBinding.tvfollow.visibility = View.GONE
     }else{
         ViewHolder.layoutFollowersAdapterBinding.tvfollowing.visibility = View.GONE
         ViewHolder.layoutFollowersAdapterBinding.tvfollow.visibility = View.VISIBLE
     }


    }

    override fun getItemCount(): Int {
        return 10
    }

    fun setPackageList(activity: BaseActivity, list: ArrayList<String>) {
        this.list = list
        this.activity = activity
        notifyDataSetChanged()
    }

    inner class ViewHolder(val layoutFollowersAdapterBinding: LayoutFollowersAdapterBinding) :
        RecyclerView.ViewHolder(layoutFollowersAdapterBinding.root)
}