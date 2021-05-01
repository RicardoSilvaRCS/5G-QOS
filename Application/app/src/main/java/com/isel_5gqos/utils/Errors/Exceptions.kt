package com.isel_5gqos.utils.Errors

import com.isel_5gqos.QosApp
import java.util.*

class Exceptions : Exception {

    constructor (message : String){
        QosApp.db.errorDao().insert(
            com.isel_5gqos.common.db.entities.Error(
                regId = UUID.randomUUID().toString(),
                description =  message,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    constructor (ex : Exception){
        QosApp.db.errorDao().insert(
            com.isel_5gqos.common.db.entities.Error(
                regId = UUID.randomUUID().toString(),
                description =  ex.message?: ex.toString(),
                timestamp = System.currentTimeMillis()
            )
        )
    }
}