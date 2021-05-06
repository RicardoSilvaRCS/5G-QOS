package com.isel_5gqos.dtos

import com.isel_5gqos.common.db.entities.ThroughPut
import java.sql.Timestamp

class ThroughPutDto(
    val regId: String,
    val txResult: Long,
    val rxResult: Long,
    val sessionId: String,
    val timestamp: Timestamp
) {

    companion object {

        fun convertThroughPutToDto(throughPut: List<ThroughPut>) = throughPut.map { convertThroughPutToDto(it) }

        fun convertThroughPutToDto(throughPut: ThroughPut) = ThroughPutDto(
            regId = throughPut.regId,
            txResult = throughPut.txResult,
            rxResult = throughPut.rxResult,
            sessionId = throughPut.sessionId,
            timestamp = Timestamp(throughPut.timestamp)
        )

    }

}