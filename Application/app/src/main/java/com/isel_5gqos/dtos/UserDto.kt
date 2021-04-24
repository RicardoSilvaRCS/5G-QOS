package com.isel_5gqos.dtos

import android.util.Log
import com.isel_5gqos.Common.TAG
import org.json.JSONObject

class UserDto (
    val username : String,
    val userToken : String
) {
    companion object {
        fun jsonObjectToUserDto(jsonObject: JSONObject,username: String):UserDto {
            Log.v(TAG,jsonObject.toString())
            val headers = jsonObject["headers"] as JSONObject
            val token = headers["Authorization"].toString().split(" ")[1]
            return UserDto(username,token)
        }
    }

}