package com.sws.oneonone.model

class VideoLInkModel (
    val success: Boolean,
    val errors : ArrayList<errors>,
    val messages:ArrayList<errors>,
    val result: LinkResult
)

class LinkResult(
    val uploadURL:String,
    val uid:String
)

class errors(
    val code: Int,
    val message:String
)