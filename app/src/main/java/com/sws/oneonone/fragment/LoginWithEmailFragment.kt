package com.sws.oneonone.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.sws.oneonone.R
import com.sws.oneonone.databinding.FragmentLoginBinding
import com.sws.oneonone.util.BaseFragment
import com.sws.oneonone.viewModel.LoginVM

class LoginWithEmailFragment: BaseFragment(){
    private var binding: FragmentLoginBinding? = null
    var loginVM: LoginVM? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginVM = ViewModelProviders.of(this).get(LoginVM::class.java)

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLoginBinding.bind(inflater.inflate(R.layout.fragment_login, container, false))
        binding!!.lifecycleOwner = this

        return binding!!.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.loginVM = loginVM
        loginVM?.activity = activity

        initView()
    }


    private fun initView() {
        // check has Observers
        if (!loginVM?.comman?.hasObservers()!!) {
            loginVM?.comman?.observe(this, object : Observer<String> {
                override fun onChanged(@Nullable str: String) {
                    when (str) {
                        "back" -> onBackPressed()
                        "sign_up" -> activity.replaceFragment(SignupFragment())
                        "forgot_password" -> activity.replaceFragment(ForgotPasswordFragment())
                        "next_ui" -> {
                            onBackPressed()
                            activity.replaceFragment(HomeFragment())
                        }
                    }
                }
            })
        }
    }


}