package com.isel_5gqos.jobs.works

import com.isel_5gqos.jobs.JobParametersEnum

interface IWorks {

    fun work (params: Map<JobParametersEnum, Any?>)

    fun getWorkTimeout () : Long

    fun getWorkParameters():Array<JobParametersEnum>
}