package com.sws.oneonone.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.sws.oneonone.R
import com.sws.oneonone.databinding.LayoutHastagAdapterBinding
import com.sws.oneonone.model.HastagResult
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.NotificationCenter


class HashtagAdapter: RecyclerView.Adapter<HashtagAdapter.ViewHolder>(){

    var HashList: ArrayList<HastagResult>? = null
    var activity: BaseActivity? = null


    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val layoutHashAdapterBinding = DataBindingUtil.inflate<LayoutHastagAdapterBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.layout_hastag_adapter, viewGroup, false
        )
        return ViewHolder(layoutHashAdapterBinding)
    }

    override fun onBindViewHolder(ViewHolder: ViewHolder, i: Int) {

        val modelResult = HashList!![i]
        ViewHolder.layoutHashAdapterBinding.hashTag.text = modelResult.hashtag
        ViewHolder.layoutHashAdapterBinding.postCount.text = modelResult.hashtagCount.toString() + " posts"
        ViewHolder.layoutHashAdapterBinding.rlHashtag.setOnClickListener {
            NotificationCenter.getInstance()
                .postNotificationName(NotificationCenter.selectedItem, modelResult.hashtag)
        }




    }

    override fun getItemCount(): Int {
        return if (HashList == null) 0 else HashList!!.size
    }

    fun setHashAdapter(activity: BaseActivity, HashList: ArrayList<HastagResult>) {
        this.HashList = HashList
        this.activity = activity
        notifyDataSetChanged()
    }



    inner class ViewHolder(val layoutHashAdapterBinding: LayoutHastagAdapterBinding) :
        RecyclerView.ViewHolder(layoutHashAdapterBinding.root)
}