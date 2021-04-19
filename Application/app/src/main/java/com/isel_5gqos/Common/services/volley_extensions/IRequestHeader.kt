package com.isel_5gqos.Common.services.volley_extensions

interface IRequestHeader {
    fun convertToRequestHeader():Pair<String,String>

}