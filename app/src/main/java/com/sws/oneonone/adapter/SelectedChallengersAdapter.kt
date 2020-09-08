package com.sws.oneonone.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.databinding.SelectedCheckboxAdapterBinding
import com.sws.oneonone.model.MyFollowingResult
import com.sws.oneonone.model.SignUpModel
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.util.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class SelectedChallengersAdapter():
    RecyclerView.Adapter<SelectedChallengersAdapter.ViewHolder>(){

    var list: ArrayList<MyFollowingResult> ? = null
    var service: ApiClient? = ApiClient()

    private var activity: BaseActivity? = null
    var selectList: ArrayList<MyFollowingResult> ? = null
    private var selectedList: HashSet<MyFollowingResult>? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val selectedCheckboxAdapterBinding = DataBindingUtil.inflate<SelectedCheckboxAdapterBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.selected_checkbox_adapter, viewGroup, false
        )
        return ViewHolder(selectedCheckboxAdapterBinding)
    }

    override fun onBindViewHolder(ViewHolder: ViewHolder, i: Int) {
        val model: MyFollowingResult? = list?.get(i)?.getMyFollowingResult()
        if (!model?.profileImg.isNullOrEmpty()){
            ShowingImage.showImage(activity!!, model?.profileImg, ViewHolder.selectedCheckboxAdapterBinding.profileImage)
        }
        ViewHolder.selectedCheckboxAdapterBinding.myFollowingResult = model

        if(model?.isFollow!!){
            ViewHolder.selectedCheckboxAdapterBinding.cbAction.visibility = View.VISIBLE
            ViewHolder.selectedCheckboxAdapterBinding.tvFollowing.visibility = View.GONE
        }else{
            ViewHolder.selectedCheckboxAdapterBinding.cbAction.visibility = View.GONE
            ViewHolder.selectedCheckboxAdapterBinding.tvFollowing.visibility = View.VISIBLE
        }

        ViewHolder.selectedCheckboxAdapterBinding.tvFollowing.setOnClickListener {
            val data: MyFollowingResult? = list?.get(i)?.getMyFollowingResult()
            followUnFollow( i, "1", data?.id)
        }

        if (!CreateChallengerData.selectList.isNullOrEmpty()) {
                // Loop arrayList1 items
                var selected = false
                for (selectedFollower in CreateChallengerData.selectList) {
                    if (model!!.id == selectedFollower.id) {
                        selected = true

                    }
                }
                ViewHolder.selectedCheckboxAdapterBinding.cbAction.isChecked = selected
        }else{
            ViewHolder.selectedCheckboxAdapterBinding.cbAction.isChecked = false
        }
        ViewHolder.selectedCheckboxAdapterBinding.cbAction.setOnClickListener {
            val data: MyFollowingResult? = list?.get(i)?.getMyFollowingResult()

               if (!ViewHolder.selectedCheckboxAdapterBinding.cbAction.isChecked) {

                   if (!CreateChallengerData.selectList.isNullOrEmpty()) {
                       for (j in 0 until CreateChallengerData.selectList.size) {
                           if (CreateChallengerData.selectList[j].id.equals(data?.id)) {
                               CreateChallengerData.selectList.removeAt(j)
                               break
                           }
                       }
                   }
               } else {
                   if (CreateChallengerData.selectList.size < 4) {
                       CreateChallengerData.selectList.add(data!!)
                   } else {
                       ViewHolder.selectedCheckboxAdapterBinding.cbAction.isChecked = false
                       Utils.showToast(activity!!, "You are not add more than four", false)
                   }
               }

        }



    }

    fun setSelectedAdapter(activity: BaseActivity, list: ArrayList<MyFollowingResult> ){
        this.activity = activity
        this.list = list
        selectList = ArrayList()
        selectedList = HashSet<MyFollowingResult>()
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return if(list == null) return 0 else list!!.size
    }

    inner class ViewHolder(val selectedCheckboxAdapterBinding: SelectedCheckboxAdapterBinding) :
        RecyclerView.ViewHolder(selectedCheckboxAdapterBinding.root)


    fun followUnFollow( position: Int, type: String?, followId: String?) {
        try {
            Loader.showLoader(activity!!)
            val rootObject = JsonObject()
            try {
                rootObject.addProperty("userId",  AppStaticData().getModel()?.result?.id)
                rootObject.addProperty("followId",  followId)
                rootObject.addProperty("type",  type)
            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.setFollowUnFollow(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<SignUpModel> {
                override fun onResponse(call: Call<SignUpModel>, response: Response<SignUpModel>) {
                    if (response.code() == 200) {
                        try {
                            Loader.hideLoader(activity!!)
                            val mSignUpModel = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity!!,mSignUpModel.code!!,mSignUpModel.message)
                            if(checkResponse){
                                if (mSignUpModel.code == 200) {
                                    val model: MyFollowingResult? = list?.get(position)?.getMyFollowingResult()
                                    model?.isFollow = true
                                    list?.get(position)?.setMyFollowingResult(model)
                                    notifyItemChanged(position)
                                }
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
                override fun onFailure(call: Call<SignUpModel>, t: Throwable) {
                    Log.e("responseError", t.message)
                    Utils.showToast(activity!!, "Have some problem...", false)
                    Loader.hideLoader(activity!!)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


}
