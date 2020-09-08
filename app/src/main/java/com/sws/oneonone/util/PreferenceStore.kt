package com.sws.oneonone.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceStore(context:Context) {
    val FB_ACCESS_TOKEN = "fbaccesstoken"
    val FB_USERID = "fbuserid"
    val FB_FIRSTNAME = "fbfirstname"
    val FB_NAME = "fbname"
    val FB_IMAGEURL = "fbimageurl"
    val FB_EMAIL = "fbemail"
    val FB_LASTNAME = "fblastname"
    private var editor:SharedPreferences.Editor? = null
    private var preferences:SharedPreferences? = null

    init{
        if (context != null)
        {
            preferences = context.getSharedPreferences(
                "OneOnOne", Context.MODE_PRIVATE)
            editor = preferences?.edit()

        }
    }
    fun saveStringData(key:String, value:String) {
        editor?.putString(key, value)
        editor?.apply()
    }
    fun saveBooleanData(key:String, value:Boolean) {
        editor?.putBoolean(key, value)
        editor?.apply()
    }
    fun saveIntegerData(key:String, value:Long) {
        editor?.putLong(key, value)
        editor?.apply()
    }
    fun getStringData(key:String): String? {
        if (preferences != null)
        {
            return preferences?.getString(key, "")!!
        }
        else
        {
            return null
        }
    }
    fun getBooleanData(key:String): Boolean? {
        if (preferences != null)
        {
            return preferences?.getBoolean(key, false)
        }
        else
        {
            return false
        }
    }
    fun getIntegerData(key:String): Long? {
        if (preferences != null)
        {
            return preferences?.getLong(key, 0)
        }
        else
        {
            return 0
        }
    }
    fun clearPrefrenceData() {
        preferences?.edit()?.clear()?.apply()
    }

    @Synchronized
    fun setFbAccessToken(json: String) {
        editor!!.putString(FB_ACCESS_TOKEN, json)
        editor!!.commit()
    }

    @Synchronized
    fun getFbAccessToken(): String? {
        return preferences?.getString(FB_ACCESS_TOKEN, null)
    }

    fun setFbUserId(fbUserId: String) {
        editor!!.putString(FB_USERID, fbUserId)
        editor!!.commit()
    }

    fun getFbUserId(): String? {
        return preferences?.getString(FB_USERID, null)
    }

    fun getFbFirstName(): String? {
        return preferences?.getString(FB_FIRSTNAME, null)
    }

    @Synchronized
    fun setFbFirstName(fbFirstName: String) {
        editor!!.putString(FB_FIRSTNAME, fbFirstName)
        editor!!.commit()
    }

    fun getFbName(): String? {
        return preferences?.getString(FB_NAME, null)
    }

    fun setFbName(fbName: String) {
        editor!!.putString(FB_NAME, fbName)
        editor!!.commit()
    }

    fun getFbImageUrl(): String? {
        return preferences?.getString(FB_IMAGEURL, null)
    }

    fun setFbImageUrl(fbImageUrl: String) {
        editor!!.putString(FB_IMAGEURL, fbImageUrl)
        editor!!.commit()
    }

    fun getFbEmail(): String? {
        return preferences?.getString(FB_EMAIL, null)
    }

    fun setFbEmail(fbEmail: String) {
        editor!!.putString(FB_EMAIL, fbEmail)
        editor!!.commit()
    }

    fun getFbLastName(): String? {
        return preferences?.getString(FB_LASTNAME, null)
    }

    fun setFbLastName(fbLastName: String) {
        editor!!.putString(FB_LASTNAME, fbLastName)
        editor!!.commit()
    }


    companion object {
        private var instance: PreferenceStore? = null
        @Synchronized fun getInstance():PreferenceStore {
            instance = if (instance == null) PreferenceStore(ApplicationLoader.applicationContext!!) else instance
            return instance!!
        }
        @Synchronized fun getInstance(context:Context):PreferenceStore {
            return PreferenceStore(context)
        }
    }
}