package com.sws.oneonone.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.sws.oneonone.R
import com.sws.oneonone.databinding.NewChatHeaderBinding
import com.sws.oneonone.firebaseModel.UserModel
import com.sws.oneonone.fragment.ChatMessageFragment
import com.sws.oneonone.model.SignUpResultModel
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.ShowingImage
import java.util.*

class ChatHeaderAdapter(activity: BaseActivity, var headerList: ArrayList<SignUpResultModel>):
    RecyclerView.Adapter<ChatHeaderAdapter.ViewHolder>(), Filterable {
    private var filter = CustomFilter()

    private var list: ArrayList<String>? = null
    private var activity: BaseActivity? = activity
    var firstChar: String?= "A"
    var childList: ArrayList<SignUpResultModel> = ArrayList<SignUpResultModel>()

    var OrginalList: ArrayList<SignUpResultModel>? = headerList
    var SuggestionList: ArrayList<SignUpResultModel>? = ArrayList<SignUpResultModel>()


    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val newChatHeaderBinding = DataBindingUtil.inflate<NewChatHeaderBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.new_chat_header, viewGroup, false
        )

        return ViewHolder(newChatHeaderBinding)
    }

    override fun onBindViewHolder(ViewHolder: ViewHolder, i: Int) {
        val model: SignUpResultModel? = headerList?.get(i)
        if(!model?.profileImg.isNullOrEmpty()){
            ShowingImage?.showImage(activity!!, model?.profileImg, ViewHolder.newChatHeaderBinding!!.profileImage)
        }

        var name: String? = model?.fullName
        if (!name.isNullOrEmpty()) {
            val first = name[0].toString()
            Log.e("qwerty123", first)
            if(i==0){
                firstChar = first
                ViewHolder.newChatHeaderBinding!!.tvHeader?.text = first.toString().capitalize()
                ViewHolder.newChatHeaderBinding!!.tvHeader?.visibility = View.VISIBLE

            } else {
                if (first.toString().capitalize().equals(firstChar?.capitalize())) {
                    ViewHolder.newChatHeaderBinding!!.tvHeader?.text = first
                    ViewHolder.newChatHeaderBinding!!.tvHeader?.visibility = View.GONE
                } else {
                    ViewHolder.newChatHeaderBinding!!.tvHeader?.text = first
                    ViewHolder.newChatHeaderBinding!!.tvHeader?.visibility = View.VISIBLE
                    firstChar = first
                }
            }
        }


        ViewHolder.newChatHeaderBinding!!.mainLayout?.setOnClickListener {

            val userModel: UserModel? = UserModel()
            userModel?.setWithChatUserId(model?.id)
            userModel?.setUserImg(model?.profileImg)
            userModel?.setUserName(model?.fullName)
            userModel?.setFcmToken(model?.token)

            val fragment = ChatMessageFragment()
            fragment?.setChatWithUserId("", model?.id, userModel)
            activity?.onBackPressed()
            activity!!.replaceFragment(fragment)
        }

        ViewHolder.newChatHeaderBinding!!.chatHeader = model
    }

    override fun getItemCount(): Int {
        return headerList?.size
    }

    inner class ViewHolder(val newChatHeaderBinding: NewChatHeaderBinding) :
        RecyclerView.ViewHolder(newChatHeaderBinding.root)

    override fun getFilter(): Filter {
        return filter
    }

    private inner class CustomFilter: Filter() {
        protected override fun performFiltering(constraint:CharSequence): FilterResults {
            SuggestionList?.clear()
            // Check if the Original List and Constraint aren't null.
            if (OrginalList != null && constraint != null) {
                for (i in 0 until OrginalList!!.size) {
                    val list: SignUpResultModel? = OrginalList?.get(i)//?.getUserMadelData()
                    if (list?.username?.toLowerCase()?.contains(constraint)!! && list?.fullName?.toLowerCase()?.contains(constraint)!!) {
                        SuggestionList?.add(OrginalList?.get(i)!!)
                    }
                }
            }
            // Create new Filter Results and return this to publishResults;
            val results = FilterResults()
            results.values = SuggestionList
            results.count = SuggestionList!!.size
            return results
        }
        protected override fun publishResults(constraint:CharSequence, results: FilterResults) {
            headerList = SuggestionList!!
            notifyDataSetChanged()
        }
    }
}