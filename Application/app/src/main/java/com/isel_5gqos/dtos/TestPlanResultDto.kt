package com.isel_5gqos.dtos

open class TestPlanResultDto (
    val date : String,
    val navigationDto : NavigationDto,
    val probeId : Int,
    val testId : String,
    val testPlanId : String,
    val type : String
)