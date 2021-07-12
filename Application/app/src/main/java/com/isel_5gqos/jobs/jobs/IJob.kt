package com.isel_5gqos.jobs.jobs

import com.isel_5gqos.jobs.JobParametersEnum

interface IJob {

    fun job (params: Map<JobParametersEnum, Any?>)

    fun getJobTimeout () : Long

    fun getJobParameters():Array<JobParametersEnum>
}