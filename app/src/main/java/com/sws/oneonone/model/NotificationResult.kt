package com.sws.oneonone.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class
NotificationResult {
    private var notificationResult: NotificationResult? = null

    @SerializedName("deviceToken")
    @Expose
    var deviceToken: String? = null

    @SerializedName("challengeName")
    @Expose
    var challengeName: String? = null


    @SerializedName("challengeImage")
    @Expose
    var challengeImage: String? = null

    @SerializedName("profileImg")
    @Expose
    var profileImg: String? = null

    @SerializedName("likename")
    @Expose
    var likename: String? = null

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("request")
    @Expose
    var request: String? = null

    @SerializedName("uid")
    @Expose
    var uid: String? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("respondTime")
    @Expose
    var respondTime: String? = null

    @SerializedName("isFollow")
    @Expose
    var isFollow: Boolean? = null

    @SerializedName("isView")
    @Expose
    var isView: Boolean? = null

    @SerializedName("type")
    @Expose
    var type: Int? = null

    @SerializedName("active")
    @Expose
    var active: Boolean? = null

    @SerializedName("hashtags")
    @Expose
    var hashtags: ArrayList<String>? = null

    @SerializedName("expireRespondTime")
    @Expose
    var expireRespondTime: Boolean? = null

    @SerializedName("challengeId")
    @Expose
    var challengeId: ChallengeIdModel? = null

    @SerializedName("_id")
    @Expose
    var id: String? = null

    @SerializedName("senderId")
    @Expose
    var senderId: SenderIdModel? = null

    @SerializedName("recieverId")
    @Expose
    var recieverId: String? = null

    @SerializedName("userId")
    @Expose
    var userId: String? = null

    @SerializedName("createdAt")
    @Expose
    var createdAt: String? = null

    @SerializedName("__v")
    @Expose
    var v: Int? = null

    var challengeResponse: String? = ""

    fun setNotificationResult(notiResult: NotificationResult?){
        notificationResult = notiResult
    }

    fun getNotificationResult(): NotificationResult? {
        return notificationResult
    }
}
