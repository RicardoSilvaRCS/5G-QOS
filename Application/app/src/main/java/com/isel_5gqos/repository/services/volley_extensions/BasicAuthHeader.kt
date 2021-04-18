package com.isel_5gqos.repository.services.volley_extensions

import android.util.Base64

public class BasicAuthHeader(private val userName: String, private val password: String) : IRequestHeader {
    override fun convertToRequestHeader(): Pair<String, String> =
        Pair(
            "Authentication",
            "Basic ${Base64.encodeToString("${userName}:${password}".toByteArray(charset("UTF-8")), Base64.DEFAULT).replace("\n","")}"
        )
}