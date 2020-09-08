package com.sws.oneonone.adapter


import android.content.Intent
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
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
import com.sws.oneonone.databinding.LayoutNotificationItemBinding
import com.sws.oneonone.dialog.AlertDialog
import com.sws.oneonone.dialog.NotificationDialog
import com.sws.oneonone.dialog.PreviewBottomDialog
import com.sws.oneonone.dialog.ProfileBottomDialog
import com.sws.oneonone.fragment.ProfileFragment
import com.sws.oneonone.fragment.ViewExploreFragment
import com.sws.oneonone.fragment.WaterChallengeFragment
import com.sws.oneonone.model.NotificationResultModel
import com.sws.oneonone.model.SignUpModel
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.service.VideoPreLoadingService
import com.sws.oneonone.util.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.regex.Pattern


class NotificationAdapter: RecyclerView.Adapter<NotificationAdapter.ViewHolder>(),NotificationCenter.NotificationCenterDelegate{
    var notificationList: ArrayList<NotificationResultModel>? = null
    var activity:BaseActivity? = null
    val pattern = Pattern.compile(AndroidUtilities().linkPatterns)
    val loginUsermodel: SignUpModel? = AppStaticData().getModel()
    var name = ""
    var service: ApiClient? = ApiClient()

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val layoutNotificationItemBinding = DataBindingUtil.inflate<LayoutNotificationItemBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.layout_notification_item, viewGroup, false
        )
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.isFollow)
        return ViewHolder(layoutNotificationItemBinding)
    }

    override fun onBindViewHolder(ViewHolder: ViewHolder, i: Int) {
       val   model = notificationList!![i]


        if(model.senderId != null){
            ShowingImage.showImage(
                activity!!,
                model.senderId.profileImg,
                ViewHolder.layoutNotificationItemBinding.ciView
            )

            if (model.senderId.fullName.isEmpty()) {
                ViewHolder.layoutNotificationItemBinding.username.text = model.senderId.username
            } else {
                ViewHolder.layoutNotificationItemBinding.username.text  = model.senderId.fullName
            }
        }else{
            ShowingImage.showImage(
                activity!!,
                loginUsermodel?.result?.profileImg,
                ViewHolder.layoutNotificationItemBinding.ciView
            )
            if (loginUsermodel?.result?.fullName!!.isEmpty()) {
                ViewHolder.layoutNotificationItemBinding.username.text  = loginUsermodel.result!!.username!!
                name = loginUsermodel.result!!.username!!
            } else {
                ViewHolder.layoutNotificationItemBinding.username.text  = loginUsermodel.result!!.fullName!!
                name = loginUsermodel.result!!.fullName!!
            }
        }
        /*ViewHolder.layoutNotificationItemBinding.notificationResult = model
        ViewHolder.layoutNotificationItemBinding.lifecycleOwner = activity*/

      /*  if(model?.challengeView!!){
            ViewHolder.layoutNotificationItemBinding.ciView.setBackgroundResource(R.drawable.image_border_gray)
        }else{
            ViewHolder.layoutNotificationItemBinding.ciView.setBackgroundResource(R.drawable.image_border)
        }*/

        if (model.challengeImage.isNotEmpty()) {
            ShowingImage.showBannerImage(
                activity!!,
                model.challengeImage,
                ViewHolder.layoutNotificationItemBinding.imageIv
            )
        }

        ViewHolder.layoutNotificationItemBinding.tvTime.text = AndroidUtilities().getTime(model.createdAt,activity!!)
        ViewHolder.layoutNotificationItemBinding.tvTime.setTextColor(
            ContextCompat.getColor(
                activity!!,
                R.color.gray
            )
        )
        when (model.type) {
            "1" -> {
                if (model.expireRespondTime) { // if true
                    model.challengeResponse = "Challenge Expired"
                } else { // if false
                    model.challengeResponse = model.respondTime + " left to respond"
                }
                val message = model.message + " \uD83D\uDE04"
                val msgHashtag = StringBuilder(message)
                for (hashtag in model.hashtags) {
                    msgHashtag.append(" $hashtag")
                }

               val messageHastag = msgHashtag.toString()
                val inputStr = SpannableString(messageHastag)

                val matcher = pattern.matcher(inputStr)
                while (matcher.find()) {
                    inputStr.setSpan(
                        ForegroundColorSpan(
                            ContextCompat.getColor(
                                activity!!,
                                R.color.pink
                            )
                        ), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                 ViewHolder.layoutNotificationItemBinding.tvTypes.text = inputStr

                ViewHolder.layoutNotificationItemBinding.tvTime.setTextColor(
                    ContextCompat.getColor(
                        activity!!,
                        R.color.purple_text_color
                    )
                )
                ViewHolder.layoutNotificationItemBinding.tvTime.text = model.challengeResponse
                ViewHolder.layoutNotificationItemBinding.tvfollow.visibility = View.GONE
                ViewHolder.layoutNotificationItemBinding.cardView.visibility = View.VISIBLE
            }

            "2" -> {
               // val spannableString = getEmotiText("Has been accepted!","your challenge",R.drawable.happy_emoti)
                //ViewHolder.layoutNotificationItemBinding.tvTypes.text = spannableString

                ViewHolder.layoutNotificationItemBinding.tvTypes.text = model.message
                ViewHolder.layoutNotificationItemBinding.tvfollow.visibility = View.GONE
                ViewHolder.layoutNotificationItemBinding.cardView.visibility = View.VISIBLE


            }

            "3" -> {
              //  val spannableString = getEmotiText("Has been refused","your challenge",R.drawable.sad_emoti)
                //ViewHolder.layoutNotificationItemBinding.tvTypes.text = spannableString
                ViewHolder.layoutNotificationItemBinding.tvTypes.text = model.message
                ViewHolder.layoutNotificationItemBinding.tvfollow.visibility = View.GONE
                ViewHolder.layoutNotificationItemBinding.cardView.visibility = View.VISIBLE
            }

            "4" -> {
                ViewHolder.layoutNotificationItemBinding.tvTypes.text = model.message
                ViewHolder.layoutNotificationItemBinding.tvfollow.visibility = View.GONE
                ViewHolder.layoutNotificationItemBinding.cardView.visibility = View.VISIBLE
            }

            "5" -> {
                ViewHolder.layoutNotificationItemBinding.tvTypes.text = model.message
                ViewHolder.layoutNotificationItemBinding.tvfollow.visibility = View.GONE
                ViewHolder.layoutNotificationItemBinding.cardView.visibility = View.VISIBLE
            }

            "6" -> {
                ViewHolder.layoutNotificationItemBinding.tvTypes.text = model.message
                ViewHolder.layoutNotificationItemBinding.tvfollow.visibility = View.GONE
                ViewHolder.layoutNotificationItemBinding.cardView.visibility = View.VISIBLE
            }
            "7" -> {
                ViewHolder.layoutNotificationItemBinding.tvTypes.text = model.message
                ViewHolder.layoutNotificationItemBinding.tvfollow.visibility = View.GONE
                ViewHolder.layoutNotificationItemBinding.cardView.visibility = View.VISIBLE
            }
            "8" -> {
                ViewHolder.layoutNotificationItemBinding.tvTypes.text = model.message
                ViewHolder.layoutNotificationItemBinding.tvfollow.visibility = View.GONE
                ViewHolder.layoutNotificationItemBinding.cardView.visibility = View.VISIBLE
            }
            "9" -> {
                ViewHolder.layoutNotificationItemBinding.tvTypes.text = model.message
                if(!model.isFollow){
                    Log.d("isFollow",model.isFollow.toString())
                    ViewHolder.layoutNotificationItemBinding.tvfollow.visibility = View.VISIBLE
                }else{
                    ViewHolder.layoutNotificationItemBinding.tvfollow.visibility = View.GONE

                }

                ViewHolder.layoutNotificationItemBinding.cardView.visibility = View.GONE

            }

        }

        ViewHolder.layoutNotificationItemBinding.tvfollow.setOnClickListener {
            followUnFollow("1",model.senderId!!._id,ViewHolder.layoutNotificationItemBinding.tvfollow,i)

        }

        ViewHolder.layoutNotificationItemBinding.cardView.setOnClickListener {
            activity?.replaceFragment(ViewExploreFragment.newInstance(model.uid))
          /*  val uiData = notificationList!![i]
             uiData.challengeId.challengeView = uiData.challengeView
            if(uiData.challengeId.video!!.isNotEmpty()){
                startPreLoadingService(uiData.challengeId.video!!)
            }
            val  videoBottomSheetDialog = PreviewBottomDialog.newInstance(activity!!,null,null,null, null,uiData.challengeId)
            videoBottomSheetDialog.show(activity!!.supportFragmentManager, PreviewBottomDialog().TAG)*/

        }

        ViewHolder.layoutNotificationItemBinding.ciView.setOnClickListener {
            if(model.senderId != null){
                val  videoBottomSheetDialog = ProfileBottomDialog.newInstance(activity!!,model.senderId._id,i,ViewHolder.layoutNotificationItemBinding.tvfollow)
                videoBottomSheetDialog.show(activity!!.supportFragmentManager, ProfileBottomDialog().TAG)

               // activity?.replaceFragment(ProfileFragment.newInstance(model.senderId._id))
            }else{
                val  videoBottomSheetDialog = ProfileBottomDialog.newInstance(activity!!,"",i,ViewHolder.layoutNotificationItemBinding.tvfollow)
                videoBottomSheetDialog.show(activity!!.supportFragmentManager, ProfileBottomDialog().TAG)
              //  activity?.replaceFragment(ProfileFragment.newInstance(loginUsermodel?.result?.id))
            }

        }

        ViewHolder.layoutNotificationItemBinding.username.setOnClickListener {
            if(model.senderId != null){
                val  videoBottomSheetDialog = ProfileBottomDialog.newInstance(activity!!,model.senderId._id,i,ViewHolder.layoutNotificationItemBinding.tvfollow)
                videoBottomSheetDialog.show(activity!!.supportFragmentManager, ProfileBottomDialog().TAG)
             //   activity?.replaceFragment(ProfileFragment.newInstance(model.senderId._id))
            }else{
                val  videoBottomSheetDialog = ProfileBottomDialog.newInstance(activity!!,"",i,ViewHolder.layoutNotificationItemBinding.tvfollow)
                videoBottomSheetDialog.show(activity!!.supportFragmentManager, ProfileBottomDialog().TAG)
                //activity?.replaceFragment(ProfileFragment.newInstance(loginUsermodel?.result?.id))
            }
        }

        ViewHolder.layoutNotificationItemBinding.mainLayout.setOnClickListener {


              if (model.type == "1") {
                  if (model.expireRespondTime) {
                    AndroidUtilities().openAlertDialog("challenge expired." , activity!!)
                  }else if(model.request == "D"){
                      openNotification(model.type, "")
                  }else if(model.isAccepted){
                      AndroidUtilities().openAlertDialog("Challenge Already Accepted",activity!!)
                  }else{
                      val uiData = notificationList!![i]//?.challengeId
                      if(uiData.challengeId.video!!.isNotEmpty()){
                          startPreLoadingService(uiData.challengeId.video!!)
                      }
                      val fragment = WaterChallengeFragment()
                      fragment.setUIData(uiData.challengeId)
                      activity?.replaceFragment(fragment)
                  }
              }else if(model.type == "2"){
                  openNotification(model.type,"")

              }else if(model.type == "3"){
                  if (model.senderId?.fullName!!.isEmpty()) {
                      ViewHolder.layoutNotificationItemBinding.username.text = model.senderId.username
                      name = model.senderId.username
                  } else {
                      ViewHolder.layoutNotificationItemBinding.username.text  = model.senderId.fullName
                      name = model.senderId.fullName
                  }
                  openNotification(model.type, name)
              }else if(model.type == "4"){
                  openNotification(model.type, "")
              }else if(model.type == "5"){
                  openNotification(model.type, "")
              } else if(model.type == "6" ||model.type == "7" || model.type == "8"){
                  activity?.replaceFragment(ViewExploreFragment.newInstance(model.uid))
              }else if(model.type == "9"){
                  if(model.senderId != null){
                      val  videoBottomSheetDialog = ProfileBottomDialog.newInstance(activity!!,model.senderId._id,i,ViewHolder.layoutNotificationItemBinding.tvfollow)
                      videoBottomSheetDialog.show(activity!!.supportFragmentManager, ProfileBottomDialog().TAG)
                     // activity?.replaceFragment(ProfileFragment.newInstance(model.senderId._id))
                  }else{
                      val  videoBottomSheetDialog = ProfileBottomDialog.newInstance(activity!!,"",i,ViewHolder.layoutNotificationItemBinding.tvfollow)
                      videoBottomSheetDialog.show(activity!!.supportFragmentManager, ProfileBottomDialog().TAG)
                    //  activity?.replaceFragment(ProfileFragment.newInstance(loginUsermodel?.result?.id))
                  }
              }
          }




    }

    override fun getItemCount(): Int {
        return if(notificationList == null) 0 else notificationList!!.size
    }

    inner class ViewHolder(val layoutNotificationItemBinding: LayoutNotificationItemBinding) :
        RecyclerView.ViewHolder(layoutNotificationItemBinding.root)

    fun  getEmotiText(strtText:String, endtxt:String, emoti:Int) : SpannableString{
        val image =
            ContextCompat.getDrawable(activity!!, emoti)
        image!!.setBounds(0, 0, image.intrinsicWidth, image.intrinsicHeight)
        // Replace blank spaces with image icon
        // Replace blank spaces with image icon
        val startText= strtText
        val textLength = startText.length
        val sb = SpannableString("$startText   $endtxt")
        val imageSpan = ImageSpan(image, ImageSpan.ALIGN_BOTTOM)
        sb.setSpan(imageSpan, textLength + 1, textLength + 2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return sb
    }

    fun setNotificationAdapter(activity: BaseActivity, notificationList: ArrayList<NotificationResultModel>) {
        this.notificationList = notificationList
        this.activity = activity
        notifyDataSetChanged()
    }

    fun addData(listItems: ArrayList<NotificationResultModel>) {
        val size = this.notificationList!!.size
        this.notificationList!!.addAll(listItems)
        val sizeNew = this.notificationList!!.size
        notifyItemRangeChanged(size, sizeNew)
    }

    fun openNotification(type : String,name: String) {
        val myDialog = NotificationDialog.newInstance(activity!!,type,name)
        myDialog.show(activity!!.supportFragmentManager, "Hello Testing")
    }

    private fun startPreLoadingService(videoUrl : String) {
        val preloadingServiceIntent = Intent(activity, VideoPreLoadingService::class.java)
        preloadingServiceIntent.putExtra(Constants.VIDEO_URL,videoUrl)
        activity?.startService(preloadingServiceIntent)
    }


    fun followUnFollow( type: String?, followId: String?, followbtn:TextView,pos : Int) {
        try {
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
                            val mSignUpModel = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity!!,mSignUpModel.code!!,mSignUpModel.message)
                            if(checkResponse) {
                                Utils.showToast(activity!!,mSignUpModel.message!!,false)
                                if (mSignUpModel.code == 200) {
                                     notificationList!![pos].isFollow = true
                                    followbtn.visibility = View.GONE
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
                notificationList!![pos].isFollow = true
               followbtn.visibility = View.GONE
                notifyDataSetChanged()
            }else{
                notificationList!![pos].isFollow = false
                followbtn.visibility = View.VISIBLE
                notifyDataSetChanged()
            }

        }
    }

}