package com.isel_5gqos.workers.work.works

import com.isel_5gqos.dtos.TestDto
import com.isel_5gqos.dtos.TestResultDto
import com.qiniu.android.netdiag.Ping

class PingWork : IWorks {

    override fun work(test : TestDto, onPostExecute : (TestResultDto) -> Unit ) {
//        var ping = Thread {
//            Ping.start(url, numberOfTries,
//                Logger(), Ping.Callback {
//                    val ping = com.isel_5gqos.models.Ping(
//                        url = pingResult.url,
//                        numberOfTries = pingResult.numberOfTries,
//                        pingInfos = pingResult.pingInfos,
//                        avg = it.avg,
//                        minMs = it.min,
//                        maxMs = it.max
//                    )
//                    liveData.postValue(ping)
//                    println(pingResult.pingInfos.size)
//                    println(pingResult)
//                })
//        }
//
//        ping.start()
//        ping.join()
    }

}