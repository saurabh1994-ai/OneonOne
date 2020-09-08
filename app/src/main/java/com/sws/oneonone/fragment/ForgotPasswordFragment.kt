package com.sws.oneonone.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.sws.oneonone.R
import com.sws.oneonone.databinding.FragmentForgotPasswordBinding
import com.sws.oneonone.util.BaseFragment
import com.sws.oneonone.viewModel.ForgotPasswordVM

class ForgotPasswordFragment: BaseFragment(){
    private var binding: FragmentForgotPasswordBinding? = null
    var forgotPassVM: ForgotPasswordVM? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        forgotPassVM = ViewModelProviders.of(this).get(ForgotPasswordVM::class.java)
        binding = FragmentForgotPasswordBinding.bind(inflater.inflate(R.layout.fragment_forgot_password, container, false))
        binding!!.setLifecycleOwner(this)
        return binding!!.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.lifecycleOwner = this
        // ToolBar View Model
        forgotPassVM?.activity = activity
        binding!!.forgotPassVM = forgotPassVM

        initView()
    }

    private fun initView() {
        // check has Observers
        if (!forgotPassVM?.comman?.hasObservers()!!) {
            forgotPassVM?.comman?.observe(this, object : Observer<String> {
                override fun onChanged(@Nullable str: String) {
                    when (str) {
                        "send_password" -> onBackPressed()
                    }
                }
            })
        }
    }

}