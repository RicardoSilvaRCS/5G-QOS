package com.isel_5gqos.common.services

import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.JsonRequest
import com.isel_5gqos.common.services.volley_extensions.IRequestHeader
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException


class VolleyExtensions {
    companion object {

        fun getHeaders(iRequestHeaders: List<IRequestHeader>): MutableMap<String, String> {
            val headers = mutableMapOf<String, String>()
            iRequestHeaders.forEach {
                val headerPair = it.convertToRequestHeader()
                headers[headerPair.first] = headerPair.second
            }

            headers["Content-Type"] = "application/json"

            return headers
        }

        fun getAuthenticationHeader(jsonObject: JSONObject): String {
            val headers = jsonObject["headers"] as JSONObject
            return headers["Authorization"].toString().split(" ")[1]
        }

        fun getAuthorization(jsonObject: JSONObject): String {
            val headers = jsonObject["headers"] as JSONObject
            return headers["Authorization"].toString()
        }

    }
}

class JsonObjectRequestBuilder {
    companion object {
        fun build(
            method: Int,
            url: String,
            bringHeaders: Boolean = true,
            jsonBody: JSONObject? = null,
            onSuccess: (JSONObject) -> Unit,
            onError: (VolleyError) -> Unit,
            getHeaders: () -> MutableMap<String, String>
        ): JsonObjectRequest {
            return object : JsonObjectRequest(
                method,
                url,
                jsonBody,
                Response.Listener { onSuccess(it) },
                Response.ErrorListener(onError)
            ) {
                override fun getHeaders(): MutableMap<String, String> = getHeaders()

                //This is required because the response from server comes empty and the normal parser can't parse the response
                override fun parseNetworkResponse(response: NetworkResponse): Response<JSONObject> {
                    var networkResponse = response
                    try {
                        if (networkResponse.data.isEmpty()) {
                            val responseData = "{}".toByteArray(charset("UTF8"))
                            networkResponse = NetworkResponse(
                                networkResponse.statusCode, responseData,
                                networkResponse.notModified, networkResponse.networkTimeMs, networkResponse.allHeaders
                            )
                        }
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                    }

                    return returnResponse(networkResponse)
                }

                fun returnResponse(networkResponse: NetworkResponse): Response<JSONObject> {
                    return try {
                        val jsonString = String(
                            networkResponse.data
                        )

                        val jsonResponse = JSONObject(jsonString)

                        if (bringHeaders) {
                            jsonResponse.put("headers", JSONObject(networkResponse.headers as Map<*, *>))
                        }

                        Response.success(
                            jsonResponse, HttpHeaderParser.parseCacheHeaders(networkResponse)
                        )
                    } catch (e: UnsupportedEncodingException) {
                        Response.error(ParseError(e))
                    } catch (je: JSONException) {
                        Response.error(ParseError(je))
                    }
                }

            }
        }
    }
}

class JsonArrayRequestBuilder(
    method: Int,
    url: String?,
    private val jsonBody: JSONObject?,
    val getRequestHeaders: () -> MutableMap<String, String>,
    private val onSuccess: Response.Listener<JSONArray>,
    onError: Response.ErrorListener?
) :
    JsonRequest<JSONArray>(method, url, jsonBody?.toString(), onSuccess, onError) {
    override fun deliverResponse(response: JSONArray) {
        onSuccess.onResponse(response)
    }

    override fun getHeaders(): MutableMap<String, String> = getRequestHeaders()

    override fun parseNetworkResponse(response: NetworkResponse): Response<JSONArray> {
        var networkResponse = response
        try {
            if (networkResponse.data.isEmpty()) {
                val responseData = "{}".toByteArray(charset("UTF8"))
                networkResponse = NetworkResponse(
                    networkResponse.statusCode, responseData,
                    networkResponse.notModified, networkResponse.networkTimeMs, networkResponse.allHeaders
                )
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        return try {
            val json = String(networkResponse.data)
            try {
                Response.success(
                    JSONArray(json),
                    HttpHeaderParser.parseCacheHeaders(networkResponse)
                )
            } catch (e: JSONException) {
                Response.error(ParseError(e))
            }
        } catch (e: UnsupportedEncodingException) {
            Response.error(ParseError(e))
        }
    }

}

/*class JsonArrayRequestBuilder {
    companion object {
        fun build(
            method: Int,
            url: String,
//            bringHeaders : Boolean = true,
            jsonBody: JSONObject? = null,
            onSuccess: (JSONArray) -> Unit,
            onError: (VolleyError) -> Unit,
//            getHeaders: () -> MutableMap<String, String>
        ): JsonRequest<JsonArrayRequest> {
            return object : JsonArrayRequest(
                method,
                url,
                jsonBody,
                Response.Listener { onSuccess(it) },
                Response.ErrorListener(onError)
            ) {
//                override fun getHeaders(): MutableMap<String, String> = headers

                //This is required because the response from server comes empty and the normal parser can't parse the response
                override fun parseNetworkResponse(response: NetworkResponse): Response<JSONArray> {
                    var networkResponse = response
                    try {
                        if (networkResponse.data.isEmpty()) {
                            val responseData = "{}".toByteArray(charset("UTF8"))
                            networkResponse = NetworkResponse(
                                networkResponse.statusCode, responseData,
                                networkResponse.notModified, networkResponse.networkTimeMs, networkResponse.allHeaders
                            )
                        }
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                    }

                    return returnResponse(networkResponse)
                }

                fun returnResponse(networkResponse: NetworkResponse): Response<JSONArray> {
                    return try {
                        val jsonString = String(
                            networkResponse.data
                        )

                        val jsonResponse = JSONArray(jsonString)

//                        if(bringHeaders) {
//                            jsonResponse.put("headers", JSONObject(networkResponse.headers as Map<*, *>))
//                        }

                        Response.success(
                            jsonResponse, HttpHeaderParser.parseCacheHeaders(networkResponse)
                        )
                    } catch (e: UnsupportedEncodingException) {
                        Response.error(ParseError(e))
                    } catch (je: JSONException) {
                        Response.error(ParseError(je))
                    }
                }

            }
        }
    }
}*/
