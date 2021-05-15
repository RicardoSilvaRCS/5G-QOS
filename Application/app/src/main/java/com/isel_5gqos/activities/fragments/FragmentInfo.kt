package com.isel_5gqos.activities.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.isel_5gqos.R
import com.isel_5gqos.common.DATABASE_NAME
import com.isel_5gqos.common.MEGABYTE
import com.isel_5gqos.models.SystemViewModel
import kotlinx.android.synthetic.main.fragment_info.*


class FragmentInfo : Fragment() {
    private val systemInfoModel by lazy {
        ViewModelProviders.of(requireActivity())[SystemViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        database_file_path_txt
//        database_size_txt
//        device_free_storage_txt

//    device_name_txt
//    android_version_txt
//    android_imei_txt

//    imsi_txt
//    operator_txt
//    sn_txt
        database_file_path_txt.text = requireContext().getDatabasePath(DATABASE_NAME).toString()

        /*systemInfoModel.getDatabaseInfo().observe(requireActivity()) {
            database_size_txt.text = "${it.page_size?.times(it.page_count ?: 0) ?: "N/A"}"
        }*/

        val internalStatFs = StatFs(Environment.getRootDirectory().absolutePath)

        val externalStatFs = StatFs(Environment.getExternalStorageDirectory().absolutePath)

        val internalFree: Long = internalStatFs.availableBlocksLong * internalStatFs.blockSizeLong / (MEGABYTE)
        val externalFree: Long = externalStatFs.availableBlocksLong * externalStatFs.blockSizeLong / (MEGABYTE)

        device_free_storage_txt.text = "${internalFree + externalFree} MB free space"

        device_name_txt.text = Build.MODEL
        android_version_txt.text = Build.VERSION.BASE_OS

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            val telephonyManager = requireContext().getSystemService(TelephonyManager::class.java)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                android_imei_txt.text = telephonyManager.deviceId.toString()
                imsi_txt.text = telephonyManager.subscriberId
                sn_txt.text = telephonyManager.simSerialNumber
            }
            operator_txt.text = telephonyManager.networkOperatorName
        } else {
            android_imei_txt.text = "N/A in android 10+"
            imsi_txt.text = "N/A in android 10+"
            operator_txt.text = "N/A"
            sn_txt.text = "N/A in android 10+"
        }
    }
}