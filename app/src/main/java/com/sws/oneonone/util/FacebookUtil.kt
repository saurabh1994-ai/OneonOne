package com.sws.oneonone.util

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.facebook.*
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class FacebookUtil(context: Activity, isFriend: Boolean){
    private var facebookUtil: FacebookUtil? = null
    private var contexts: Activity? = null
    private var isFriends: Boolean = false
    private var fbUserIdForFriends: String? = null
    private var friendListener: FrienListGetSuccessfull? = null
    var preferenceStore:PreferenceStore? = null


    init {

        contexts = context
        isFriends = isFriend
        preferenceStore =  PreferenceStore.getInstance()
    }

    interface LoginFaceBookListener {

        fun loginWithFaceBook()
    }

    interface FrienListGetSuccessfull {

        fun getFrienListData(jsonObject: JSONObject)
    }

    private var loginFaceBookListener: LoginFaceBookListener? = null

    fun setListener(loginFaceBookListener: LoginFaceBookListener) {
        this.loginFaceBookListener = loginFaceBookListener
    }

    fun setFriendListener(friendListener: FrienListGetSuccessfull) {

        this.friendListener = friendListener
    }

    fun getInstance(context: Activity, isFriend: Boolean): FacebookUtil {
       contexts = context
        isFriends = isFriend
        if (facebookUtil == null)
            facebookUtil = FacebookUtil(contexts!!,isFriends)
        return facebookUtil!!
    }

    fun onAuth(callbackManager: CallbackManager) {

        authenticate(callbackManager)
    }

    private fun getAllFriendsList() {
        GraphRequest(
            AccessToken.getCurrentAccessToken(),
            "/$fbUserIdForFriends/friends", null,
            HttpMethod.GET,
            GraphRequest.Callback { response ->
                Log.e("FaceBookUserId", "LoginresponseInfo  $response")
                try {
                    val jsonObject = JSONObject(response.jsonObject.toString())
                    if (friendListener != null)
                        friendListener!!.getFrienListData(jsonObject)
                } catch (e: Exception) {

                    e.printStackTrace()
                }
            }
        ).executeAsync()
    }



    fun getAccessToken(): AccessToken? {

        val accessToken: AccessToken? = null
        try {
            val json = preferenceStore!!.getFbAccessToken()
            return if (json == null) null else Gson().fromJson<AccessToken>(
                json,
                AccessToken::class.java
            )


        } catch (e: Exception) {
            e.printStackTrace()
            return accessToken
        }

    }


    private fun saveAccessToken(accessToken: AccessToken?) {
        try {
            val json = if (accessToken == null) null else Gson().toJson(accessToken)
            preferenceStore!!.setFbAccessToken(json!!)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }



    private fun authenticate(callbackManager: CallbackManager) {
        LoginManager.getInstance().logInWithReadPermissions(
            contexts,
            listOf(
                "public_profile", "email"
            )
        )
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    println("rohit$loginResult")
                    val request = GraphRequest.newMeRequest(
                        loginResult.accessToken
                    ) { `object`, _ ->
                        try {
                            var name: String? = null
                            var email: String? = null
                            var id: String? = null
                            var firstName:String?= null
                            var lastName:String? = null
                            if (`object` != null) {
                                if (`object`.has("name"))
                                    name = `object`.getString("name")

                                if (`object`.has("email"))
                                    email = `object`.getString("email")
                                if (`object`.has("id"))
                                    id = `object`.getString("id")
                                if (`object`.has("first_name"))
                                    firstName = `object`.getString("first_name")
                                if (`object`.has("first_name"))
                                    lastName = `object`.getString("last_name")

                                if (!isFriends)
                                    storeFacebookIdToPreferences(name!!, email!!, id!!,firstName!!,lastName!!)
                                else
                                    fbUserIdForFriends = id
                                Log.e("facebook", name + email + id + "")
                            }

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                        if (!isFriends) {
                            val accessToken = AccessToken
                                .getCurrentAccessToken()
                            if (loginFaceBookListener != null) {
                                loginFaceBookListener!!.loginWithFaceBook()
                            }
                            saveAccessToken(accessToken)
                        } else {

                            getAllFriendsList()
                        }
                    }
                    val parameters = Bundle()
                    parameters.putString("fields", "id,email,name,first_name,last_name,gender")
                    request.parameters = parameters
                    request.executeAsync()
                }

                override fun onCancel() {

                    Log.e("facebook login", "cancel")
                }

                override fun onError(exception: FacebookException) {
                    exception.printStackTrace()
                    Log.e("facebook login", "cancel")

                }
            })
    }

    private fun storeFacebookIdToPreferences(name: String, email: String, id: String, firstName:String, lastName:String) {


        preferenceStore!!.setFbUserId(id)
        preferenceStore!!.setFbFirstName(firstName)
        preferenceStore!!.setFbName(name)
        preferenceStore!!.setFbImageUrl("https://graph.facebook.com/$id/picture?width=640&height=640")
        preferenceStore!!.setFbEmail(email)
        preferenceStore!!.setFbLastName(lastName)

        Log.e("facebook", name + email + id + "")

    }

    fun logout() {
        try {
            LoginManager.getInstance().logOut()
            preferenceStore!!.setFbAccessToken("")
          //  saveAccessToken(null)
        } catch (e: Exception) {

        }

    }







}
