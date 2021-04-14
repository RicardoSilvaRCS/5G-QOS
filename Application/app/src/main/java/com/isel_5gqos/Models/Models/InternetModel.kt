package com.isel_5gqos.Models.Models

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.isel_5gqos.Utils.Logger
import com.qiniu.android.netdiag.Ping

class InternetModel (): ViewModel() {
    val liveData: MutableLiveData<Ping.Result> by lazy {
        MutableLiveData<Ping.Result>()
    }

    val pingResult: Ping.Result? get() = liveData.value

    fun observe(owner: LifecycleOwner, observer: (Ping.Result) -> Unit) {
        liveData.observe(owner, androidx.lifecycle.Observer { observer(it) })
    }

    fun getResults () {
        Ping.start("www.google.com", 25,
            Logger(), Ping.Callback { liveData.postValue(it)})
    }
}