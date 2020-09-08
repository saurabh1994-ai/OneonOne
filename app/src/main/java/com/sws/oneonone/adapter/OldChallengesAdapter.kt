package com.sws.oneonone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.sws.oneonone.R
import com.sws.oneonone.databinding.LayoutHomeBinding
import com.sws.oneonone.model.HomeModel
import com.sws.oneonone.util.BaseActivity
import java.util.ArrayList

class OldChallengesAdapter:
    RecyclerView.Adapter<OldChallengesAdapter.ViewHolder>(){

    private var list: ArrayList<String>? = null
    private var activity: BaseActivity? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val packagesListItemBinding = DataBindingUtil.inflate<LayoutHomeBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.layout_home, viewGroup, false
        )
        return ViewHolder(packagesListItemBinding)
    }

    override fun onBindViewHolder(ViewHolder: ViewHolder, i: Int) {

        val homeModel = HomeModel(View.GONE, View.GONE, View.GONE,View.GONE)
        ViewHolder.layoutHomeBinding.homeModel = homeModel
        ViewHolder.layoutHomeBinding.lifecycleOwner = activity


    }

    override fun getItemCount(): Int {
        return 4
    }

    fun setPackageList(activity: BaseActivity, list: ArrayList<String>) {
        this.list = list
        this.activity = activity
        notifyDataSetChanged()
    }

    inner class ViewHolder(val layoutHomeBinding: LayoutHomeBinding) :
        RecyclerView.ViewHolder(layoutHomeBinding.root)


}
