package com.isel_5gqos.models

import androidx.lifecycle.*

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

    fun observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        liveData.observeOnce(lifecycleOwner, observer)
    }
}

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}
