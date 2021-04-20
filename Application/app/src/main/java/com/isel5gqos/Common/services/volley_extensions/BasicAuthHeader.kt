package com.isel5gqos.Common.services.volley_extensions

import android.util.Base64

public class BasicAuthHeader(private val userName: String, private val password: String) : IRequestHeader {
    override fun convertToRequestHeader(): Pair<String, String> =
        Pair(
            "Authorization",
            "Basic ${Base64.encodeToString("${userName}:${password}".toByteArray(charset("UTF-8")), Base64.DEFAULT).replace("\n","")}"
        )
}