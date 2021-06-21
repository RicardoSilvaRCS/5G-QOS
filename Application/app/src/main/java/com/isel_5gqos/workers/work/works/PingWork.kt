package com.isel_5gqos.workers.work.works

import android.util.Log
import com.isel_5gqos.dtos.*
import com.qiniu.android.netdiag.Ping
import java.util.*

class PingWork : IWorks {


    /**
     * PING LINE EXAMPLE
     *
     * [64, bytes, from, 216.58.215.142:, icmp_seq=10, ttl=115, time=108, ms]
     *
     * **/

    override fun work(test: TestDto, resultDto: TestPlanResultDto, onPostExecute: (TestPlanResultDto) -> Unit) {

        val detailedPing = mutableListOf<DetailedPingDto>()

        Ping.start(test.server.host, test.nPings,
            { line ->
                if (!line!!.contains("ttl"))
                    return@start

                val pingLine = line.split(' ')

                Log.v("PINGTEST", pingLine.toString())
                Log.v("PINGTEST", "time = " + pingLine[6].split('=')[1])
                Log.v("PINGTEST", "ttl = " +pingLine[5].split('=')[1])

                detailedPing.add(
                    DetailedPingDto(
                        from = pingLine[3],
                        bytes = pingLine[0].toInt(),
                        time = pingLine[6].split('=')[1].toFloat(),
                        ttl = pingLine[5].split('=')[1].toInt()
                    )
                )

            }, {
                Log.v("PINGTEST", "FINISHED")
                onPostExecute(
                    PingTestResultDto(
                        avgRtt = it.avg,
                        maxRtt = it.max,
                        minRtt = it.min,
                        nPings = detailedPing.size,
                        sent = it.sent,
                        lost = it.dropped,
                        lostPercent = (it.dropped.toFloat()/detailedPing.size)*100,
                        detailedPing = detailedPing,
                        date = resultDto.date,
                        navigationDto = resultDto.navigationDto,
                        probeId = resultDto.probeId,
                        testExecutionId = resultDto.testExecutionId,
                        testPlanId = resultDto.testPlanId,
                        type = resultDto.type
                    )
                )
            })
    }

}