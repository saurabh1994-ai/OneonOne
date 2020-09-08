package com.sws.oneonone.firebase_notification

class NotiModel {
    private var title: String? = null
    private var body: String? = null
    private var comeFrom: String? = null

    fun NotificationModel(title: String?, body: String?, comeFrom: String?) {
        this.title = title
        this.body = body
        this.comeFrom = comeFrom
    }

    fun getBody(): String? {
        return body
    }

    fun setBody(body: String?) {
        this.body = body
    }

    fun getTitle(): String? {
        return title
    }

    fun setTitle(title: String?) {
        this.title = title
    }

    fun setComeFrom(comeFrom: String?){
        this.comeFrom = comeFrom
    }
    fun getComeFrom(): String? {
        return comeFrom
    }
}