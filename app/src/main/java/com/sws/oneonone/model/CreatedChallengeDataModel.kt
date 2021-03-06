package com.sws.oneonone.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CreatedChallengeDataModel{
     @SerializedName("code")
     @Expose
     var code: Int? = null

     @SerializedName("result")
     @Expose
     var result: ArrayList<ArrayList<ExploreResult>>? = null
 }