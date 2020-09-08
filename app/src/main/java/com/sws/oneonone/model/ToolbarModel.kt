package com.sws.oneonone.model

class ToolbarModel(
    isBack:Int, title:String , isProfile:Int, isTitle:Int, isSearch:Int, isNoti:Int, bgColor: Int,
    actionTitle: String, count:String, isShowCount:Int
){
    var isBack:Int? = null
    var title:String? = null
    var isProfile:Int? = null
    var isTitle:Int? = null
    var isSearch:Int? = null
    var isNoti:Int? = null
    var bgColor:Int? = null
    var actionTitle:String? = null
    var count:String? = null
    var isShowCount : Int? = null

    init {
        this.isBack = isBack
        this.title = title
        this.isProfile = isProfile
        this.isTitle = isTitle
        this.isSearch = isSearch
        this.isNoti= isNoti
        this.bgColor = bgColor
        this.actionTitle = actionTitle
        this.count = count
        this.isShowCount = isShowCount
    }
}