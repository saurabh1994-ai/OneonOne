package com.sws.oneonone.model

class ViewExploreModel (
    val code:Int,
    val message:String,
    val result : ArrayList<ViewResultModel>,
    var position: Int
)

// isOpenChallenge, isStartVote, expireRespondTime, isAccepted
class ViewResultModel(
    val hashtagIds : ArrayList<String>,
    val hashtags : ArrayList<String>,
    val userTo : ArrayList<String>,
    val uid : String,
    val title: String,
    val image:String,
    val video:String,
    val thumbnails:String,
    val type: String,
    val userId : UserDataModel,
    val count: Int,
    val likes: Int,
    val comments : Int,
    var votes: Int,
    val _id:String,
    val active:Boolean,
    var isVote: Boolean,
    val createdAt :String,
    val createdBy :String,
    val request:String,
    var viewCount:Int,
    var isView:Boolean,
    var isLike :Boolean,
    var isOpenChallenge :Boolean,
    var isStartVote :Boolean,
    var expireRespondTime :Boolean,
    var isAccepted :Boolean
)

class UserDataModel(
    var username : String,
    var profileImg : String,
    var _id : String,
    var createdAt:String,
    var name:String

)



