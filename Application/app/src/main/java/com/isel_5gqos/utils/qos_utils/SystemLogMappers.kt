package com.isel_5gqos.utils.qos_utils

interface ILogMapper{
    fun map (props : SystemLogProperties) : String
}

class EmptyMapper : ILogMapper {
    override fun map(props: SystemLogProperties): String = ""
}

class MapperWithCause : ILogMapper {
    override fun map(props: SystemLogProperties): String = " \"details\" : { \"cause\" :  \"${props.cause}\" "
}

class MapperWithTestPlanId : ILogMapper {
    override fun map(props: SystemLogProperties): String = " \"detail\" : {\"testPlanId\" : \"${props.testPlanId}\"}"
}

class MapperWithTestPlanIdAndCause : ILogMapper{
    override fun map(props: SystemLogProperties): String = " \"detail\" : { \" testPlanId \" : \"${props.testPlanId}\"}, { \"cause\" :  \"${props.cause}\" }"
}

class MapperWithTestId : ILogMapper {
    override fun map(props: SystemLogProperties): String = " \"detail\" : { \"testId\" : ${props.testId}}"
}

class MapperWithTestIdAndCause : ILogMapper {
    override fun map(props: SystemLogProperties): String = " \"detail\": { \"testId\" : ${props.testId}, { \"cause\" :  \"${props.cause}\" }"
}
