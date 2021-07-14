package com.isel_5gqos.models

import androidx.lifecycle.ViewModel
import com.isel_5gqos.repositories.SystemRepository

class SystemViewModel(private val systemRepository: SystemRepository):ViewModel(){

    fun getDatabaseInfo() = systemRepository.getDatabaseInfo()
}