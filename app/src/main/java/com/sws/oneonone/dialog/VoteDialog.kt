package com.sws.oneonone.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.sws.oneonone.R
import com.sws.oneonone.databinding.DialogVoteBinding
import com.sws.oneonone.util.AppStaticData
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.ShowingImage

class VoteDialog: DialogFragment() {
    var activity: BaseActivity? = null
    var binding: DialogVoteBinding? = null

    companion object {

        fun newInstance(activity: BaseActivity) = VoteDialog().apply {
            this.activity = activity

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.bind(inflater.inflate(R.layout.dialog_vote, container, false))
        val dialog = dialog
        if (dialog != null) {
            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(true)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ShowingImage.showImage(
            activity!!,
            AppStaticData().getModel()?.result?.profileImg,
            binding!!.ivProfile
        )

        binding!!.tvusername.text =  AppStaticData().getModel()?.result?.username

    }

}
