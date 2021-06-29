package com.isel_5gqos.factories

import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.isel_5gqos.QosApp
import com.isel_5gqos.models.QosViewModel
import com.isel_5gqos.models.SystemViewModel
import com.isel_5gqos.repositories.QosRepository
import com.isel_5gqos.repositories.SystemRepository

const val SYSTEM_FACTORY = "SYSTEM_FACTORY"

class SystemFactory(bundle: Bundle?) : AbstractFactory(bundle) {
    override fun getModel(): ViewModel = SystemViewModel(SystemRepository())

    override fun getParcelableValue(): String = SYSTEM_FACTORY

}