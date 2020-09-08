package com.sws.oneonone.firebaseModel

class GroupModel {

    private var userMadelData: GroupModel? = null
    private var groupKey: String? = null

    private var fmcToken:String? = null
    private var isTyping: Boolean? = null
    private var lastSeen: String? = null
    //   private var status: Boolean? = null
    private var groupImg: String? = null
    private var groupName: String? = null
    private var createdBy: String? = null
    private var members: ArrayList<String>? = null

    constructor() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    constructor(fmcToken: String?,
                isTyping: Boolean?,
                lastSeen: String?,
        //  status: Boolean?,
                groupImg: String?,
                groupName: String?,
                createdBy: String?,
                members: ArrayList<String>?) {
        this.fmcToken = fmcToken
        this.isTyping = isTyping
        this.lastSeen = lastSeen
        // this.status = status
        this.groupImg = groupImg
        this.groupName = groupName
        this.createdBy = createdBy
        this.members = members
    }

    // all group details
    fun setUserMadelData(_userList: GroupModel?){
        userMadelData = _userList
    }
    fun getUserMadelData(): GroupModel? {
        return userMadelData
    }


    // group details key (get by firebase database)
    fun setGroupKey(key: String?){
        groupKey = key
    }
    fun getGroupKey(): String? {
        return groupKey
    }


    // All group Details
    fun setFmcToken(token: String?){
        fmcToken = token
    }
    fun getFmcToken(): String? {
        return fmcToken
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

    /*fun setStatus(_status: Boolean?){
        status = _status
    }
    fun getStatus(): Boolean? {
        return status
    }*/

    fun setGroupImg(img: String?){
        groupImg = img
    }
    fun getGroupImg(): String? {
        return groupImg
    }

    fun setGroupName(name: String?){
        groupName = name
    }
    fun getGroupName(): String? {
        return groupName//+"😊😊\uD83C\uDF3B"
    }

    fun setCreatedBy(created_by: String?){
        createdBy = created_by
    }
    fun getCreatedBy(): String? {
        return createdBy
    }

    fun setMembers(members: ArrayList<String>?){
        this.members = members
    }
    fun getMembers(): ArrayList<String>? {
        return members
    }
}