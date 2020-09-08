package com.sws.oneonone.fragment

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.sws.oneonone.R
import com.sws.oneonone.adapter.ViewPagerAdapter
import com.sws.oneonone.databinding.FragmentHomeBinding
import com.sws.oneonone.dialog.NotificationDialog
import com.sws.oneonone.dialog.ProfileBottomDialog
import com.sws.oneonone.util.BaseFragment
import com.sws.oneonone.util.NotificationBarColor
import com.sws.oneonone.util.NotificationCenter
import com.sws.oneonone.util.Utils
import org.json.JSONObject

class HomeFragment: BaseFragment(){
    private var binding: FragmentHomeBinding? = null
    //Fragments
    var exploreFragment: ExploreFragment? = null
    var callsFragment: CallFragment? = null
    var cameraFragment: CameraUIFragment? = null
    var prevMenuItem: MenuItem? = null
    var currentPosition: Int = 1
    var adapter: ViewPagerAdapter? = null
    var uId = ""
    var isFromChallenge = false
    var ForVideo = false
    var notidata = ""
   var profileId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(uId.isNotEmpty() || uId.isNotBlank() || isFromChallenge){
            currentPosition = 2
        } else {
            currentPosition = 1
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        NotificationBarColor.WhiteNotificationBar(activity)
      binding = FragmentHomeBinding.bind(inflater.inflate(R.layout.fragment_home, container, false))


        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.bottomNavigation.itemIconTintList = null

        binding!!.bottomNavigation.menu.getItem(1).isChecked = true
        binding!!.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_chat -> {
                    binding!!.viewpager.currentItem = 0
                currentPosition = 0
                }
                R.id.action_camera  ->{
                    binding!!.viewpager.currentItem = 1
                    currentPosition = 1
                }
                R.id.action_explore -> {
                    binding!!.viewpager.currentItem = 2
                    currentPosition = 2
                }
            }
            false
        }

        binding!!.viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (prevMenuItem != null) {
                    prevMenuItem!!.isChecked = false
                } else {
                    binding!!.bottomNavigation.menu.getItem(0).isChecked = false
                }
                currentPosition = position
                Log.d("page", "onPageSelected: $position")
                binding!!.bottomNavigation.menu.getItem(position).isChecked = true
                prevMenuItem =  binding!!.bottomNavigation.menu.getItem(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        setupViewPager(binding!!.viewpager)

    }

    override fun onResume() {
        super.onResume()
       if (adapter != null) {
            adapter?.notifyDataSetChanged()
            binding!!.viewpager.currentItem = currentPosition
        }
    }

    private fun setupViewPager(viewPager: ViewPager?) {
        adapter = ViewPagerAdapter(childFragmentManager)
        callsFragment = CallFragment()
        exploreFragment = ExploreFragment.newInstance(uId,isFromChallenge,ForVideo)
        cameraFragment = CameraUIFragment.newInstance(profileId,notidata)
        adapter?.addFragment(callsFragment!!)
        adapter?.addFragment(cameraFragment!!)
        adapter?.addFragment(exploreFragment!!)
        viewPager!!.adapter = adapter
        viewPager.currentItem = currentPosition

    }


    override fun onBackPressed() {
        super.onBackPressed()
        activity.finish()
    }

    companion object {

        fun newInstance(uid: String?,notidata:String, profileId:String,isFromChallenge : Boolean, ForVideo: Boolean) = HomeFragment().apply {
            this.uId = uid!!
            this.isFromChallenge = isFromChallenge
            this.ForVideo = ForVideo
            this.notidata = notidata
            this.profileId = profileId


        }
    }






}