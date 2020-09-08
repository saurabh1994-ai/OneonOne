package com.sws.oneonone.model



class CareateChallengeModel (
    val code:Int,
    val message:String,
    val result : challengeResultModel)

class challengeResultModel(
    val hashtagIds : ArrayList<String>,
    val hashtags : ArrayList<String>,
    val userTo : ArrayList<String>,
    val uid : String,
    val title: String
)

