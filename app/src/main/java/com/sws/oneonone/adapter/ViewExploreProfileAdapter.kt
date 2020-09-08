package com.sws.oneonone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.sws.oneonone.R
import com.sws.oneonone.databinding.CommentListItemBinding
import com.sws.oneonone.databinding.FragmentViewExploreBinding
import com.sws.oneonone.databinding.LayoutProfileItemBinding
import com.sws.oneonone.fragment.ViewExploreFragment
import com.sws.oneonone.model.ViewExploreModel
import com.sws.oneonone.model.ViewResultModel
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.ShowingImage
import com.sws.oneonone.viewModel.ViewExplorerVM
import java.util.ArrayList

class ViewExploreProfileAdapter: RecyclerView.Adapter<ViewExploreProfileAdapter.ViewHolder>(){

    private var ViewExploreList: ViewExploreModel? = null
    private var activity:BaseActivity? = null
    var clickListerner: ClickListerner? = null



    interface ClickListerner {
        fun onSuccess(id: String?)
    }

    fun setListerner(clickListerner: ClickListerner) {
        this.clickListerner = clickListerner
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val layoutProfileItemBinding = DataBindingUtil.inflate<LayoutProfileItemBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.layout_profile_item, viewGroup, false
        )
        return ViewHolder(layoutProfileItemBinding)
    }

    override fun onBindViewHolder(ViewHolder: ViewHolder, i: Int) {
        val viewList = ViewExploreList!!.result[i]
        if(ViewExploreList!!.position == i){
            ViewHolder.layoutProfileItemBinding.ciView.background =
                ContextCompat.getDrawable(activity?.applicationContext!!, R.drawable.image_border)
            ViewHolder.layoutProfileItemBinding.tipDown.visibility = View.VISIBLE
            ViewHolder.layoutProfileItemBinding.tvCount.background =
                ContextCompat.getDrawable(activity?.applicationContext!!, R.drawable.bg_red_circle)
            ViewHolder.layoutProfileItemBinding.tvCount.setTextColor(ContextCompat.getColor(activity?.applicationContext!!, R.color.white))
        }else{
            ViewHolder.layoutProfileItemBinding.ciView.background =
                ContextCompat.getDrawable(activity?.applicationContext!!, R.drawable.image_border_gray)
            ViewHolder.layoutProfileItemBinding.tipDown.visibility = View.GONE
            ViewHolder.layoutProfileItemBinding.tvCount.background =
                ContextCompat.getDrawable(activity?.applicationContext!!, R.drawable.bg_gray_circle)
            ViewHolder.layoutProfileItemBinding.tvCount.setTextColor(ContextCompat.getColor(activity?.applicationContext!!, R.color.pink))
        }

        ViewHolder.layoutProfileItemBinding.rlProfile.setOnClickListener {
          //  ViewExploreList!!.position  = i
            if (clickListerner != null) {
                clickListerner!!.onSuccess(viewList.userId._id)
            }
            notifyDataSetChanged()
        }

        ShowingImage.showImage(
            activity!!,
            viewList.userId.profileImg,
            ViewHolder.layoutProfileItemBinding.ciView
        )

        ViewHolder.layoutProfileItemBinding.tvCount.text = viewList.votes.toString()


    }

    override fun getItemCount(): Int {
        return if (ViewExploreList == null) 0 else ViewExploreList!!.result.size
    }

    fun setExploreProfileAdapter(activity: BaseActivity, ViewExploreList: ViewExploreModel) {
        this.ViewExploreList = ViewExploreList
        this.activity = activity
        notifyDataSetChanged()
    }

    inner class ViewHolder(val layoutProfileItemBinding: LayoutProfileItemBinding) :
        RecyclerView.ViewHolder(layoutProfileItemBinding.root)


}
