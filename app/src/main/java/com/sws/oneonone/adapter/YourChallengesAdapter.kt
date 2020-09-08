package com.sws.oneonone.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.sws.oneonone.R
import com.sws.oneonone.databinding.LayoutYourChallengesBinding
import com.sws.oneonone.dialog.PreviewBottomDialog
import com.sws.oneonone.model.ExploreResult
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.ShowingImage
import kotlin.collections.ArrayList

class YourChallengesAdapter:
    RecyclerView.Adapter<YourChallengesAdapter.ViewHolder>(){
    var activity: BaseActivity? = null
    var challengersList: ArrayList<ArrayList<ExploreResult>>? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val yourChallengesBinding = DataBindingUtil.inflate<LayoutYourChallengesBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.layout_your_challenges, viewGroup, false
        )


        return ViewHolder(yourChallengesBinding)
    }

    override fun onBindViewHolder(ViewHolder: ViewHolder, i: Int) {
        val model = challengersList!![i]
        if (!model[0].userId?.profileImg.isNullOrEmpty()) {
            ShowingImage.showImage(
                activity!!,
                model[0].userId?.profileImg,
                ViewHolder.layoutYourChallengesBinding.ivPic
            )
        }



        if(model[0].isView!!){
            ViewHolder.layoutYourChallengesBinding.ivPic.setBackgroundResource(R.drawable.image_border_gray)
        }


        ViewHolder.layoutYourChallengesBinding.llYourchallenge.setOnClickListener {

            val  videoBottomSheetDialog = PreviewBottomDialog.newInstance(activity!!,null,null, model,null)
            videoBottomSheetDialog.show(activity!!.supportFragmentManager, PreviewBottomDialog().TAG)
        }

        ViewHolder.layoutYourChallengesBinding.exploreResult = model[0]

    }

    override fun getItemCount(): Int {
        return if(challengersList == null)0 else challengersList!!.size
    }


    fun setChallengList(activity: BaseActivity, challengersList: ArrayList<ArrayList<ExploreResult>>) {

        this.challengersList = challengersList
        this.activity = activity
        notifyDataSetChanged()
    }



    inner class ViewHolder(val layoutYourChallengesBinding: LayoutYourChallengesBinding) :
        RecyclerView.ViewHolder(layoutYourChallengesBinding.root)




}
