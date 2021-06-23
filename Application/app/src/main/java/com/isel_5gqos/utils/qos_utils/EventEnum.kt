package com.isel_5gqos.utils.qos_utils

enum class EventEnum (val eventType : String) {
    SYSTEM_START("SYSTEM_START"),
    SERVER_REGISTRATION_ATTEMPT("SERVER_REGISTRATION_ATTEMPT"),
    SERVER_REGISTRATION_OK("SERVER_REGISTRATION_OK"),
    CONTROL_CONNECTION_OK("CONTROL_CONNECTION_OK"),
    CONTROL_CONNECTION_ATTEMPT("CONTROL_CONNECTION_ATTEMPT"),
    GPS_ON("GPS_ON"),
    GPS_OFF("GPS_OFF")
}