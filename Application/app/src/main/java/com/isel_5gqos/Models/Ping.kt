package com.isel_5gqos.Models

data class Ping(
    val numberOfTries: Int,
    val url: String,
    val pingInfos: MutableList<PingInfo> = mutableListOf(),
    var avg: Float = 0.0F,
    var minMs: Float = 0.0F,
    var maxMs: Float = 0.0F
)

data class PingInfo(
    val seq: Int,
    val ttl: Int,
    val time: Double
)