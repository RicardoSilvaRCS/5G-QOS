package com.isel_5gqos.activities.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.isel_5gqos.QosApp
import com.isel_5gqos.R
import com.isel_5gqos.common.MEGABYTE
import com.isel_5gqos.common.utils.android_utils.AndroidUtils
import com.isel_5gqos.common.utils.publisher_subscriber.SessionMessageEvent
import com.isel_5gqos.common.utils.publisher_subscriber.SessionMessageTypeEnum
import com.isel_5gqos.factories.QosFactory
import com.isel_5gqos.factories.SystemFactory
import com.isel_5gqos.models.QosViewModel
import com.isel_5gqos.models.SystemViewModel
import com.isel_5gqos.models.observeOnce
import kotlinx.android.synthetic.main.fragment_controlled_session.*
import kotlinx.android.synthetic.main.fragment_info.*
import org.greenrobot.eventbus.EventBus


class FragmentInfo : Fragment() {

    private lateinit var systemFactory: SystemFactory
    private val systemViewModel by lazy {
        ViewModelProvider(this, systemFactory)[SystemViewModel::class.java]
    }

    private lateinit var qosFactory: QosFactory
    private val model: QosViewModel by lazy {
        ViewModelProvider(this, qosFactory)[QosViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        database_file_path_txt.text = QosApp.db.openHelper.readableDatabase.path

        systemFactory = SystemFactory(savedInstanceState)
        qosFactory = QosFactory(savedInstanceState)

        val (pageSize, cursor) = systemViewModel.getDatabaseInfo()

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


        go_to_excel_directory.setOnClickListener {
            val path = "${Environment.getExternalStorageDirectory()}/Documents/QoS5G"
            val uri = Uri.parse(path)

            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(uri, "*/*")
            startActivity(intent)
        }

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

        model.getLoggedUser().observeOnce(this) {
            username_edit_text.text = it.user
        }

        model.getDeviceId().observeOnce(this) {
            device_id_edit_txt.text = it.mobileUnitId.toString()
        }

        logout_session_btn.setOnClickListener {
            EventBus.getDefault().post(SessionMessageEvent(SessionMessageTypeEnum.LOGOUT_USER))
        }

    }
}