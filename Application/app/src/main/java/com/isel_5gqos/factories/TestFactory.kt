package com.isel_5gqos.factories

import android.os.Bundle
import androidx.lifecycle.ViewModel

class TestFactory(bundle: Bundle):AbstractFactory(bundle) {
    override fun getModel(): ViewModel {
        TODO("Not yet implemented")
    }

    override fun getParcelableValue(): String {
        TODO("Not yet implemented")
    }
}