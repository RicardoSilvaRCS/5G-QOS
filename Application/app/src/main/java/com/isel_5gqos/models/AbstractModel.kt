package com.isel_5gqos.models

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel

abstract class AbstractModel<T>(val tMaker: () -> T) : ViewModel() {
    val liveData: MutableLiveData<T> by lazy {
        MutableLiveData<T>()
    }

    val value: T get() = liveData.value ?: tMaker()

    fun observe(owner: LifecycleOwner, observer: (T) -> Unit) {
        liveData.observe(owner, {
            observer(it)
        })
    }
}