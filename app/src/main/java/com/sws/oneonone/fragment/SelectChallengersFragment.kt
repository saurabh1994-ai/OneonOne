package com.sws.oneonone.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.sws.oneonone.R
import com.sws.oneonone.adapter.SelectedChallengersAdapter
import com.sws.oneonone.databinding.FragmentSelectChallengersBinding
import com.sws.oneonone.model.MyFollowingResult
import com.sws.oneonone.model.ToolbarModel
import com.sws.oneonone.util.BaseFragment
import com.sws.oneonone.util.CreateChallengerData
import com.sws.oneonone.viewModel.SelectChallengerVM
import com.sws.oneonone.viewModel.ToolbarVM
import java.util.ArrayList

class SelectChallengersFragment: BaseFragment() {

    private var binding: FragmentSelectChallengersBinding? = null
    var toolbarVM: ToolbarVM? = null
    var challengerVM: SelectChallengerVM? = null
    var adapter: SelectedChallengersAdapter? =  null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // NotificationBar change backgraound/text color
        toolbarVM = ViewModelProviders.of(this).get(ToolbarVM::class.java)
        challengerVM = ViewModelProviders.of(this).get(SelectChallengerVM::class.java)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSelectChallengersBinding.bind(inflater.inflate(R.layout.fragment_select_challengers, container, false))
        binding!!.lifecycleOwner = this
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tool bar model
        val toolbarModel = ToolbarModel(View.VISIBLE,"Challengers", View.GONE,View.VISIBLE,View.VISIBLE,View.GONE, 1, "DONE","",View.GONE)
        binding!!.layoutToolbar.toolbarModel = toolbarModel
        binding!!.layoutToolbar.lifecycleOwner = this

        toolbarVM?.activity = activity
        binding!!.layoutToolbar.toolbarVM = toolbarVM

        challengerVM?.activity = activity
        binding!!.selectChallengeVM = challengerVM

        // Call API function
        challengerVM?.onChallengeClick()

        initView()
    }

    private fun initView() {
        // check has Observers
        if (!toolbarVM?.comman?.hasObservers()!!) {
            toolbarVM?.comman?.observe(this, object : Observer<String> {
                override fun onChanged(@Nullable str: String) {
                    when (str) {
                        "onBack" -> onBackPressed()
                        "next" -> {
                            if (adapter != null){
                                onBackPressed()

                            }
                        }
                    }
                }
            })
        }

        // Add Challenger View Model
        if (!challengerVM?.comman?.hasObservers()!!) {
            challengerVM?.comman?.observe(this, object : Observer<ArrayList<MyFollowingResult>> {
                override fun onChanged(@Nullable list: ArrayList<MyFollowingResult>) {
                    val layoutManager: LinearLayoutManager = LinearLayoutManager(activity)
                    layoutManager.orientation = LinearLayoutManager.VERTICAL
                    binding!!.rvAddChallenges.layoutManager = layoutManager
                    adapter = SelectedChallengersAdapter()
                    adapter!!.setSelectedAdapter(activity, list)
                    binding!!.rvAddChallenges.adapter = adapter
                }
            })
        }
    }
}