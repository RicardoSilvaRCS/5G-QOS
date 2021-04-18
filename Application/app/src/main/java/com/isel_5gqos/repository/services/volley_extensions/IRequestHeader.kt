package com.isel_5gqos.repository.services.volley_extensions

interface IRequestHeader {
    fun convertToRequestHeader():Pair<String,String>

}