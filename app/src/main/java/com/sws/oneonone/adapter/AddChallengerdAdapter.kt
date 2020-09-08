package com.sws.oneonone.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.databinding.LayoutAddChallengerItemBinding
import com.sws.oneonone.dialog.PreviewBottomDialog
import com.sws.oneonone.dialog.ProfileBottomDialog
import com.sws.oneonone.model.*
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.util.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class AddChallengerdAdapter:
    RecyclerView.Adapter<AddChallengerdAdapter.ViewHolder>(),NotificationCenter.NotificationCenterDelegate{

    private var activity: BaseActivity? = null
    var service: ApiClient? = ApiClient()
    var followingList: ArrayList<MyFollowingResult>? = null
    var pref: PreferenceStore? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val layoutAddChallengerItemBinding = DataBindingUtil.inflate<LayoutAddChallengerItemBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.layout_add_challenger_item, viewGroup, false
        )
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.isFollow)
        return ViewHolder(layoutAddChallengerItemBinding)
    }

    override fun onBindViewHolder(ViewHolder: ViewHolder, i: Int) {
        val model: MyFollowingResult? = followingList!![i]
        if (!model?.profileImg.isNullOrEmpty()) {
            ShowingImage.showImage(activity!!, model?.profileImg, ViewHolder.layoutAddChallengerItemBinding.profileImage)
        }

        ViewHolder.layoutAddChallengerItemBinding.tvusername.text = "@" + model?.username
        if(model?.activeChallenge != null){
            if(model.activeChallenge?.isView!!){
                ViewHolder.layoutAddChallengerItemBinding.profileImage.setBackgroundResource(R.drawable.image_border_gray)
            }else{
                ViewHolder.layoutAddChallengerItemBinding.profileImage.setBackgroundResource(R.drawable.image_border)
            }

        }else{
            ViewHolder.layoutAddChallengerItemBinding.profileImage.setBackgroundResource(R.drawable.image_border_gray)
        }

        ViewHolder.layoutAddChallengerItemBinding.followingResult = model

        ViewHolder.layoutAddChallengerItemBinding.tvFollowing.setOnClickListener {
            val model1: MyFollowingResult? = followingList!![i]
            if (Utils.isNetworkAvailable(activity)) {
                if (model1?.isFollow!!) {
                    followUnFollow(ViewHolder.layoutAddChallengerItemBinding.tvFollowing, i, "2", model1.id)
                } else {
                    followUnFollow(ViewHolder.layoutAddChallengerItemBinding.tvFollowing, i, "1", model1.id)
                }
            } else {
                SnackBar().internetSnackBar(activity)
            }
        }

        ViewHolder.layoutAddChallengerItemBinding.llProfile.setOnClickListener {
            val  videoBottomSheetDialog = ProfileBottomDialog.newInstance(activity!!,model?.id,i, ViewHolder.layoutAddChallengerItemBinding.tvFollowing)
            videoBottomSheetDialog.show(activity!!.supportFragmentManager, ProfileBottomDialog().TAG)

        }

        ViewHolder.layoutAddChallengerItemBinding.profileImage.setOnClickListener {
            if(model?.activeChallenge != null){
                val  videoBottomSheetDialog = PreviewBottomDialog.newInstance(activity!!,null,null,null, model.activeChallenge)
                videoBottomSheetDialog.show(activity!!.supportFragmentManager, PreviewBottomDialog().TAG)
            }else{
                val  videoBottomSheetDialog = ProfileBottomDialog.newInstance(activity!!,model?.id,i,ViewHolder.layoutAddChallengerItemBinding.tvFollowing)
                videoBottomSheetDialog.show(activity!!.supportFragmentManager, ProfileBottomDialog().TAG)
            }
        }
    }

    override fun getItemCount(): Int {
        return if(followingList == null) 0 else followingList!!.size
    }

    fun setchallengerAdapter(activity: BaseActivity, followingList: ArrayList<MyFollowingResult>?) {
        this.followingList = followingList
        this.activity = activity
        notifyDataSetChanged()
    }

    fun addData(listItems: ArrayList<MyFollowingResult>?) {
        val size = this.followingList!!.size
        this.followingList!!.addAll(listItems!!)
        val sizeNew = this.followingList!!.size
        notifyItemRangeChanged(size, sizeNew)
    }

    inner class ViewHolder(val layoutAddChallengerItemBinding: LayoutAddChallengerItemBinding) :
        RecyclerView.ViewHolder(layoutAddChallengerItemBinding.root)


    fun followUnFollow(textView: TextView, position: Int, type: String?, followId: String?) {
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
                               if (type.equals("1")){
                                    followingList!![position].getMyFollowingResult()?.isFollow = true
                                    textView.text = activity!!.getText(R.string.following)
                                    textView.setTextColor(ContextCompat.getColor(activity!!, R.color.pink))
                                    textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sub_icon, 0, 0, 0)

                                   val model = AppStaticData().getModel()
                                   model?.result?.followers = model?.result?.followers!! + 1
                                   val gson = Gson()
                                   val json = gson.toJson(model)
                                   pref = PreferenceStore.getInstance()
                                   pref?.saveStringData("OneOnOne", json)
                                } else {
                                    followingList!![position].getMyFollowingResult()?.isFollow = false
                                    textView.text = activity!!.getText(R.string.follow)
                                    textView.setTextColor(ContextCompat.getColor(activity!!, R.color.black))
                                    textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.add_sub_icon, 0, 0, 0)

                                   val model = AppStaticData().getModel()
                                   model?.result?.followers = model?.result?.followers!! - 1
                                   val gson = Gson()
                                   val json = gson.toJson(model)
                                   pref = PreferenceStore.getInstance()
                                   pref?.saveStringData("OneOnOne", json)
                                }
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

    override fun didReceivedNotification(id: Int, vararg args: Any?) {
        if(id == NotificationCenter.isFollow){
            val type = args[0] as String
            val pos = args[1] as Int
            val followbtn = args[2] as TextView

            if(type == "1"){
                followingList!![pos].isFollow = true
                followbtn.visibility = View.GONE
                notifyDataSetChanged()
            }else{
                followingList!![pos].isFollow = false
                followbtn.visibility = View.VISIBLE
                notifyDataSetChanged()
            }

        }

    }
}
