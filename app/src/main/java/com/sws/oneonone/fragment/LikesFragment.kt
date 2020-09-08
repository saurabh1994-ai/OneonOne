package com.sws.oneonone.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.adapter.LikesAdapter
import com.sws.oneonone.adapter.ViewExploreProfileAdapter
import com.sws.oneonone.databinding.FragmentLikesBinding
import com.sws.oneonone.model.*
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.util.*
import com.sws.oneonone.viewModel.ToolbarVM
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class LikesFragment: BaseFragment() {
    private var binding: FragmentLikesBinding? = null
    var toolbarVM: ToolbarVM? = null
    var uid = ""
    var service: ApiClient? = ApiClient()
    var likesAdapter:LikesAdapter? = null
    var isLastPage: Boolean = false
    var isLoading: Boolean = false
    var pageNo = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarVM = ViewModelProviders.of(this).get(ToolbarVM::class.java)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;//  set status text dark
        activity.window.statusBarColor = ContextCompat.getColor(
            activity,
            R.color.black
        );// set status background white
        binding = FragmentLikesBinding.bind(
            inflater.inflate(
                R.layout.fragment_likes,
                container,
                false
            )
        )
        binding!!.lifecycleOwner = this

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbarModel: ToolbarModel = ToolbarModel(View.VISIBLE,getString(R.string.likes), View.GONE,View.VISIBLE,View.VISIBLE,View.GONE, 1, "","",View.VISIBLE)
        binding!!.layoutToolbar.toolbarModel = toolbarModel
        binding!!.layoutToolbar.lifecycleOwner = this

        toolbarVM?.activity = activity
        binding!!.layoutToolbar.toolbarVM = toolbarVM
        getLikes(pageNo)

        val layoutManager1 = LinearLayoutManager(activity)
        layoutManager1.orientation = LinearLayoutManager.VERTICAL
        likesAdapter = LikesAdapter()
        binding!!.rvLikes.layoutManager = layoutManager1
        binding!!.rvLikes.adapter = likesAdapter
        binding!!.rvLikes.addOnScrollListener(object : PaginationScrollListener(layoutManager1) {
            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

            override fun loadMoreItems() {
                isLoading = true
                pageNo++
                //you have to call loadmore items to get more data
                getLikes(pageNo)

            }
        })

        initView()


    }

    private fun initView() {
        // check has Observers
        if (!toolbarVM?.comman?.hasObservers()!!) {
            toolbarVM?.comman?.observe(this, object : Observer<String> {
                override fun onChanged(@Nullable str: String) {
                    when (str){
                        "onBack" -> onBackPressed()
                    }
                }
            })
        }

    }


    fun getLikes(pageNo : Int){
        try {
            val rootObject = JsonObject()


            try {
                rootObject.addProperty("uid", uid)
                rootObject.addProperty("pageNumber",  pageNo)

            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.getLikes(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<LikesModel> {
                override fun onResponse(call: Call<LikesModel>, response: Response<LikesModel>) {
                    if (response.code() == 200) {
                        try {
                          val   likesModel = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity!!,likesModel.code!!,likesModel.message)
                            if(checkResponse) {
                                if (likesModel.code == 200) {

                                    getMoreItems(likesModel)

                                } else {
                                    Utils.showToast(activity, likesModel.message, false)
                                }
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
                override fun onFailure(call: Call<LikesModel>, t: Throwable) {
                    Log.e("responseError", t.message)
                    Utils.showToast(activity, "Have some problem...", false)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    companion object {

        fun newInstance( uid :String) = LikesFragment().apply {

            this.uid = uid

        }
    }

    fun getMoreItems(likesModel : LikesModel) {
        //after fetching your data assuming you have fetched list in your
        // recyclerview adapter assuming your recyclerview adapter is
        //rvAdapter
        //  after getting your data you have to assign false to isLoading
        isLoading = false

        if(pageNo == 1)
            likesAdapter!!.setLikesAdapter(activity,likesModel.result,true)

        else {
            likesAdapter!!.addData(likesModel.result)
        }




    }
}