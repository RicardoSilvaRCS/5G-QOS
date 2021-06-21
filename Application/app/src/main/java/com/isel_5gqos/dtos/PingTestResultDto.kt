package com.isel_5gqos.dtos

class PingTestResultDto (
    val avgRtt: Float,
    val maxRtt: Float,
    val minRtt: Float,
    val nPings: Int,
    val sent: Int,
    val lost: Int,
    val lostPercent: Float,
    val detailedPing: List<DetailedPingDto>,
    date: String,
    navigationDto: NavigationDto,
    probeId: Int,
    testExecutionId: String,
    testPlanId: String,
    type: String
) : TestPlanResultDto(date, navigationDto, probeId, testExecutionId, testPlanId, type)

class DetailedPingDto(
    val from: String,
    val bytes: Int,
    val time: Float, //time in ms
    val ttl: Int,
)