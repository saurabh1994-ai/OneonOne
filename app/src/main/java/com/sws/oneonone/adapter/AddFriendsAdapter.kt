package com.sws.oneonone.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.sws.oneonone.R
import com.sws.oneonone.databinding.LayoutAddChallengerItemBinding
import com.sws.oneonone.util.BaseActivity
import java.util.*

class AddFriendsAdapter(activity: BaseActivity):
    RecyclerView.Adapter<AddFriendsAdapter.ViewHolder>(){

    private var list: ArrayList<String>? = null
    private var activity: BaseActivity? = activity

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val layoutAddChallengerItemBinding = DataBindingUtil.inflate<LayoutAddChallengerItemBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.layout_add_challenger_item, viewGroup, false
        )
        return ViewHolder(layoutAddChallengerItemBinding)
    }

    override fun onBindViewHolder(ViewHolder: ViewHolder, i: Int) {
        ViewHolder.layoutAddChallengerItemBinding!!.tvusername?.text = "@UmeshSir"
        if (i == 0){
            ViewHolder.layoutAddChallengerItemBinding!!.tvFollowing?.text = "Added"
            ViewHolder.layoutAddChallengerItemBinding!!.tvFollowing?.setTextColor(ContextCompat.getColor(activity!!, R.color.pink))
            ViewHolder.layoutAddChallengerItemBinding!!.tvFollowing?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sub_icon, 0, 0, 0)
        } else {
            ViewHolder.layoutAddChallengerItemBinding!!.tvFollowing?.text = "Add"
            ViewHolder.layoutAddChallengerItemBinding!!.tvFollowing?.setTextColor(ContextCompat.getColor(activity!!, R.color.black))
            ViewHolder.layoutAddChallengerItemBinding!!.tvFollowing?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.add_sub_icon, 0, 0, 0);
        }

        ViewHolder.layoutAddChallengerItemBinding!!.tvFollowing?.setOnClickListener {
            if ( ViewHolder.layoutAddChallengerItemBinding!!.tvFollowing?.text.equals("Add")){
                ViewHolder.layoutAddChallengerItemBinding!!.tvFollowing?.text = "Added"
                ViewHolder.layoutAddChallengerItemBinding!!.tvFollowing?.setTextColor(ContextCompat.getColor(activity!!, R.color.pink))
                ViewHolder.layoutAddChallengerItemBinding!!.tvFollowing?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sub_icon, 0, 0, 0)
            } else {
                ViewHolder.layoutAddChallengerItemBinding!!.tvFollowing?.text = "Add"
                ViewHolder.layoutAddChallengerItemBinding!!.tvFollowing?.setTextColor(ContextCompat.getColor(activity!!, R.color.black))
                ViewHolder.layoutAddChallengerItemBinding!!.tvFollowing?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.add_sub_icon, 0, 0, 0);

            }
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

    inner class ViewHolder(val layoutAddChallengerItemBinding: LayoutAddChallengerItemBinding) :
        RecyclerView.ViewHolder(layoutAddChallengerItemBinding.root)


}
