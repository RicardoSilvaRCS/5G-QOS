package com.isel_5gqos.jobs

import com.isel_5gqos.jobs.works.RadioParametersWork
import com.isel_5gqos.jobs.works.ThroughPutWork

class WorksMap {

    companion object{

        val worksMap = mapOf(
            WorkTypesEnum.RADIO_PARAMS_TYPES.workType to RadioParametersWork(),
            WorkTypesEnum.THROUGHPUT_TYPE.workType to ThroughPutWork()
        )

    }
}