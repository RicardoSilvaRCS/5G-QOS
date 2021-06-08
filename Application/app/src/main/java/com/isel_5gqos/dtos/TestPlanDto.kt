package com.isel_5gqos.dtos

class TestPlanDto(
    val id: String,
    val name: String,
    val creator :String,
    val creatorName : String,
    val created : String,
    val modifier: String ,
    val modifierName : String,
    val modified : String,
    val href : String,
    val stateDate : String,
    val state: String,
    val startDate: String,
    val stopDate: String,
    //val scanningSampleTime: String,
    //val maxRepeats: Int,
    val tests: List<TestDto>,
)

class TestDto(
    val testType: String,
    val id: String,
    val name: String,
    val delay: String,
    val timeout: String,
    val nPings: Int,
    val messageInterval: String,
    val server: Server,
    val measurePeriod: String,
    val href : String
)

class Server(
    val host: String,
    val port : String
)
