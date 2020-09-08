package com.sws.oneonone.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.Player
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.adapter.NotificationAdapter
import com.sws.oneonone.databinding.FragmentNotificationBinding
import com.sws.oneonone.model.*
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.service.VideoPreLoadingService
import com.sws.oneonone.util.*
import com.sws.oneonone.viewModel.ToolbarVM
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class NotificationFragment: BaseFragment() {
    private var binding: FragmentNotificationBinding? = null
    var toolbarVM: ToolbarVM? = null
    var isLastPage: Boolean = false
    var isLoading: Boolean = false
    var pageNo = 1
    var notificationAdapter: NotificationAdapter? = null
    var service: ApiClient? = ApiClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarVM = ViewModelProviders.of(this).get(ToolbarVM::class.java)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;//  set status text dark
        activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.white);// set status background white
        binding = FragmentNotificationBinding.bind(inflater.inflate(R.layout.fragment_notification, container, false))
        binding!!.lifecycleOwner = this
        pageNo = 1
        YourNotificationData(pageNo)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Tool bar model
        val toolbarModel = ToolbarModel(View.VISIBLE,"Notification", View.GONE,View.VISIBLE,View.VISIBLE,View.GONE, 1, "","",View.GONE)
        binding!!.layoutToolbar.toolbarModel = toolbarModel
        binding!!.layoutToolbar.lifecycleOwner = this

        toolbarVM?.activity = activity
        binding!!.layoutToolbar.toolbarVM = toolbarVM

        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL

        binding!!.rvNotification.layoutManager = layoutManager
        notificationAdapter = NotificationAdapter()
        binding!!.rvNotification.adapter = notificationAdapter

        binding!!.rvNotification.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

            override fun loadMoreItems() {
                isLoading = true
                pageNo++
                Log.d("pageNo+++++",pageNo.toString())
                //you have to call loadmore items to get more data
                YourNotificationData(pageNo)

            }
        })
       initView()

    }

    fun refreshData() {
        pageNo = 1
        isLoading = true
        YourNotificationData(pageNo)
    }

    private fun initView() {
        // check has Observers
        if (!toolbarVM?.comman?.hasObservers()!!) {
            toolbarVM?.comman?.observe(this, Observer { str ->
                when (str){
                    "onBack" -> onBackPressed()
                }
            })
        }
    }


    fun YourNotificationData(page: Int) {
        try {
            val rootObject = JsonObject()
            try {
                rootObject.addProperty("userId",  AppStaticData().getModel()?.result?.id)
                rootObject.addProperty("pageNumber",  page)
            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.getAllNotifications(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<NotificationResponseModel> {
                override fun onResponse(call: Call<NotificationResponseModel>, response: Response<NotificationResponseModel>) {
                    if (response.code() == 200) {
                        try {
                            val mNotificationListl = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity!!,mNotificationListl.code!!,mNotificationListl.message)
                            if(checkResponse){
                                if (mNotificationListl.code == 200) {
                                    Log.d("pgeNumber===", page.toString())
                                    if (!mNotificationListl.result.isNullOrEmpty()) {

                                        getMoreItems(mNotificationListl.result)

                                    }

                                }
                            }
                            else {
                                Utils.showToast(activity, mNotificationListl.message, false)
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
                override fun onFailure(call: Call<NotificationResponseModel>, t: Throwable) {
                    Log.e("responseError", t.message)
                    Utils.showToast(activity, "Have some problem...", false)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun getMoreItems(list : ArrayList<NotificationResultModel>) {
        //after fetching your data assuming you have fetched list in your
        // recyclerview adapter assuming your recyclerview adapter is
        //rvAdapter
        //  after getting your data you have to assign false to isLoading
        isLoading = false

        if(pageNo == 1){
            notificationAdapter!!.setNotificationAdapter(activity,list)
        }else{
            notificationAdapter!!.addData(list)
        }



    }


   /* override fun onResume() {
        super.onResume()
        YourNotificationData(1)

    }*/

}