package com.isel_5gqos.models

import com.isel_5gqos.QosApp
import com.isel_5gqos.dtos.SystemInfoDto
import com.isel_5gqos.repositories.SystemRepository

class SystemViewModel(private val systemRepository: SystemRepository):AbstractModel<SystemInfoDto>({ SystemInfoDto(0,0) }){

    fun getDatabaseInfo() = systemRepository.getDatabaseInfo()
}