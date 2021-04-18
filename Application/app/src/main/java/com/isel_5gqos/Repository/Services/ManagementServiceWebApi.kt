package com.isel_5gqos.Repository.Services

import android.content.Context
import android.os.AsyncTask
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.isel_5gqos.Dtos.UserDto

class ManagementServiceWebApi (val ctx : Context) {

    private val queue = Volley.newRequestQueue(ctx)
    private val gson = Gson()


    fun login (username : String , password : String, onSuccess : (UserDto)-> Unit, onError : (VolleyError) -> Unit) {
        val stringRequest = StringRequest(
            Request.Method.POST,
            USER_LOGIN_URI,
            Response.Listener<String> { response -> executeAsyncTask(response, onSuccess) },
            Response.ErrorListener(onError)
        )
    }

    private fun executeAsyncTask (response: String?, onSuccess: (UserDto) -> Unit) =
        object : AsyncTask<String, Int, UserDto>() {
            override fun doInBackground(vararg params: String?): UserDto =
                gson.fromJson<UserDto>(response, UserDto::class.java)

            override fun onPostExecute(result:UserDto) = onSuccess(result!!)
        }.execute(response)
}