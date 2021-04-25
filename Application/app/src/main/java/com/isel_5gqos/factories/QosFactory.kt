package com.isel_5gqos.factories

import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.isel_5gqos.common.QoSApp
import com.isel_5gqos.models.QosViewModel

const val QOS_FACTORY = "QOS_FACTORY"

class QosFactory(bundle: Bundle?) : AbstractFactory(bundle) {
    override fun getModel(): ViewModel = QosViewModel(QoSApp.msWebApi)

    override fun getParcelableValue(): String = QOS_FACTORY

}