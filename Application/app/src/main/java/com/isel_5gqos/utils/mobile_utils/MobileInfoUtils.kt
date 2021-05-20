package com.isel_5gqos.utils.mobile_utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager

class MobileInfoUtils {

    companion object {

        fun getImei(context: Context, telephonyManager: TelephonyManager) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
            )
        } else {
            telephonyManager.imei
        }

<<<<<<< Updated upstream
=======
        fun getDeviceSerialNumber (): String {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                return "3393182b"
            }

            return Build.getSerial()
        }
>>>>>>> Stashed changes
    }
}