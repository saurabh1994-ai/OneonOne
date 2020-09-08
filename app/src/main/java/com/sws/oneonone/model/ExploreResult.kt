package com.sws.oneonone.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class ExploreResult {
    private var exploreData: ExploreResult? = null

    @SerializedName("hashtagIds")
    @Expose
    var hashtagIds: List<String>? = null

    @SerializedName("hashtags")
    @Expose
    var hashtags: List<String>? = null

    @SerializedName("userTo")
    @Expose
    var userTo: List<String>? = null

    @SerializedName("deniedBy")
    @Expose
    var deniedBy: List<Any>? = null

    @SerializedName("acceptedBy")
    @Expose
    var acceptedBy: List<String>? = null

    @SerializedName("uid")
    @Expose
    var uid: String? = null

    @SerializedName("title")
    @Expose
    var title: String? = null

    @SerializedName("image")
    @Expose
    var image: String? = null

    @SerializedName("video")
    @Expose
    var video: String? = null

    @SerializedName("thumbnails")
    @Expose
    var thumbnails: String? = null

    @SerializedName("type")
    @Expose
    var type: String? = null

    @SerializedName("active")
    @Expose
    var active: Boolean? = null

    @SerializedName("delete")
    @Expose
    var delete: Boolean? = null

    @SerializedName("request")
    @Expose
    var request: String? = null

    @SerializedName("votes")
    @Expose
    var votes: Int? = null

    @SerializedName("likes")
    @Expose
    var likes: Int? = null

    @SerializedName("comments")
    @Expose
    var comments: Int? = null

    @SerializedName("viewCount")
    @Expose
    var viewCount: Int? = null

    @SerializedName("challengeResult")
    @Expose
    var challengeResult: Int? = null

    @SerializedName("isLike")
    @Expose
    var isLike: Boolean? = null

    @SerializedName("isView")
    @Expose
    var isView: Boolean? = null

    @SerializedName("isVote")
    @Expose
    var isVote: Boolean? = null

    @SerializedName("acceptTime")
    @Expose
    var acceptTime: String? = null

    @SerializedName("isOpenChallenge")
    @Expose
    var isOpenChallenge: Boolean? = null

    @SerializedName("_id")
    @Expose
    var id: String? = null

    @SerializedName("userId")
    @Expose
    var userId: UserId? = null

    @SerializedName("createdAt")
    @Expose
    var createdAt: String? = null

    @SerializedName("__v")
    @Expose
    var v: Int? = null

    @SerializedName("createdBy")
    @Expose
    var createdBy: String? = null


    fun setExploreData(list: ExploreResult){
        exploreData = list
    }
    fun getExploreData(): ExploreResult? {
        return exploreData
    }
}