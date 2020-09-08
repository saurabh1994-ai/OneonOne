package com.sws.oneonone.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ChallengeIdModel() : Parcelable {
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

    @SerializedName("_id")
    @Expose
    var id: String? = null

    @SerializedName("userId")
    @Expose
    var userId: String? = null

    @SerializedName("createdAt")
    @Expose
    var createdAt: String? = null

    @SerializedName("createdBy")
    @Expose
    var createdBy: String? = null

    @SerializedName("__v")
    @Expose
    var v: Int? = null

     var challengeView : Boolean ? = null

    constructor(parcel: Parcel) : this() {
        hashtagIds = parcel.createStringArrayList()
        hashtags = parcel.createStringArrayList()
        userTo = parcel.createStringArrayList()
        acceptedBy = parcel.createStringArrayList()
        uid = parcel.readString()
        title = parcel.readString()
        image = parcel.readString()
        video = parcel.readString()
        thumbnails = parcel.readString()
        type = parcel.readString()
        active = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        delete = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        request = parcel.readString()
        votes = parcel.readValue(Int::class.java.classLoader) as? Int
        likes = parcel.readValue(Int::class.java.classLoader) as? Int
        comments = parcel.readValue(Int::class.java.classLoader) as? Int
        viewCount = parcel.readValue(Int::class.java.classLoader) as? Int
        challengeResult = parcel.readValue(Int::class.java.classLoader) as? Int
        isLike = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        isView = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        isVote = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        acceptTime = parcel.readString()
        id = parcel.readString()
        userId = parcel.readString()
        createdAt = parcel.readString()
        createdBy = parcel.readString()
        v = parcel.readValue(Int::class.java.classLoader) as? Int
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {

    }

    override fun describeContents(): Int {
      return 0
    }

    companion object CREATOR : Parcelable.Creator<ChallengeIdModel> {
        override fun createFromParcel(parcel: Parcel): ChallengeIdModel {
            return ChallengeIdModel(parcel)
        }

        override fun newArray(size: Int): Array<ChallengeIdModel?> {
            return arrayOfNulls(size)
        }
    }

}