package com.sws.oneonone.model

class NewChatModel(name: String?, text: String?,image: String? ) {
    var chatName:String? = null
    var simpleText:String? = null
    var userImage:String? = null

    init {
        this.chatName = name
        this.simpleText = text
        this.userImage = image
    }


}