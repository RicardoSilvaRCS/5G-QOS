package com.isel_5gqos.common.db

import android.os.AsyncTask

fun asyncTask(method: () -> Unit, onPostExecute: () -> Unit) {
    object : AsyncTask<Unit, Int, Unit>() {
        override fun doInBackground(vararg p0: Unit?) {
            method()
        }

        override fun onPostExecute(result: Unit?) {
            onPostExecute()
        }
    }.execute()
}