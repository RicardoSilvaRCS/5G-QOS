package com.isel_5gqos.common


/**        Permissions           **/

const val APP_PERMISSIONS = 10

/**        In case to make exclusive permissions treatment        **/
const val ACCESS_FINE_LOCATION_PERMISSION = 1
const val INTERNET_PERMISSION = 2
const val ACCESS_NETWORK_STATE_PERMISSION = 3
const val READ_PHONE_STATE_PERMISSION = 4

/**        INTENT VARIABLES           **/
const val USER = "USER"
//const val TOKEN = "TOKEN"

/**        TAGS           **/
const val TAG = "5GQosApp"
const val WORKER_TAG = "WORKER_TAG"
const val RADIO_PARAMETERS_WORKER_ID = "RAD_PAR_ID"

/**   WORKER INPUT DATA   **/
const val SESSION_ID = "SESSION_ID"
const val DB_SAVE = "DB_SAVE"
const val PROGRESS = "PROGRESS"
const val TOKEN = "TOKEN"
const val CREDENTIALS = "USER_LOGIN_CREDENTIALS"

const val JOB_TYPE = "JOB_TYPE"

/** NOTIFICATION CHANNEL **/

const val CHANNEL_ID = "5G_QOS_NOTIFICATION_CHANNEL"

/** END **/

/**      VARIABLES        **/
const val K_BIT = 1024
const val BITS_IN_BYTE = 8
const val DEFAULT_SESSION_ID = "-1"
const val SECRET_PASSWORD = "SECRET_PASSWORD"
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
object StrongestNeighborIndex {
    const val RSSI_GSM: Int = 0
    const val RSSI_WCDMA: Int = 1
    const val RSRP_LTE: Int = 2
    const val NUMBER: Int = 3
}

/** GLOBAL CONSTANTS   **/
const val DATABASE_NAME = "Qos-Db"
const val KILOBYTE = 1024
const val MEGABYTE = KILOBYTE * KILOBYTE


//TODO FAZER CLASS QUE RECEBE UM ID E UM CONTEXT E VAI BUSCAR AO "R" A STRING ASSOCIADA
/**Volley Errors Labels**/

/**GENERIC ERRORS**/

const val NO_CONNECTION_ERROR = "Please check your internet connection"
const val TIMEOUT_ERROR = "There is a problem with the server"
const val GENERIC_ERROR = "There is a problem please contact the it team!"

/**END**/

/**CREDENTIALS**/
const val AUTH_FAILED_ERROR = "Authentication token is no longer available"
const val INVALID_CREDENTIALS = "There is a problem with the server"
const val TOKEN_NOT_REFRESHED = "The token wasn't able to be refreshed please insert your credentials"

/****/



