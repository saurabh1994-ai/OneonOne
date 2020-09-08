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
import com.sws.oneonone.databinding.DialogNotificationBinding
import com.sws.oneonone.util.BaseActivity

class NotificationDialog : DialogFragment() {
    var activity: BaseActivity? = null
    var binding: DialogNotificationBinding? = null
    var type = ""
    var name = ""

    companion object {

        fun newInstance(activity: BaseActivity, type :String, name: String) = NotificationDialog().apply {
            this.activity = activity
            this.type = type
            this.name = name

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.bind(inflater.inflate(R.layout.dialog_notification, container, false))
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

        if(type == "1"){
            binding!!.tvMessage.text = getText(R.string.you_loose_challenge)
            binding!!.ivMessage.setImageResource(R.drawable.decline_icon)
        }else if(type == "2"){
            binding!!.tvMessage.text = getText(R.string.congrats_mesaage)
            binding!!.ivMessage.setImageResource(R.drawable.accepted_icon)
            binding!!.dancer.visibility = View.VISIBLE
        }else if(type == "3"){
            binding!!.tvMessage.text = "Sorry! \n" + name +   " refused to challenge you"
            binding!!.ivMessage.setImageResource(R.drawable.decline_icon)
        }else if(type == "4"){
            binding!!.tvMessage.text = getText(R.string.won_message)
            binding!!.ivMessage.setImageResource(R.drawable.won_icon)
            binding!!.dancer.visibility = View.VISIBLE
        }else if(type == "5"){
            binding!!.tvMessage.text = getText(R.string.loose_challenge)
            binding!!.ivMessage.setImageResource(R.drawable.decline_icon)
        }

        binding!!.okBtn.setOnClickListener {
            dismiss()
        }


    }

}
