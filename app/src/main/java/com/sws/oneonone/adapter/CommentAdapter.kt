package com.sws.oneonone.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.sws.oneonone.R
import com.sws.oneonone.databinding.CommentListItemBinding
import com.sws.oneonone.model.CommentResultModel
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.ShowingImage
import java.util.ArrayList

class CommentAdapter(): RecyclerView.Adapter<CommentAdapter.ViewHolder>(){

    private var commentList: ArrayList<CommentResultModel>? = null
    private var activity: BaseActivity? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val commentListItemBinding = DataBindingUtil.inflate<CommentListItemBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.comment_list_item, viewGroup, false
        )
        return ViewHolder(commentListItemBinding)
    }

    override fun onBindViewHolder(ViewHolder: ViewHolder, i: Int) {

        val commentData = commentList!![i]

        ViewHolder.commentListItemBinding.commentModel = commentData
        ShowingImage.showImage(
            activity!!,
            commentList!![i].userId.profileImg,
            ViewHolder.commentListItemBinding.ciView
        )
    }

    override fun getItemCount(): Int {
        return if (commentList == null) 0 else commentList!!.size

    }

    fun setCommentsList(activity: BaseActivity, commentList: ArrayList<CommentResultModel>) {
        this.commentList = commentList
        this.activity = activity
        notifyDataSetChanged()
    }

    fun addData(listItems: ArrayList<CommentResultModel> , data :CommentResultModel?) {
        if(data != null){
            commentList!!.add(0, data       )
        }

        val size = this.commentList!!.size
        this.commentList!!.addAll(listItems)
        val sizeNew = this.commentList!!.size
        notifyItemRangeChanged(size, sizeNew)
    }

    inner class ViewHolder(val commentListItemBinding: CommentListItemBinding) :
        RecyclerView.ViewHolder(commentListItemBinding.root)

}