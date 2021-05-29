package com.isel_5gqos.factories

import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.isel_5gqos.QosApp
import com.isel_5gqos.models.TestViewModel

const val TEST_FACTORY = "TEST_FACTORY"

class TestFactory(bundle: Bundle?,val username:String):AbstractFactory(bundle) {
    override fun getModel(): ViewModel = TestViewModel(username)

    override fun getParcelableValue(): String = TEST_FACTORY
}