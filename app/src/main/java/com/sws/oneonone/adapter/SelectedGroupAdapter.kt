package com.sws.oneonone.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.sws.oneonone.R
import com.sws.oneonone.databinding.LayoutSelectedGroupBinding
import com.sws.oneonone.model.ChildModel
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.ShowingImage
import com.sws.oneonone.viewModel.NewGroupChatVM
import java.util.*

class SelectedGroupAdapter(activity: BaseActivity, var childList: ArrayList<ChildModel>?, newGroupChatVM: NewGroupChatVM?,  adapter: CheckChatHeaderAdapter?):
    RecyclerView.Adapter<SelectedGroupAdapter.ViewHolder>(){

    private var newGroupVM: NewGroupChatVM? = newGroupChatVM
    private var activity: BaseActivity? = activity
    private var adapter: CheckChatHeaderAdapter? = adapter

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val layoutSelectedGroupBinding = DataBindingUtil.inflate<LayoutSelectedGroupBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.layout_selected_group, viewGroup, false
        )
        return ViewHolder(layoutSelectedGroupBinding)
    }

    override fun onBindViewHolder(ViewHolder: ViewHolder, i: Int) {
        val model: ChildModel? = childList?.get(i)
        if(!model?.getImage().isNullOrEmpty()){
            ShowingImage?.showImage(activity!!, model?.getImage(), ViewHolder.layoutSelectedGroupBinding!!.ivPic)
        }
        ViewHolder.layoutSelectedGroupBinding!!.childModel= model

        ViewHolder.layoutSelectedGroupBinding!!.ivPlusIcon?.setOnClickListener {
            if (adapter != null){
                adapter?.removeCheckedItem(childList?.get(i)?.getPosition())
            }
            newGroupVM?.removeFromSelectedList(childList?.get(i))
        }
    }

    override fun getItemCount(): Int {
        return childList!!.size
    }

    inner class ViewHolder(val layoutSelectedGroupBinding: LayoutSelectedGroupBinding) :
        RecyclerView.ViewHolder(layoutSelectedGroupBinding.root)

}