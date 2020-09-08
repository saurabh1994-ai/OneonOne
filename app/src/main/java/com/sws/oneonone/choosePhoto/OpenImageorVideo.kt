package com.sws.oneonone.choosePhoto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sws.oneonone.R
import com.sws.oneonone.util.BaseActivity

class OpenImageorVideo : BottomSheetDialogFragment() {


    private var activity: BaseActivity? = null

    private var listener: CustomItemListener? = null

    interface CustomItemListener {

        fun onTakeTextViewClicked()

        fun onUploadTextViewClicked()

    }

    fun setListener(listener: CustomItemListener) {
        this.listener = listener
    }

    fun setActvity(actvity: BaseActivity) {
        this.activity = actvity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the spinner_textview for this fragment
        return inflater.inflate(R.layout.open_chooser_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cameraButton = view.findViewById(R.id.cameraButton) as ImageButton
        val galleryButton = view.findViewById(R.id.galleryButton) as ImageButton

        cameraButton.setOnClickListener { view1 ->
            if (listener != null)
                listener!!.onTakeTextViewClicked()
        }

        galleryButton.setOnClickListener { view12 ->
            if (listener != null)
                listener!!.onUploadTextViewClicked()
        }

    }
}
