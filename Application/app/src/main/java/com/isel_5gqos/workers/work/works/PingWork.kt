package com.isel_5gqos.workers.work.works

import com.isel_5gqos.dtos.TestDto
import com.isel_5gqos.dtos.TestResultDto
import com.qiniu.android.netdiag.Output
import com.qiniu.android.netdiag.Ping

class PingWork : IWorks {

    override fun work(test: TestDto, onPostExecute: (TestResultDto) -> Unit) {

        Ping.start(test.server.host, test.nPings,
            {
                println(it.toString())
            }, {
                println(it.toString())
                onPostExecute(TestResultDto())
            })
    }

}