package com.sws.oneonone.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class ProfileChallengeModel {
    @SerializedName("code")
    @Expose
    var code: Int? = null

    @SerializedName("userProfile")
    @Expose
    var userProfile: SignUpResultModel? = null

    @SerializedName("result")
    @Expose
    var result: ArrayList<ProfileChallengeResult>? = null
}