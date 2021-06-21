package com.isel_5gqos.workers.work.works

import android.content.Context
import com.isel_5gqos.dtos.NavigationDto
import com.isel_5gqos.dtos.TestDto
import com.isel_5gqos.dtos.TestPlanResultDto
import com.isel_5gqos.utils.DateUtils
import com.isel_5gqos.utils.mobile_utils.LocationUtils

interface IWorks {

    fun work (test : TestDto, resultDto: TestPlanResultDto, onPostExecute : (TestPlanResultDto) -> Unit)
}