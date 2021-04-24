package com.isel_5gqos.Common.services

import android.content.Context
import android.os.AsyncTask
import com.android.volley.Request.Method.POST
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.isel_5gqos.Common.services.volley_extensions.BasicAuthHeader
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
            onSuccess = { onSuccess(UserDto.jsonObjectToUserDto(it,username))},//{ executeAsyncTask(response = it, username = username, onSuccess = onSuccess) },
            onError = onError,
            getHeaders = { VolleyExtensions.getHeaders(listOf(BasicAuthHeader(userName = username, password = password))) }
        )

        queue.add(requestObjectRequest)
    }

    private fun executeAsyncTask(response: JSONObject, username: String, onSuccess: (UserDto) -> Unit) =
        object : AsyncTask<String, Int, UserDto>() {
            override fun doInBackground(vararg params: String?): UserDto =
                UserDto.jsonObjectToUserDto(response, username)

            override fun onPostExecute(result: UserDto) = onSuccess(result!!)
        }.execute()
}
