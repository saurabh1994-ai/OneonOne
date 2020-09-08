package com.sws.oneonone.model

class ChildModel {
    private var id: String? = null
    private var image: String? = null
    private var fullname: String? = null
    private var userName: String? = null
    private var position: Int? = null

    fun setId(id: String?){
        this.id = id
    }
    fun getId(): String? {
        return id
    }

    fun setImage(image: String?){
        this.image = image
    }
    fun getImage(): String? {
        return image
    }

    fun setFullName(fullName: String?){
        this.fullname = fullName
    }
    fun getFullName(): String?{
        return fullname
    }
    fun setUserName(userName: String?){
        this.userName = userName
    }
    fun getUserName(): String?{
        return userName
    }

    fun setPosition(position: Int){
        this.position =  position
    }
    fun getPosition(): Int? {
        return position
    }
}