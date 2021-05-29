package com.isel_5gqos.common.db

import android.os.AsyncTask

fun asyncTask(doInBackground: () -> Unit, onPostExecute: () -> Unit = {}) {
    object : AsyncTask<Unit, Int, Unit>() {
        override fun doInBackground(vararg p0: Unit?) {
            doInBackground()
        }

        override fun onPostExecute(result: Unit?) {
            onPostExecute()
        }
    }.execute()
}

fun <T, R> executeAsyncTaskGeneric(function: (T) -> R, param: T, onSuccess: (R) -> Unit): AsyncTask<T, Int, R> =
    object : AsyncTask<T, Int, R>() {
        override fun doInBackground(vararg params: T): R = function(param)
        override fun onPostExecute(result: R) = onSuccess(result)
    }
