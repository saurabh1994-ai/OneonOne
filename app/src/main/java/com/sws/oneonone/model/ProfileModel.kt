package com.sws.oneonone.model

class ProfileModel(acceptUnderline:Int, challengeUnderline:Int){

    var acceptUnderline:Int? = null
    var challengeUnderline:Int? = null

    init {
        this.acceptUnderline = acceptUnderline
        this.challengeUnderline = challengeUnderline
    }
}