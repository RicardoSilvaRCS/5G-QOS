package com.isel_5gqos.dtos

import androidx.work.WorkRequest
import com.isel_5gqos.common.db.entities.Session
import java.sql.Timestamp

class SessionDto(val id: String, val sessionName: String, val username: String, val beginDate: Timestamp, var endDate: Timestamp = Timestamp(0L)) {
    val throughPuts: MutableList<ThroughPutDto> = mutableListOf()
    val pings: MutableList<PingDto> = mutableListOf()
    lateinit var radioParameters : WrapperDto

    companion object {
        fun makeDefault() = SessionDto(
            "",
            "",
            "",
            Timestamp(0L),
            Timestamp(0L)
        )

    }

    fun dtoToDaoMapper() = Session(
        id = this.id,
        sessionName = this.sessionName,
        user = this.username,
        beginDate = this.beginDate.time,
        endDate = this.endDate.time
    )
}