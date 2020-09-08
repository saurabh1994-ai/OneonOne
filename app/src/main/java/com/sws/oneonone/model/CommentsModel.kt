package com.sws.oneonone.model

class CommentsModel(
    val code:Int,
    val message:String,
    val result: ArrayList<CommentResultModel>,
    val viewsCount: Int,
    val commentsCount: Int,
    val comments : Int,
    var likesCount: Int,
    val pageNumber:Int
)

class CommentResultModel(
    val comment :String,
    val _id :String,
    val userId: UserDataModel

)









