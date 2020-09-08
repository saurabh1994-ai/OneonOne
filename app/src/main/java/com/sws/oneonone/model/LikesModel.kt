package com.sws.oneonone.model

class LikesModel (
    val code:Int,
    val message:String,
    val result : ArrayList<LikesDataModel>

)

class LikesDataModel(
    val uid :String,
    val _id:String,
    val userId: UserDataModel?,
    val createdAt : String
)

