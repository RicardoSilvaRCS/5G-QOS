package com.isel_5gqos.dtos

class NavigationDto(
    var gpsFix : String = "FIX_3D",
    var heading : Int = 0,
    var heightAboveEllipsoid : Int = 0,
    var heightAboveMSL : Int = 0,
    val latitude : Double,
    val longitude : Double,
    var speed : Float? = 0f
)