package com.sws.oneonone.model

class NotificationResponseModel (
    val code : Int,
    val message :String,
    val result : ArrayList<NotificationResultModel>

)


class  NotificationResultModel(
    val challengeName : String,
    val challengeImage : String,
    val profileImg : String,
    val likename :String,
    val name : String,
    val request : String,
    val uid : String,
    var message : String,
    val hashtags : ArrayList<String>,
    val _id : String,
    val senderId : NotificationSenderId ?,
    val type : String,
    val expireRespondTime : Boolean,
    val respondTime : String,
    var challengeResponse : String,
    val createdAt : String,
    val challengeId : ChallengeIdModel,
    val isAccepted : Boolean,
    var challengeView : Boolean,
    var isFollow : Boolean


)

class NotificationSenderId(
    var fullName : String,
    val username : String,
    val profileImg : String?,
    val _id : String
)



