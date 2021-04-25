package com.isel_5gqos.common.services

import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.isel_5gqos.common.services.volley_extensions.IRequestHeader
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

            //TODO change user agent
//            headers["User-Agent"] = "blablabla"
            return headers
        }
    }
}

class JsonObjectRequestBuilder {
    companion object {
        fun build(
            method: Int,
            url: String,
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

                    return returnResponseWithHeaders(networkResponse)
                }

                fun returnResponseWithHeaders(networkResponse: NetworkResponse): Response<JSONObject> {
                    return try {
                        val jsonString = String(
                            networkResponse.data
                        )

                        val jsonResponse = JSONObject(jsonString)
                        jsonResponse.put("headers", JSONObject(networkResponse.headers as Map<*, *>))

                        Response.success(
                            jsonResponse, HttpHeaderParser.parseCacheHeaders(networkResponse)
                        )
                    } catch (e: UnsupportedEncodingException) {
                        Response.error<JSONObject>(ParseError(e))
                    } catch (je: JSONException) {
                        Response.error<JSONObject>(ParseError(je))
                    }
                }

            }
        }
    }
}

