package com.isel_5gqos.common.db

import com.isel_5gqos.QosApp
import com.isel_5gqos.common.db.entities.Worker


class WorkerUtils {
    companion object {
        fun getWorkerById(workerId: String) = QosApp.db.workerDao().getWorkersByTag(workerId)

        fun addWorkerToDb(worker: Worker) {
            asyncTask({ QosApp.db.workerDao().insertWorker(worker) }) {}
        }

        fun signalWorkerToFinish(workerTag: String) {
            asyncTask({ QosApp.db.workerDao().signalWorkerByTagToFinish(workerTag) }) {}
        }

    }
}