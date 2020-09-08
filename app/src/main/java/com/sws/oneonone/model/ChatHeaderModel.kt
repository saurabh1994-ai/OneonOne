package com.sws.oneonone.model

class ChatHeaderModel(headerText: String?, childArray: ArrayList<NewChatModel>) {
    var headerText: String? = null
    var childArray: ArrayList<NewChatModel>? = null

    init {
        this.headerText = headerText
        this.childArray = childArray
    }
}