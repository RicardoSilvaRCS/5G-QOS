package com.isel_5gqos.common.services.volley_extensions

import android.util.Base64

class BasicAuthHeader(private val userName: String, private val password: String) : IRequestHeader {
    override fun convertToRequestHeader(): Pair<String, String> =
        Pair(
            "Authorization",
            "Basic ${Base64.encodeToString("${userName}:${password}".toByteArray(charset("UTF-8")), Base64.DEFAULT).replace("\n", "")}"
        )
}

class TokenAuthHeader (private val authenticationToken : String ) : IRequestHeader {
    override fun convertToRequestHeader(): Pair<String, String> =
        Pair(
            "Authorization",
            authenticationToken
        )
}