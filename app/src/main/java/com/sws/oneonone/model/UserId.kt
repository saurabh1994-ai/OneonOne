package com.sws.oneonone.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class UserId {
    @SerializedName("username")
    @Expose
    var userName: String? = null
 @SerializedName("profileImg")
    @Expose
    var profileImg: String? = null

    @SerializedName("_id")
    @Expose
    var id: String? = null

}