package com.isel_5gqos.dtos

import android.util.Log
import com.isel_5gqos.Common.TAG
import org.json.JSONObject
import kotlin.math.log

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