package com.sws.oneonone.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.sws.oneonone.R
import com.sws.oneonone.databinding.FragmentChangePasswordBinding
import com.sws.oneonone.model.ToolbarModel
import com.sws.oneonone.util.BaseFragment
import com.sws.oneonone.util.NotificationBarColor
import com.sws.oneonone.viewModel.ChangePassVM
import com.sws.oneonone.viewModel.ToolbarVM

class ChangePasswordFragment: BaseFragment() {
    private var binding: FragmentChangePasswordBinding? = null
    var toolbarVM: ToolbarVM? = null
    var changePassVM: ChangePassVM? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationBarColor?.WhiteNotificationBar(activity)
        toolbarVM = ViewModelProviders.of(this).get(ToolbarVM::class.java)
        changePassVM = ViewModelProviders.of(this).get(ChangePassVM::class.java)

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentChangePasswordBinding.bind(inflater.inflate(R.layout.fragment_change_password, container, false))
        binding!!.lifecycleOwner = this
        return binding!!.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbarModel = ToolbarModel(View.VISIBLE, activity?.getString(R.string.change_password) ,View.GONE,View.VISIBLE,View.GONE,View.GONE, 1, "","",View.GONE)
        binding!!.layoutToolbar.toolbarModel = toolbarModel
        binding!!.layoutToolbar.lifecycleOwner = activity

        // ToolBar ViewModel
        toolbarVM?.activity = activity
        binding!!.layoutToolbar.toolbarVM = toolbarVM
       // Change Password ViewModel
        changePassVM?.activity = activity
        binding!!.changePassVM = changePassVM

        initView()
    }
    private fun initView() {
        // check has Observers
        if (!toolbarVM?.comman?.hasObservers()!!) {
            toolbarVM?.comman?.observe(this, Observer<String> { str ->
                when (str) {
                    "onBack" -> onBackPressed()
                }
            })
        }
        // check has Observers
        if (!changePassVM?.comman?.hasObservers()!!) {
            changePassVM?.comman?.observe(this, Observer<String> { str ->
                when (str) {
                    "onBack" -> onBackPressed()
                }
            })
        }
    }

}