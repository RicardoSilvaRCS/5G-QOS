package com.isel_5gqos.jobs

import com.isel_5gqos.jobs.jobs.RadioParametersJob
import com.isel_5gqos.jobs.jobs.ThroughPutJob

class JobsMap {

    companion object{

        val worksMap = mapOf(
            JobTypeEnum.RADIO_PARAMS_TYPE.jobType to RadioParametersJob(),
            JobTypeEnum.THROUGHPUT_TYPE.jobType to ThroughPutJob()
        )

    }
}