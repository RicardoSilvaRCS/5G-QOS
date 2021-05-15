package com.isel_5gqos.models

import com.isel_5gqos.QosApp
import com.isel_5gqos.dtos.SystemInfoDto

class SystemViewModel:AbstractModel<SystemInfoDto>({ SystemInfoDto(0,0) }){

    fun getDatabaseInfo()=QosApp.db.systemInfoDao().getDatabaseSize()
}