package com.sws.oneonone.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.sws.oneonone.R
import com.sws.oneonone.databinding.DialogAlertBinding
import com.sws.oneonone.util.BaseActivity

class AlertDialog : DialogFragment() {
    var activity: BaseActivity? = null
    var binding: DialogAlertBinding? = null
    var message = ""

    companion object {

        fun newInstance(activity: BaseActivity, msg :String) = AlertDialog().apply {
            this.activity = activity
            this.message = msg

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.bind(inflater.inflate(R.layout.dialog_alert, container, false))
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

        binding!!.tvMessage.text = message
        binding!!.okBtn.setOnClickListener {
            dismiss()
        }


    }

}
