package com.isel_5gqos.common


/**        Permissions           **/

const val APP_PERMISSIONS = 10

/**        In case to make exclusive permissions treatment        **/
const val ACCESS_FINE_LOCATION_PERMISSION = 1
const val INTERNET_PERMISSION = 2
const val ACCESS_NETWORK_STATE_PERMISSION = 3
const val READ_PHONE_STATE_PERMISSION = 4


/**        TAGS           **/
const val TAG = "5GQosApp"
const val WORKER_TAG = "WORKER_TAG"
const val RADIO_PARAMETERS_WORKER_ID = "RAD_PAR_ID"

/**   WORKER INPUT DATA   **/
const val SESSION_ID = "SESSION_ID"
const val DB_SAVE = "DB_SAVE"
const val PROGRESS = "PROGRESS"

const val RADIO_PARAMS_TYPE = "RADIO_PARAMS_TYPE"
const val THROUGHPUT_TYPE = "THROUGHPUT_TYPE"

object WorkTypes {
    operator fun get(workType: String) = types[workType]

    val types = mapOf(
        RADIO_PARAMS_TYPE to arrayOf("telephonyManager","sessionId","context"),
        THROUGHPUT_TYPE to arrayOf("sessionId"),
        "" to arrayOf()
    )
    val timeouts = mapOf(
        RADIO_PARAMS_TYPE to 5000L,
        THROUGHPUT_TYPE to 1000L,
        "" to 0L
    )
}

const val JOB_TYPE = "JOB_TYPE"

/**      VARIABLES        **/
const val K_BIT = 1024
const val BITS_IN_BYTE = 8
const val DEFAULT_SESSION_ID = "-1"
/**Default sessionId for real time*/


/**  RADIO PARAMS CONSTANT VALUES **/
const val MIN_RSSI = -113
const val MIN_RSRP = -140
const val MIN_RSRQ = -34
const val MIN_RSSNR = -20


/**   CHART SECTIONS INDEX   **/
object ThroughputIndex {
    const val TX = 0
    const val RX = 1
}
object ServingCellIndex {
    const val RSSI: Int = 0
    const val RSRP: Int = 1
    const val RSQR: Int = 2
    const val RSSNR: Int = 3
}




