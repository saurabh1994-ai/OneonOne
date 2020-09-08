package com.sws.oneonone.firebase_notification

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ChatNotificationModel {
    @SerializedName("multicast_id")
    @Expose
    val multicastId: Int? = null

    @SerializedName("success")
    @Expose
    val success: Int? = null

    @SerializedName("failure")
    @Expose
    val failure: Int? = null

    @SerializedName("results")
    @Expose
    val results: List<TokenResult>?  = null;

}
class TokenResult {
    @SerializedName("message_id")
    @Expose
    val messageId: String? = null
}