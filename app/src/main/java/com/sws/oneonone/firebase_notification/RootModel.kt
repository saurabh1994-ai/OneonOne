package com.sws.oneonone.firebase_notification

import com.google.gson.annotations.SerializedName
import com.sws.oneonone.model.NotificationModel


class RootModel {
    @SerializedName("to") //  "to" changed to token
    private var token: String? = null

    @SerializedName("notification")
    private var notification: NotiModel? = null

    fun RootModel(
        token: String?,
        notification: NotiModel?
       // data: DataModel?
    ) {
        this.token = token
        this.notification = notification
       // this.data = data
    }

    fun getToken(): String? {
        return token
    }

    fun setToken(token: String?) {
        this.token = token
    }

    fun getNotification(): NotiModel? {
        return notification
    }

    fun setNotification(notification: NotiModel?) {
        this.notification = notification
    }

}