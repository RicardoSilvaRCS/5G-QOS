package com.isel_5gqos.dtos

import android.util.Log
import com.isel_5gqos.common.TAG
import org.json.JSONObject

class UserDto(
    val username: String,
    val userToken: String
) {
    companion object {

        fun jsonObjectToUserDto(token : String, username: String): UserDto {
            return UserDto(username, token)
        }

    }

}