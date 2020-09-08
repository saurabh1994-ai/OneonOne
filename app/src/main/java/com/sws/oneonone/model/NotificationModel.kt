package com.sws.oneonone.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class NotificationModel {
    @SerializedName("code")
    @Expose
    var code: Int? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("result")
    @Expose
    var result: ArrayList<NotificationResult>? = null

    @SerializedName("count")
    @Expose
    var count: Int? = null

}