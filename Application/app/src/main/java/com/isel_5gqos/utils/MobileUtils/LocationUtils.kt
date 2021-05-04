package com.isel_5gqos.utils.MobileUtils

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.telephony.TelephonyManager
import com.isel_5gqos.dtos.LocationDto

class LocationUtils {

    companion object {

        @SuppressLint("MissingPermission")
        fun getLocation(telephonyManager: TelephonyManager?, context: Context): LocationDto {

            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            return LocationDto(
                networkOperatorName = telephonyManager!!.networkOperatorName,
                latitude = lastKnownLocation?.latitude,
                longitude = lastKnownLocation?.longitude
            )

        }


    }
}