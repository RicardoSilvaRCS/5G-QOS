package com.isel_5gqos.workers.work.works

import com.isel_5gqos.dtos.TestDto
import com.isel_5gqos.dtos.TestPlanResultDto

interface IWork {

    fun work (test : TestDto, resultDto: TestPlanResultDto, onPostExecute : (TestPlanResultDto) -> Unit)
}