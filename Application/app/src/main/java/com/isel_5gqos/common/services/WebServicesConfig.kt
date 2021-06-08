package com.isel_5gqos.common.services

const val MANAGEMENT_SYSTEM_URL = "http://192.68.221.41:8080/mobix"

/**USER ENDPOINTS**/
const val USER_LOGIN_URI = "${MANAGEMENT_SYSTEM_URL}/user/login"
const val REGISTER_MOBILE_DEVICE = "${MANAGEMENT_SYSTEM_URL}/probe/register"
const val REFRESH_TOKEN = "${MANAGEMENT_SYSTEM_URL}/user/refresh"
fun getTestPlan (deviceId : Int , testPlanId : String) = "${MANAGEMENT_SYSTEM_URL}/probe/$deviceId/test-plan/${testPlanId}/complete"

/**END**/
