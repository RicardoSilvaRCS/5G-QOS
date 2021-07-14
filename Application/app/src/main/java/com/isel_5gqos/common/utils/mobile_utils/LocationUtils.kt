package com.isel_5gqos.common.utils.mobile_utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.telephony.TelephonyManager

class LocationUtils {

    companion object {

        @SuppressLint("MissingPermission")
        fun getLocation (context: Context): Location? {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }

        fun getLocationDto(telephonyManager: TelephonyManager?, context: Context): Triple<String?,Double?,Double?> {

            val lastKnownLocation = getLocation(context = context)
            return Triple(
                telephonyManager!!.networkOperatorName,
                lastKnownLocation?.latitude,
                lastKnownLocation?.longitude
            )

        }

    }
}