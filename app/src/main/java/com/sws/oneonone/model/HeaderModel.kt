package com.sws.oneonone.model

class HeaderModel {

    private var userResult: SignUpResultModel? = null
    private var childList: ArrayList<SignUpResultModel>? = null
    private var string: String? = null

    fun setUserResult(data: SignUpResultModel?) {
        userResult = data
    }

    fun getUserResult(): SignUpResultModel? {
        return userResult
    }

    fun setString(data: String?) {
        string = data
    }

    fun getString(): String? {
        return string
    }

    fun setChildList(data: ArrayList<SignUpResultModel>?){
        childList = data
    }
    fun getChildList(): ArrayList<SignUpResultModel>? {
        return childList
    }
}