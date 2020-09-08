package com.sws.oneonone.firebaseModel

class UserModel {

    private var userMadelData: UserModel? = null
    private var withChatUserId: String? = null
    private var chatKey: String? = null
    private var chatType: String? = null
    private var members: ArrayList<String>? = null

    private var fcmToken:String? = null
    private var isTyping: Boolean? = null
    private var lastSeen: String? = null
    private var notificationStatus: String? = null
    private var onChatScreen: Boolean? = null
    private var status: Boolean? = null
    private var userEmail: String? = null
    private var userImg: String? = null
    private var userName: String? = null
    private var readable: String? = null
    private var msgStatus: String? = null

    constructor() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    constructor(fcmToken: String?,
                isTyping: Boolean?,
                lastSeen: String?,
                notificationStatus: String?,
                onChatScreen: Boolean?,
                status: Boolean?,
                userEmail: String?,
                userImg: String?,
                userName: String?,
                msgStatus: String?,
                members: ArrayList<String>?) {
        this.fcmToken = fcmToken
        this.isTyping = isTyping
        this.lastSeen = lastSeen
        this.notificationStatus = notificationStatus
        this.onChatScreen = onChatScreen
        this.status = status
        this.userEmail = userEmail
        this.userImg = userImg
        this.userName = userName
        this.msgStatus = msgStatus
        this.members = members
    }

    /*--------------------------------------------*/
    fun setUserMadelData(_userList: UserModel?){
        userMadelData = _userList
    }
    fun getUserMadelData(): UserModel? {
        return userMadelData
    }

    fun setWithChatUserId(userId: String?){
        withChatUserId = userId
    }
    fun getWithChatUserId(): String? {
        return withChatUserId
    }

    fun setChatKey(key: String?){
        chatKey = key
    }
    fun getChatKey(): String? {
        return chatKey
    }

    /*------------------------------------*/


    fun setChatType(type: String?) {
        chatType = type
    }
    fun getChatType(): String?{
        return chatType
    }
    fun setFcmToken(token: String?){
        fcmToken = token
    }
    fun getFcmToken(): String? {
        return fcmToken
    }

    fun setIsTyping(typing: Boolean?){
        isTyping = typing
    }
    fun getIsTyping(): Boolean? {
        return isTyping
    }

    fun setLastSeen(seen: String?){
        lastSeen = seen
    }
    fun getLastSeen(): String? {
        return lastSeen
    }

    fun setNotificationStatus(status: String?){
        notificationStatus = status
    }
    fun getNotificationStatus(): String? {
        return notificationStatus
    }

    fun setOnChatScreen(onScreen: Boolean?){
        onChatScreen = onScreen
    }
    fun getOnChatScreen(): Boolean? {
        return onChatScreen
    }

    fun setStatus(_status: Boolean?){
        status = _status
    }
    fun getStatus(): Boolean? {
        return status
    }

    fun setUserEmail(email: String?){
        userEmail = email
    }
    fun getUserEmail(): String? {
        return userEmail
    }

    fun setUserImg(img: String?){
        userImg = img
    }
    fun getUserImg(): String? {
        return userImg
    }

    fun setUserName(name: String?){
        userName = name
    }
    fun getUserName(): String? {
        return userName//+"😊😊\uD83C\uDF3B"
    }


    fun setReadable(read: String?){
        readable = read
    }

    fun getReadable(): String? {
        return readable
    }

    fun setMsgStatus(msg: String?){
        msgStatus = msg
    }
    fun getMsgStatus(): String?{
        return msgStatus
    }

    fun setMembers(list: ArrayList<String>?){
        members = list
    }
    fun getMembers(): ArrayList<String>?{
        return members
    }
}