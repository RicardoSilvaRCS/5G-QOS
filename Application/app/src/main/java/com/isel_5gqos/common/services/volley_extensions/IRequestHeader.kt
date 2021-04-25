package com.isel_5gqos.common.services.volley_extensions

interface IRequestHeader {
    fun convertToRequestHeader(): Pair<String, String>

}