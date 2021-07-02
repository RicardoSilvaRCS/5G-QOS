package com.isel_5gqos.dtos

data class ControlConnectionDto(
	val gpsFix: String? = null,
	val heading: Int? = null,
	val heightAboveEllipsoid: Int? = null,
	val heightAboveMSL: Int? = null,
	val lon: Double? = null,
	val lat: Double? = null,
	val speed: Int? = null
)

