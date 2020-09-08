package com.sws.oneonone.retrofit

import java.util.regex.Pattern

class Validation {

    fun checkString(string: String?): Boolean {
        if (string?.trim().isNullOrBlank()){
            return false
        } else {
            return true
        }
    }

    fun checkPassword(string: String?): Boolean {
        if (string?.trim().isNullOrBlank()){
            return false
        } else if (string?.length!! < 6) {
            return false
        } else {
            return true
        }
    }

    fun checkGender(string: String?): String {
        if (string?.trim().isNullOrBlank()){
            return ""
        } else if (string?.toUpperCase().equals("MALE")) {
            return "M"
        } else if (string?.toUpperCase().equals("FEMALE")) {
            return "F"
        } else  if (string?.toUpperCase().equals("OTHER")) {
            return "O"
        } else {
            return ""
        }
    }

    fun checkEmail(string: String?): Boolean {
        if (string?.trim().isNullOrBlank()){
            return false
        } else {
            val EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
            val pattern = Pattern.compile(EMAIL_PATTERN)
            val matcher = pattern.matcher(string)
            if (matcher.matches()){
                return true
            } else {
                return false
            }
        }
    }

}