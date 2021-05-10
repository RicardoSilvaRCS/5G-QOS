package com.isel_5gqos.jobs.works

interface IWorks {

    fun work (params: Map<String, Any?>)

    fun getWorkTimeout () : Long

    fun getWorkParameters():Array<String>
}