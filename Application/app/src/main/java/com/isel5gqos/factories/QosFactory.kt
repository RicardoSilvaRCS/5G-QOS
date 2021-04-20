package com.isel5gqos.factories

import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.isel5gqos.Common.QoSApp
import com.isel5gqos.models.QosViewModel

const val QOS_FACTORY = "QOS_FACTORY"

class QosFactory(bundle: Bundle?):AbstractFactory(bundle){
    override fun getModel(): ViewModel = QosViewModel(QoSApp.msWebApi,QoSApp.api)

    override fun getParcelableValue(): String = QOS_FACTORY

}