package com.sws.oneonone.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class MyFollowingModel {
    @SerializedName("code")
    @Expose
    var code: Int? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("result")
    @Expose
    var result: ArrayList<MyFollowingResult>? = null

    @SerializedName("followings")
    @Expose
    var followings: ArrayList<MyFollowingResult>? = null

    @SerializedName("followers")
    @Expose
    val followers:ArrayList<MyFollowingResult>? = null

}
