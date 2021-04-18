package com.isel_5gqos.repository.services

import android.content.Context
import android.os.AsyncTask
import com.android.volley.Request.Method.POST
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.isel_5gqos.dtos.UserDto
import com.isel_5gqos.repository.services.volley_extensions.BasicAuthHeader

class ManagementServiceWebApi(val ctx: Context) {

    private val queue = Volley.newRequestQueue(ctx)
    private val gson = Gson()

    fun login(
        username: String,
        password: String,
        onSuccess: (UserDto) -> Unit,
        onError: (VolleyError) -> Unit
    ) {

        val requestObjectRequest:JsonObjectRequest = JsonObjectRequestBuilder.build(
            method = POST,
            url = USER_LOGIN_URI,
            jsonBody = null,
            onSuccess = { onSuccess(UserDto.jsonObjectToUserDto(it))},
            onError = onError,
            getHeaders = { VolleyExtensions.getHeaders(listOf(BasicAuthHeader(userName = username,password = password))) }
        )

        queue.add(requestObjectRequest)
    }

    private fun executeAsyncTask(response: String?, onSuccess: (UserDto) -> Unit) =
        object : AsyncTask<String, Int, UserDto>() {
            override fun doInBackground(vararg params: String?): UserDto =
                gson.fromJson<UserDto>(response, UserDto::class.java)

            override fun onPostExecute(result: UserDto) = onSuccess(result!!)
        }.execute(response)
}
