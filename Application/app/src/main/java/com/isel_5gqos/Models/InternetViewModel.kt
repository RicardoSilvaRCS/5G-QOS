package com.isel_5gqos.Models

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.qiniu.android.netdiag.Output
import com.qiniu.android.netdiag.Ping

class InternetViewModel (): ViewModel() {

    val liveData: MutableLiveData<com.isel_5gqos.Models.Ping> by lazy {
        MutableLiveData<com.isel_5gqos.Models.Ping>()
    }

    val pingResult: com.isel_5gqos.Models.Ping get() = liveData.value ?: Ping(0,"")

    fun observe(owner: LifecycleOwner, observer: (com.isel_5gqos.Models.Ping) -> Unit) {
        liveData.observe(owner, androidx.lifecycle.Observer { observer(it) })
    }

    fun getResults (url : String , numberOfTries : Int) {

        liveData.postValue(com.isel_5gqos.Models.Ping(
            url = url,
            numberOfTries = numberOfTries
        ))

        Ping.start(url, numberOfTries,
            Logger(), Ping.Callback {
                liveData.value!!.avg = it.avg
                println(pingResult.pingInfos.size)
                println(pingResult)
            })
    }

    inner class Logger() : Output {
        override fun write(line: String?) {

            println(line)

            if(!line!!.contains("ttl") || pingResult.pingInfos.size == pingResult.numberOfTries)
                return

            val pingLine = line.split(' ')

            val pingInfo = PingInfo(
                seq = pingLine[4].split('=')[1].toInt(),
                ttl = pingLine[5].split('=')[1].toInt(),
                time = pingLine[6].split('=')[1].toDouble()
            )

            liveData.value!!.pingInfos.add(pingInfo)
        }
    }
}

