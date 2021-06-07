package com.isel_5gqos.dtos

class TestPlanDto(
    val id: String,
    val name: String,
    val startDate: String,
    val stopDate: String,
    val scanningSampleTime: String,
    val maxRepeats: Int,
    val tests: List<TestDto>,
)

class TestDto(
    val id: String,
    val name: String,
    val testType: String,
    val delay: String,
    val timeout: String,
    val nPings: Int,
    val messageInterval: String,
    val server: Server,
    val measurePeriod: String,
)

class Server(
    val host: String
)
