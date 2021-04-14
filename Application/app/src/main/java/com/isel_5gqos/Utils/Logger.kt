package com.isel_5gqos.Utils

import com.qiniu.android.netdiag.Output

class Logger() : Output {
    override fun write(line: String?) {
        println(line)
    }
}