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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.isel_5gqos.QosApp
import com.isel_5gqos.R
import com.isel_5gqos.activities.DashboardActivity
import com.isel_5gqos.common.MEGABYTE
import com.isel_5gqos.models.SystemViewModel
import com.isel_5gqos.models.TestViewModel
import kotlinx.android.synthetic.main.fragment_info.*


class FragmentInfo : Fragment() {
   /* private val systemInfoModel by lazy {
        ViewModelProvider(this)[SystemViewModel::class.java]
    }

    private val testModel by lazy {
        ViewModelProvider(this).get(TestViewModel::class.java)
    }

    private val dashboardActivity by lazy {
        requireActivity() as DashboardActivity
    }
*/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        database_file_path_txt.text = QosApp.db.openHelper.readableDatabase.path

        val pageSize = QosApp.db.openHelper.readableDatabase.pageSize
        val cursor = QosApp.db.openHelper.readableDatabase.query("Select page_count from pragma_page_count")
        var pageCount = 0L

        if (cursor.moveToFirst())
            pageCount = cursor.getLong(0)

        database_size_txt.text =
            if (pageCount == 0L) getString(R.string.na)
            else String.format(getString(R.string.database_size), pageSize * pageCount / 1024)


        val internalStatFs = StatFs(Environment.getRootDirectory().absolutePath)

        val externalStatFs = StatFs(Environment.getExternalStorageDirectory().absolutePath)

        val internalFree: Long = internalStatFs.availableBlocksLong * internalStatFs.blockSizeLong / (MEGABYTE)
        val externalFree: Long = externalStatFs.availableBlocksLong * externalStatFs.blockSizeLong / (MEGABYTE)


        device_free_storage_txt.text = String.format(getString(R.string.mb_of_free_space), internalFree + externalFree)

        device_name_txt.text = Build.MODEL
        android_version_txt.text = Build.VERSION.RELEASE

        android_imei_txt.text = getString(R.string.na_in_android_10_plus)
        imsi_txt.text = getString(R.string.na_in_android_10_plus)
        operator_txt.text = getString(R.string.na)
        sn_txt.text = getString(R.string.na_in_android_10_plus)

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            val telephonyManager = requireContext().getSystemService(TelephonyManager::class.java)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                android_imei_txt.text = telephonyManager.deviceId.toString()
                imsi_txt.text = telephonyManager.subscriberId
                sn_txt.text = telephonyManager.simSerialNumber
            }
            operator_txt.text = telephonyManager.networkOperatorName
        }
    }
}