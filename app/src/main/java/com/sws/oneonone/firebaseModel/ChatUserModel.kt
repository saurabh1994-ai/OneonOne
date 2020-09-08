package com.sws.oneonone.firebaseModel

import com.google.gson.JsonArray
import com.google.gson.JsonObject

class ChatUserModel {

    private var chatUserModel: ChatUserModel? = null
    private var userId: String? = null

    private var image:String? = ""
    private var message: String? = ""
    private var msgStatus: String? = null
    private var isGroup: Boolean? = false
    private var read: String? = null
    private var recieverDelete: String? = ""
    private var recieverId: String? = ""
    private var senderDelete: String? = ""
    private var senderId: String? = ""
    private var time: String? = ""
    private var video: String? = ""
    private var videoThumb: String? = ""
    private var msgKey: String? = null
    private var chatKey: String? = null

    constructor() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    constructor(image: String?,
                message: String?,
                msgStatus: String?,
                read: String?,
                recieverDelete: String?,
                recieverId: String?,
                senderDelete: String?,
                senderId: String?,
                time: String?) {
               // msgKey: String?
              //  chatKey: String?) {
        this.image = image
        this.message = message
        this.msgStatus = msgStatus
        this.read = read
        this.recieverDelete = recieverDelete
        this.recieverId = recieverId
        this.senderDelete = senderDelete
        this.senderId = senderId
        this.time = time
       // this.msgKey = msgKey
       // this.chatKey = chatKey
    }


    fun setImage(token: String?){
        image = token
    }
    fun getImage(): String? {
        return image
    }

    fun setMessage(typing: String?){
        message = typing
    }
    fun getMessage(): String? {
        return message
    }

    fun setIsGroup(isGroup: Boolean?){
        this.isGroup = isGroup
    }
    fun getIsGroup(): Boolean? {
        return isGroup
    }

    fun setMsgStatus(seen: String?){
        msgStatus = seen
    }
    fun getMsgStatus(): String? {
        return msgStatus
    }

    fun setRead(recieverId: String?){
        read = recieverId
    }
    fun getRead(): String? {
        return read
    }

    fun setRecieverDelete(onScreen: String?){
        recieverDelete = onScreen
    }
    fun getRecieverDelete(): String? {
        return recieverDelete
    }

    fun setRecieverId(_recieverId: String?){
        recieverId = _recieverId
    }
    fun getRecieverId(): String? {
        return recieverId
    }

    fun setSenderDelete(email: String?){
        senderDelete = email
    }
    fun getSenderDelete(): String? {
        return senderDelete
    }

    fun setSenderId(sender_id: String?){
        senderId = sender_id
    }
    fun getSenderId(): String? {
        return senderId
    }

    fun setTime(_time: String?){
        time = _time
    }
    fun getTime(): String? {
        return time
    }

    fun setVideo(_video: String?){
        video = _video
    }
    fun getVideo(): String? {
        if(video.isNullOrEmpty()){
            return ""
        } else {
            return video
        }
    }

    fun setVideoThumb(_videoThumb: String?){
        videoThumb = _videoThumb
    }
    fun getVideoThumb(): String? {
        return videoThumb
    }

    fun setChatUserModel(userData: ChatUserModel?) {
        chatUserModel = userData
    }
    fun getChatUserModel(): ChatUserModel? {
        return chatUserModel
    }

    fun setUserId(id: String?){
        userId = id
    }
    fun getUserId(): String? {
        return userId
    }

    fun setMsgKey(msgKey: String?){
        this.msgKey = msgKey
    }
    fun getMsgKey(): String? {
        return msgKey
    }

    fun setChatKey(key: String?) {
        chatKey = key
    }
    fun getChatKey(): String? {
        return chatKey
    }

}