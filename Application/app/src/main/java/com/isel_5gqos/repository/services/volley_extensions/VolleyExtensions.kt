package com.isel_5gqos.repository.services

import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.isel_5gqos.repository.services.volley_extensions.IRequestHeader
import org.json.JSONObject

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
            }
        }
    }
}

