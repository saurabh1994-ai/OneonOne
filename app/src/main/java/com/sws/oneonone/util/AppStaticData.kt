package com.sws.oneonone.util

import com.google.gson.Gson
import com.sws.oneonone.model.SignUpModel

class AppStaticData {
    companion object {
        // Amazon Server
        var BUSKET_NAME = "wystap"
        val AWS_URL = "https://s3.amazonaws.com/wystap/"
        var POOL_ID = "us-east-1:53ac7085-42f0-41a5-8dbf-daa611a3d8db"

        // 1On1 App All Dala get from Shared Prefrences
        var pref: PreferenceStore? = null
        var model: SignUpModel? = null
        private var fmcToken: String? = null

        fun setFmcToken(token: String?) {
            fmcToken = token
        }
        fun getFmcToken(): String? {
            return fmcToken
        }


    }

    fun clearData(){
        pref =  PreferenceStore.getInstance()
        pref?.clearPrefrenceData()
    }

    fun APIToken(): String? {
        //API token
        pref = PreferenceStore.getInstance()
        val gson = Gson()
        val json = pref?.getStringData("OneOnOne")
        model = gson.fromJson(json, SignUpModel::class.java)
        val TOKEN: String? = "Bearer " + model?.result?.token
        return TOKEN
    }

    fun getModel(): SignUpModel? {
        pref = PreferenceStore.getInstance()
        val gson = Gson()
        val json = pref?.getStringData("OneOnOne")
        model = gson.fromJson(json, SignUpModel::class.java)
        return model
    }
}