package com.isel_5gqos.dtos

open class TestPlanResultDto (
    val id : Int,
    val date : String,
    val navigationDto : NavigationDto,
    val probeId : Int,
    val testId : String,
    val testPlanId : String,
    val type : String
)

class NavigationDto (
    var gpsFix : String = "",
    var heading : Int = 0,
    var heightAboveEllipsoid : Int = 0,
    var heightAboveMSL : Int = 0,
    val latitude : Double,
    val longitude : Double,
    var speed : Float? = 0f
)