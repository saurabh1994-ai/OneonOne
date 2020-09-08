package com.sws.oneonone.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.sws.oneonone.R
import com.sws.oneonone.adapter.FollowersAdapter
import com.sws.oneonone.databinding.FragmentFollowersBinding
import com.sws.oneonone.model.ToolbarModel
import com.sws.oneonone.util.BaseFragment

class FollowersFragment: BaseFragment() {
    private var binding: FragmentFollowersBinding? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentFollowersBinding.bind(inflater.inflate(R.layout.fragment_followers, container, false))
        return binding!!.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbarModel = ToolbarModel(View.VISIBLE, getString(R.string.followers) ,View.GONE,View.GONE,View.GONE,View.GONE, 1, "","",View.GONE)
        binding!!.layoutToolbar.toolbarModel = toolbarModel
        binding!!.layoutToolbar.lifecycleOwner = activity


        binding!!.layoutToolbar.tvback.setOnClickListener {
            onBackPressed()
        }

        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL

        binding!!.rvFollowers.layoutManager = layoutManager
        binding!!.rvFollowers.adapter = FollowersAdapter()
    }
}