package com.sws.oneonone.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class ProfileChallengeResult {
    private var profileChallenge: ProfileChallengeResult? = null

    @SerializedName("userTo")
    @Expose
    var userTo: List<String>? = null

    @SerializedName("uid")
    @Expose
    var uid: String? = null

    @SerializedName("title")
    @Expose
    var title: String? = null

    @SerializedName("image")
    @Expose
    var image: String? = null


    @SerializedName("thumbnails")
    @Expose
    var thumbnails: String? = null
    @SerializedName("video")
    @Expose
    var video: String? = null

    @SerializedName("active")
    @Expose
    var active: Boolean? = null

    @SerializedName("challengeResult")
    @Expose
    var challengeResult: Int? = null

    @SerializedName("_id")
    @Expose
    var id: String? = null

    @SerializedName("userId")
    @Expose
    var userId: String? = null

    @SerializedName("isOpenChallenge")
    @Expose
    var isOpenChallenge: Boolean? = null

    @SerializedName("createdAt")
    @Expose
    var createdAt: String? = null

    @SerializedName("createdBy")
    @Expose
    var createdBy: CreatedBy? = null

    fun setProfileChallenge(challenge: ProfileChallengeResult?){
        profileChallenge = challenge
    }
    fun getProfileChallenge(): ProfileChallengeResult? {
        return profileChallenge
    }
}

class CreatedBy {
    @SerializedName("profileImg")
    @Expose
    var profileImg: String? = null

    @SerializedName("_id")
    @Expose
    var id: String? = null
}