package com.isel_5gqos.common.services

import android.content.Context
import android.os.AsyncTask
import com.android.volley.Request.Method.POST
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.isel_5gqos.common.db.entities.MobileUnit
import com.isel_5gqos.common.services.volley_extensions.BasicAuthHeader
import com.isel_5gqos.common.services.volley_extensions.TokenAuthHeader
import com.isel_5gqos.dtos.MobileDeviceDto
import com.isel_5gqos.dtos.UserDto
import org.json.JSONObject

class ManagementServiceWebApi(val ctx: Context) {

    private val queue = Volley.newRequestQueue(ctx)
    private val gson = Gson()

    fun login(
        username: String,
        password: String,
        onSuccess: (UserDto) -> Unit,
        onError: (VolleyError) -> Unit
    ) {

        val requestObjectRequest: JsonObjectRequest = JsonObjectRequestBuilder.build(
            method = POST,
            url = USER_LOGIN_URI,
            jsonBody = null,
            onSuccess = {
                onSuccess(
                    UserDto.jsonObjectToUserDto(
                        it,
                        username
                    )
                )
            },
            onError = onError,
            getHeaders = { VolleyExtensions.getHeaders(listOf(BasicAuthHeader(userName = username, password = password))) }
        )

        queue.add(requestObjectRequest)
    }

    fun registerMobileDevice (
        mobileSerialNumber: String,
        authenticationToken : String,
        onSuccess: (MobileDeviceDto) -> Unit,
        onError: (VolleyError) -> Unit
    ) {

        val jsonBody = JSONObject( mapOf(
            "serialNumber" to mobileSerialNumber
        ))

        val requestObjectRequest: JsonObjectRequest = JsonObjectRequestBuilder.build(
            method = POST,
            url = REGISTER_MOBILE_DEVICE,
            jsonBody = jsonBody,
            onSuccess = { responseBody ->

                executeAsyncTaskGeneric(
                    function = {

                        MobileDeviceDto.jsonObjectToMobileDeviceDto(it)

                    },
                    param = responseBody,
                    onSuccess = { result ->
                        onSuccess(result)
                    }
                )

            },
            onError = onError,
            getHeaders = { VolleyExtensions.getHeaders(listOf(TokenAuthHeader(authenticationToken))) }
        )

        queue.add(requestObjectRequest)
    }

    private fun <T, R> executeAsyncTaskGeneric(function: (T) -> R, param: T, onSuccess: (R) -> Unit): AsyncTask<T, Int, R> =
        object : AsyncTask<T, Int, R>() {
            override fun doInBackground(vararg params: T): R = function(param)
            override fun onPostExecute(result: R) = onSuccess(result)
        }

}
