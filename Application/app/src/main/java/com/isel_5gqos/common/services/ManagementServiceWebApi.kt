package com.isel_5gqos.common.services

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.android.volley.Request.Method.GET
import com.android.volley.Request.Method.POST
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.isel_5gqos.common.services.volley_extensions.BasicAuthHeader
import com.isel_5gqos.common.services.volley_extensions.TokenAuthHeader
import com.isel_5gqos.dtos.MobileDeviceDto
import com.isel_5gqos.dtos.TestPlanDto
import com.isel_5gqos.dtos.TestPlanResultDto
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

                val token = VolleyExtensions.getAuthenticationHeader(it)

                onSuccess(
                    UserDto.jsonObjectToUserDto(
                        token,
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
            url = REGISTER_MOBILE_DEVICE_URI,
            jsonBody = jsonBody,
            onSuccess = { responseBody ->

                onSuccess(MobileDeviceDto.jsonObjectToMobileDeviceDto(responseBody))

            },
            onError = onError,
            getHeaders = { VolleyExtensions.getHeaders(listOf(TokenAuthHeader(authenticationToken))) }
        )

        queue.add(requestObjectRequest)
    }

    fun refreshToken (
        authenticationToken : String,
        onSuccess: (String) -> Unit,
        onError: (VolleyError) -> Unit
    ) {

        val requestObjectRequest: JsonObjectRequest = JsonObjectRequestBuilder.build(
            method = POST,
            url = REFRESH_TOKEN_URI,
            jsonBody = JSONObject(),
            onSuccess = {

                val token = VolleyExtensions.getAuthorization(it)

                onSuccess(token)

            },
            onError = onError,
            getHeaders = { VolleyExtensions.getHeaders(listOf(TokenAuthHeader(authenticationToken))) }
        )

        queue.add(requestObjectRequest)
    }

    fun getTestPlan (
        authenticationToken: String,
        deviceId : Int,
        testPlanId : String,
        onSuccess: (TestPlanDto) -> Unit,
        onError: (VolleyError) -> Unit
    ) {

        val requestObjectRequest: JsonObjectRequest = JsonObjectRequestBuilder.build(
            method = GET,
            url = TEST_PLAN_URI(deviceId,testPlanId),
            bringHeaders = false,
            jsonBody = JSONObject(),
            onSuccess = { responseBody ->

                convertToTestPlanDtoAsync(response = responseBody.toString(), onSuccess = onSuccess)

            },
            onError = onError,
            getHeaders = { VolleyExtensions.getHeaders(listOf(TokenAuthHeader(authenticationToken))) }
        )

        queue.add(requestObjectRequest)
    }

    fun postTestPlanResults (
        authenticationToken: String,
        deviceId : Int,
        testPlanResult : Any,
        onSuccess: () -> Unit,
        onError: (VolleyError) -> Unit
    ) {


        Log.v("PINGTEST", gson.toJson(testPlanResult))

        val requestObjectRequest: JsonObjectRequest = JsonObjectRequestBuilder.build(
            method = POST,
            url = TEST_PLAN_RESULT_URI(deviceId),
            bringHeaders = false,
            jsonBody = JSONObject(gson.toJson(testPlanResult)),
            onSuccess = {

                onSuccess()

            },
            onError = onError,
            getHeaders = { VolleyExtensions.getHeaders(listOf(TokenAuthHeader(authenticationToken))) }
        )

        queue.add(requestObjectRequest)
    }



    private fun convertToTestPlanDtoAsync (response: String?, onSuccess: (TestPlanDto) -> Unit) =
        object : AsyncTask<String, Int, TestPlanDto>() {
            override fun doInBackground(vararg params: String?): TestPlanDto =
                gson.fromJson(response, TestPlanDto::class.java)

            override fun onPostExecute(result:TestPlanDto) = onSuccess(result!!)
        }.execute(response)


}
