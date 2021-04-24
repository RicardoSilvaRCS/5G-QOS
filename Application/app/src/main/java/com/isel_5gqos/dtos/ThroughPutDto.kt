package com.isel_5gqos.dtos

import java.sql.Timestamp
import java.util.*

class ThroughPutDto(
    val regId: String,
    val txResult: Long,
    val rxResult: Long,
    val sessionId: String,
    val timestamp: Timestamp
) {
}