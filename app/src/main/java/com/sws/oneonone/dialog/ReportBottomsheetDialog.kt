package com.sws.oneonone.dialog

import android.app.Dialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.core.Repo
import com.google.gson.JsonObject
import com.sws.oneonone.R
import com.sws.oneonone.databinding.DialogReportBottomsheetBinding
import com.sws.oneonone.fragment.VideoPreviewFragment
import com.sws.oneonone.model.ExploreResult
import com.sws.oneonone.model.SuccessModel
import com.sws.oneonone.retrofit.ApiClient
import com.sws.oneonone.retrofit.ApiInterface
import com.sws.oneonone.util.AndroidUtilities
import com.sws.oneonone.util.AppStaticData
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.Utils
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ReportBottomsheetDialog : BottomSheetDialogFragment(){
    var binding: DialogReportBottomsheetBinding? = null
    var videoUri : Uri? = null
    var image: Bitmap? = null
    var exploreResult : ExploreResult? = null
    var service: ApiClient? = ApiClient()
    val TAG = "ActionBottomDialog"
    var reportedId = ""
    var challengeId = ""
    var activity:BaseActivity? = null




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.bind(inflater.inflate(R.layout.dialog_report_bottomsheet, container, false))
        activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding!!.spam.setOnClickListener {
            reportUserAsyncTask(this, binding!!.spam.text.toString().trim()).execute()
        }

        binding!!.appropriate.setOnClickListener {
            reportUserAsyncTask(this, binding!!.appropriate.text.toString().trim()).execute()
        }


    }




    companion object {


        fun newInstance(activity: BaseActivity,reportedId : String?, challengeId: String?) = ReportBottomsheetDialog().apply {

             this.activity = activity
            this.reportedId = reportedId!!
            this.challengeId = challengeId!!

        }
    }


    fun reportUser(reason : String){
        try {
            val rootObject = JsonObject()

            try {
                rootObject.addProperty("reportedBy",AppStaticData().getModel()?.result?.id)
                rootObject.addProperty("reportedTo",reportedId)
                rootObject.addProperty("challengeId",challengeId)
                rootObject.addProperty("region",  reason)

            } catch (e: JSONException) {
                Log.e("tagg", e.message)
            }
            val apiService = service!!.getClient(false,"").create(ApiInterface::class.java)
            val call = apiService.reportUser(AppStaticData().APIToken(), rootObject)
            call.enqueue(object : Callback<SuccessModel> {
                override fun onResponse(call: Call<SuccessModel>, response: Response<SuccessModel>) {
                    if (response.code() == 200) {
                        try {
                            val ViewData = response.body()!!
                            val checkResponse =  AndroidUtilities().apisResponse(activity!!,ViewData.code!!,ViewData.message)
                            if(checkResponse) {
                                if (ViewData.code == 200) {
                                    AndroidUtilities().openAlertDialog(ViewData.message, activity!!)

                                }
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
                override fun onFailure(call: Call<SuccessModel>, t: Throwable) {
                    Log.e("responseError", t.message)
                    Utils.showToast(activity!!, "Have some problem...", false)
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    internal class reportUserAsyncTask(val fragment: ReportBottomsheetDialog,val reason: String): AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params:String):String {
            // your async action
            fragment.reportUser(reason)
            return ""
        }
        override fun onPostExecute(aVoid:String) {

        }
    }


}