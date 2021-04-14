package com.isel_5gqos.Models.Models

import androidx.lifecycle.ViewModel

class QosModel (): ViewModel() {
    companion object {
        val logins = mapOf("Afonso" to "Nobre","Ricardo" to "Silva")
    }

    fun login(username : String , password:String) : Boolean{
        return logins[username] == password;
    }
}