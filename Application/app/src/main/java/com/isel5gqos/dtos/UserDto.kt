package com.isel5gqos.dtos

import android.util.Log
import com.isel5gqos.Common.TAG
import org.json.JSONObject

class UserDto (
    val username : String,
    val userToken : String
) {
    companion object {
        fun jsonObjectToUserDto(jsonObject: JSONObject):UserDto {
            Log.v(TAG,jsonObject.toString())
            return UserDto("","")
        }
    }

}