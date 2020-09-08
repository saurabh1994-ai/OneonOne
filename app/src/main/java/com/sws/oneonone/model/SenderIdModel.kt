package com.sws.oneonone.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SenderIdModel {
    @SerializedName("fullName")
    @Expose
    var fullName: String? = null

    @SerializedName("username")
    @Expose
    var username: String? = null

    @SerializedName("email")
    @Expose
    var email: String? = null

    @SerializedName("gender")
    @Expose
    var gender: String? = null

    @SerializedName("profileImg")
    @Expose
    var profileImg: String? = null

    @SerializedName("firebaseCount")
    @Expose
    var firebaseCount: Int? = null

    @SerializedName("password")
    @Expose
    var password: String? = null

    @SerializedName("sessionId")
    @Expose
    var sessionId: String? = null

    @SerializedName("socialType")
    @Expose
    var socialType: String? = null

    @SerializedName("socialId")
    @Expose
    var socialId: String? = null

    @SerializedName("points")
    @Expose
    var points: Int? = null

    @SerializedName("followers")
    @Expose
    var followers: Int? = null

    @SerializedName("following")
    @Expose
    var following: Int? = null

    @SerializedName("winChallenge")
    @Expose
    var winChallenge: Int? = null

    @SerializedName("challenge")
    @Expose
    var challenge: Int? = null

    @SerializedName("votes")
    @Expose
    var votes: Int? = null

    @SerializedName("createdChallenges")
    @Expose
    var createdChallenges: Int? = null

    @SerializedName("acceptedChallenges")
    @Expose
    var acceptedChallenges: Int? = null

    @SerializedName("notificationStatus")
    @Expose
    var notificationStatus: String? = null

    @SerializedName("phoneNumber")
    @Expose
    var phoneNumber: String? = null

    @SerializedName("countryCode")
    @Expose
    var countryCode: String? = null

    @SerializedName("otp")
    @Expose
    var otp: String? = null

    @SerializedName("numberVerify")
    @Expose
    var numberVerify: Boolean? = null

    @SerializedName("active")
    @Expose
    var active: Boolean? = null

    @SerializedName("delete")
    @Expose
    var delete: Boolean? = null

    @SerializedName("token")
    @Expose
    var token: String? = null

    @SerializedName("isFollow")
    @Expose
    var isFollow: Boolean? = null

    @SerializedName("isPrivate")
    @Expose
    var isPrivate: Boolean? = null

    @SerializedName("_id")
    @Expose
    var id: String? = null

    @SerializedName("devices")
    @Expose
    var devices: List<DeviceModel>? = null

    @SerializedName("createdAt")
    @Expose
    var createdAt: String? = null

    @SerializedName("__v")
    @Expose
    var v: Int? = null

}