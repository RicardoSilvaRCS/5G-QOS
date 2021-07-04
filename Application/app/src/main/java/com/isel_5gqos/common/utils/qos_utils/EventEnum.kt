package com.isel_5gqos.common.utils.qos_utils

enum class EventEnum () {
    CONTROL_CONNECTION_OK,
    CONTROL_CONNECTION_ERROR,
    TESTPLAN_SCHEDULED,
    TESTPLAN_STARTED,
    TESTPLAN_FINISHED,
    TESTPLAN_ERROR,
    TEST_START,
    TEST_END,
    TEST_ERROR
}




/**
 * CONTROL_CONNECTION_OK (sem details)
 * CONTROL_CONNECTION_ERROR (details: {“cause”:  “<descrição do erro>”}
 * TESTPLAN_SCHEDULED (detail: {“testPlanId”: <test_plan_id>})
 * TESTPLAN_STARTED (detail: {“testPlanId”: <test_plan_id>})
 * TESTPLAN_FINISHED (detail: {“testPlanId”: <test_plan_id>})
 * TESTPLAN_ERROR (detail: {“testPlanId”: <test_plan_id>}, {“cause”:  “<descrição do erro>”}))
 * TEST_START (detail: {“testId”: <test_id>})
 * TEST_END (detail: {“testId”: <test_id>})
 * TEST_ERROR (detail: {“testId”: <test_id>, {“cause”:  “<descrição do erro>”})
 * */