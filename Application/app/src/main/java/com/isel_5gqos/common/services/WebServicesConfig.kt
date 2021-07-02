package com.isel_5gqos.common.services

const val MANAGEMENT_SYSTEM_URL = "http://192.68.221.41:8080/mobix"

/**USER ENDPOINTS**/
const val USER_LOGIN_URI = "${MANAGEMENT_SYSTEM_URL}/user/login"
const val REGISTER_MOBILE_DEVICE_URI = "${MANAGEMENT_SYSTEM_URL}/probe/register"
const val REFRESH_TOKEN_URI = "${MANAGEMENT_SYSTEM_URL}/user/refresh"
val TEST_PLAN_URI : (deviceId:Int, testPlanId:String) -> String = {deviceId, testPlanId -> "${MANAGEMENT_SYSTEM_URL}/probe/$deviceId/test-plan/${testPlanId}/complete" }
val TEST_PLAN_RESULT_URI : ( deviceId:Int ) -> String = {deviceId -> "${MANAGEMENT_SYSTEM_URL}/probe/$deviceId/test-log" }
val SYSTEM_LOG : ( deviceId:Int ) -> String = {deviceId -> "${MANAGEMENT_SYSTEM_URL}/probe/$deviceId/system-log" }
val CONTROL_CONNECTION : ( deviceId:Int ) -> String = {deviceId -> "${MANAGEMENT_SYSTEM_URL}/probe/$deviceId/control-connection" }


/**END**/
