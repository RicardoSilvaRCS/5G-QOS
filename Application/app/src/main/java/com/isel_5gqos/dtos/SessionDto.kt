package com.isel_5gqos.dtos

import java.sql.Timestamp
import java.util.*

class SessionDto (
    val id : String,
    val sessionName : String,
    val username : String,
    val beginDate : Timestamp,
    val endDate: Timestamp
){

}