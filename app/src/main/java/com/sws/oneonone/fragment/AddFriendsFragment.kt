package com.sws.oneonone.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.sws.oneonone.R
import com.sws.oneonone.adapter.AddFriendsAdapter
import com.sws.oneonone.databinding.FragmentAddChallengersBinding
import com.sws.oneonone.util.BaseFragment

class AddFriendsFragment: BaseFragment() {

    private var binding: FragmentAddChallengersBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentAddChallengersBinding.bind(inflater.inflate(R.layout.fragment_add_friends, container, false))
        return binding!!.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager: LinearLayoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding!!.rvAddChallenges.layoutManager = layoutManager
        binding!!.rvAddChallenges.adapter = AddFriendsAdapter(activity!!)

    }
}
