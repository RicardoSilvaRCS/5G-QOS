package com.isel_5gqos.dtos

class SystemLogDto(
    val date : String,
    val event : String,
    val id : Int,
    val level : String,
    val navigationDto : NavigationDto,
    val probeId : Int,
    val properties : String
)