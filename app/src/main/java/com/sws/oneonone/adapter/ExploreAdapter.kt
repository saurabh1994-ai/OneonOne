package com.sws.oneonone.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.sws.oneonone.R
import com.sws.oneonone.databinding.LayoutExploreBinding
import com.sws.oneonone.fragment.ChatMessageFragment
import com.sws.oneonone.fragment.ViewExploreFragment
import com.sws.oneonone.model.CommentResultModel
import com.sws.oneonone.model.ExploreResult
import com.sws.oneonone.service.VideoPreLoadingService
import com.sws.oneonone.util.AndroidUtilities
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.Constants
import com.sws.oneonone.util.ShowingImage
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ExploreAdapter:
    RecyclerView.Adapter<ExploreAdapter.ViewHolder>(){

    var exploreList: ArrayList<ExploreResult>? = null
    private var activity: BaseActivity? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val exploreBinding = DataBindingUtil.inflate<LayoutExploreBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.layout_explore, viewGroup, false
        )
        return ViewHolder(exploreBinding)
    }

    override fun onBindViewHolder(ViewHolder: ViewHolder, i: Int) {
        val model: ExploreResult? = exploreList!![i]
        if (!model?.image.isNullOrEmpty()) {
            ShowingImage.showBannerImage(activity!!, model?.image, ViewHolder.layoutExploreBinding.ivBannerIamge)
        }
        val draw = AndroidUtilities().generateRandomBg()
        ViewHolder.layoutExploreBinding.layoutMain.background = draw
        if (!model?.userId?.profileImg.isNullOrEmpty()) {
            ShowingImage.showImage(
                activity!!,
                model?.userId?.profileImg,
                ViewHolder.layoutExploreBinding.ivPic
            )
        }

        if (!model?.video.isNullOrEmpty()) {
            ShowingImage.showBannerImage(activity!!, model?.thumbnails, ViewHolder.layoutExploreBinding.ivBannerIamge)
        }

        if(model?.isView!!){
            ViewHolder.layoutExploreBinding.ivPic.setBackgroundResource(R.drawable.image_border_gray)
        }

        ViewHolder.layoutExploreBinding.exploreResult = model
        ViewHolder.layoutExploreBinding.layoutMain.setOnClickListener {
           /* if(model?.video!!.isNotEmpty()){
                startPreLoadingService(model.video!!)
            }*/
            activity!!.replaceFragment(ViewExploreFragment.newInstance(model.uid))
        }

        //if (model?.isOpenChallenge!!) {
            getTime(model?.createdAt!!, ViewHolder.layoutExploreBinding.timer)
      //  } else {
       //     ViewHolder.layoutExploreBinding.timer?.visibility = View.GONE
       // }
    }

    override fun getItemCount(): Int {
        return if (exploreList == null) 0 else exploreList!!.size
    }

    fun setExploreList(activity: BaseActivity, exploreList: ArrayList<ExploreResult>) {
        this.exploreList = exploreList
        this.activity = activity
        notifyDataSetChanged()
    }

    fun addData(listItems: ArrayList<ExploreResult>) {
        val size = this.exploreList!!.size
        this.exploreList!!.addAll(listItems)
        val sizeNew = this.exploreList!!.size
        notifyItemRangeChanged(size, sizeNew)
    }

    inner class ViewHolder(val layoutExploreBinding: LayoutExploreBinding) :
        RecyclerView.ViewHolder(layoutExploreBinding.root)


    private fun startPreLoadingService(videoUrl : String) {
        val preloadingServiceIntent = Intent(activity, VideoPreLoadingService::class.java)
        preloadingServiceIntent.putExtra(Constants.VIDEO_URL,videoUrl)
        activity?.startService(preloadingServiceIntent)
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
