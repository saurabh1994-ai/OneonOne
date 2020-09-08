package com.sws.oneonone.model

class HastagModel (
    val code:Int,
    val message : String,
    val result : ArrayList<HastagResult>

)

class HastagResult(
    val hashtagCount : Long,
    val hashtag : String
)
