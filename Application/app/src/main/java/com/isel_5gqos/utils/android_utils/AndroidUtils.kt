package com.isel_5gqos.utils.android_utils

import android.content.Context
import android.widget.Toast

class AndroidUtils {

    companion object{

        fun makeBurnedToast(context: Context, message : String ){
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }

        fun makeRawToast(context: Context, message : String ){
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

    }
}