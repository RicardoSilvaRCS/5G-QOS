package com.isel_5gqos.workers.work.works

import com.isel_5gqos.dtos.TestDto
import com.isel_5gqos.dtos.TestResultDto

interface IWorks {

    fun work (test : TestDto, onPostExecute : (TestResultDto) -> Unit)

}