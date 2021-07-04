package com.isel_5gqos.activities

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.isel_5gqos.R
import com.isel_5gqos.common.*
import com.isel_5gqos.factories.QosFactory
import com.isel_5gqos.models.QosViewModel
import com.isel_5gqos.models.observeOnce
import com.isel_5gqos.common.utils.android_utils.AndroidUtils
import com.isel_5gqos.workers.scheduleRefreshTokenWorker

class SplashActivity : AppCompatActivity() {

    private lateinit var qosFactory: QosFactory
    private val model: QosViewModel by lazy {
        ViewModelProvider(this, qosFactory)[QosViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        qosFactory = QosFactory(savedInstanceState)

        requestAppPermissions()

    }

    //<editor-fold name="EVENTS">

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if(requestCode == APP_PERMISSIONS && grantResults[0] != PackageManager.PERMISSION_GRANTED){
            AndroidUtils.makeRawToast(this, "Please Grant All the permissions")
            finish()
        }
        else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            login()
        }
    }

    //</editor-fold>

    //<editor-fold name="AUX FUNCTIONS">

    private fun requestAppPermissions () {

        val permissionsToGrant = mutableListOf<String>()

        if(checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionsToGrant.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if(checkSelfPermission(android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            permissionsToGrant.add(android.Manifest.permission.INTERNET)
        }
        if(checkSelfPermission(android.Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionsToGrant.add(android.Manifest.permission.ACCESS_NETWORK_STATE)
        }
        if(checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionsToGrant.add(android.Manifest.permission.READ_PHONE_STATE)
        }
        if(checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionsToGrant.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissionsToGrant.isNotEmpty()) {
            requestPermissions(permissionsToGrant.toTypedArray(), APP_PERMISSIONS)
        }else{
            login()
        }
    }

    private fun login () {
        model.getLoggedUser().observeOnce(this) {
            if(it == null){
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            else{

                val serialNumber = AndroidUtils.getPreferences( MOBILE_ID_KEY, applicationContext)

                model.refreshToken(it.user,it.token,serialNumber!!)

                model.liveData.observeOnce(this) { user ->

                    if(user.userToken.isEmpty()){
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else{
                        val intent = Intent(this, DashboardActivity::class.java)

                        intent.putExtra(TOKEN, user.userToken)
                        intent.putExtra(USER, user.username)
                        AndroidUtils.setPreferences(TOKEN_FOR_WORKER,user.userToken,applicationContext)

                        /**Launch Refresh Token Worker**/
                        scheduleRefreshTokenWorker(user.username,user.deviceId)

                        startActivity(intent)
                        finish()
                    }
                }

            }
        }
    }

    //</editor-fold>

}