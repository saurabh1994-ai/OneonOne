package com.sws.oneonone.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class DeviceModel {
    @SerializedName("device_type")
    @Expose
    var deviceType: String? = null

    @SerializedName("device_token")
    @Expose
    var deviceToken: String? = null

    @SerializedName("_id")
    @Expose
    var id: String? = null

    @SerializedName("device_id")
    @Expose
    var deviceId: String? = null

}