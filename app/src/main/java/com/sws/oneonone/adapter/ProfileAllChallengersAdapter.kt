package com.sws.oneonone.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sws.oneonone.R
import com.sws.oneonone.databinding.LayoutMyChallengerItemBinding
import com.sws.oneonone.model.ProfileChallengeResult
import com.sws.oneonone.util.AndroidUtilities
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.ShowingImage
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ProfileAllChallengersAdapter:
    RecyclerView.Adapter<ProfileAllChallengersAdapter.ViewHolder>(){

     var activity: BaseActivity? = null
    var chellenge: ArrayList<ProfileChallengeResult> ? = null
    var clickListerner: ClickListerner? = null

    interface ClickListerner {
        fun onSuccess(uid: String?)
    }

    fun setListerner(clickListerner: ClickListerner) {
        this.clickListerner = clickListerner
    }
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val layoutMyChallengerItemBinding = DataBindingUtil.inflate<LayoutMyChallengerItemBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.layout_my_challenger_item, viewGroup, false
        )
        return ViewHolder(layoutMyChallengerItemBinding)
    }

    override fun onBindViewHolder(ViewHolder: ViewHolder, i: Int) {
        val model: ProfileChallengeResult? = chellenge!![i]
        val draw = AndroidUtilities().generateRandomBg()
        ViewHolder.layoutMyChallengerItemBinding.CardImage.background = draw
        if(!model?.image.isNullOrEmpty()){
            ShowingImage.showBannerImage(activity!!, model?.image, ViewHolder.layoutMyChallengerItemBinding.CardImage)
        }else if(!model?.thumbnails.isNullOrEmpty()){
            ShowingImage.showBannerImage(activity!!, model?.thumbnails, ViewHolder.layoutMyChallengerItemBinding.CardImage)
        }

        if(!model?.createdBy?.profileImg.isNullOrEmpty()){
            ShowingImage.showImage(activity!!, model?.createdBy?.profileImg, ViewHolder.layoutMyChallengerItemBinding.profileImage)


        }

        ViewHolder.layoutMyChallengerItemBinding.card.setOnClickListener {
            if (clickListerner != null) {
                clickListerner!!.onSuccess(model?.uid)
            }
        }
        if (!model?.createdAt.isNullOrEmpty()) {
            getTime(model?.createdAt!!, ViewHolder.layoutMyChallengerItemBinding!!.timer)
        }
        ViewHolder.layoutMyChallengerItemBinding.profileChallenge = model


    }

    override fun getItemCount(): Int {
        return if(chellenge == null) 0 else chellenge!!.size
    }

    inner class ViewHolder(val layoutMyChallengerItemBinding: LayoutMyChallengerItemBinding) :
        RecyclerView.ViewHolder(layoutMyChallengerItemBinding.root)

    fun setNotificationAdapter(activity: BaseActivity, allList: ArrayList<ProfileChallengeResult>?) {
        this.chellenge = allList
        this.activity = activity
        notifyDataSetChanged()
    }

    fun addData(listItems: ArrayList<ProfileChallengeResult>?) {
        val size = this.chellenge!!.size
        this.chellenge!!.addAll(listItems!!)
        val sizeNew = this.chellenge!!.size
        notifyItemRangeChanged(size, sizeNew)
    }


    fun getTime(time: String, textView: TextView) {
        @SuppressLint("SimpleDateFormat")
        val spf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val dateFormat2 = SimpleDateFormat("HH:mm:SS")
        spf.timeZone = TimeZone.getTimeZone("UTC")
        dateFormat2.timeZone = TimeZone.getDefault()

        try {
            val millisToAdd = 86400000
            val startDate = Calendar.getInstance().time
            val endDate = spf.parse(time)
            endDate.time = endDate.time + millisToAdd
            val newTime = dateFormat2.format(endDate!!)
            var different = endDate.time - startDate.time
            val timer = object: CountDownTimer(different, 1000)  {
                override fun onTick(millisUntilFinished: Long) {

                    val hms = String.format(
                        "%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                            TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                        ),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                        )
                    )

                    val timeunits: List<String> = hms.split(":") //will break the string up into an array

                    val hours  = timeunits[0].toLong()
                    val minutes = timeunits[1].toLong() //first element
                    val seconds = timeunits[2].toLong() //second element

                    var  mins = ""
                    var hrs = ""
                    var scnds = ""
                    if(minutes < 10){
                        mins = "0$minutes"
                    }else{
                        mins = minutes.toString()
                    }


                    if(hours < 10){
                        hrs = "0$hours"
                    }else{
                        hrs = hours.toString()
                    }

                    if(seconds < 10){
                        scnds = "0$seconds"
                    }else{
                        scnds = seconds.toString()
                    }

                    textView?.visibility = View.VISIBLE
                    textView?.text = hrs+":"+mins// +":" + scnds
                    //viewExplorerVM?.timer?.value = hrs+"\n"+mins +"\n" + scnds
                }

                override fun onFinish() {
                    textView?.visibility = View.GONE
                    textView?.text = "00"+" "+ "00" //+" " + "00"
                    //viewExplorerVM?.timer?.value = "00"+"\n"+ "00" +"\n" + "00"
                }
            }
            timer?.start()

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


}