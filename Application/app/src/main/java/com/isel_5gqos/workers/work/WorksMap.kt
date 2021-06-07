package com.isel_5gqos.workers.work

import com.isel_5gqos.workers.work.works.PingWork

class WorksMap {

    companion object {

        val worksMap = mapOf(
           WorkTypeEnum.PING to PingWork()
        )

    }

}