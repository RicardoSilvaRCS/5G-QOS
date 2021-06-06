package com.isel_5gqos.activities

import android.R.attr.label
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputEditText
import com.isel_5gqos.QosApp
import com.isel_5gqos.R
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.common.db.entities.User
import com.isel_5gqos.factories.QosFactory
import com.isel_5gqos.models.QosViewModel
import com.isel_5gqos.utils.android_utils.AndroidUtils
import com.isel_5gqos.workers.scheduleRefreshTokenWorker
import java.util.*


class LoginActivity : AppCompatActivity() {

    private lateinit var qosFactory: QosFactory
    private val model: QosViewModel by lazy {
        ViewModelProviders.of(this, qosFactory)[QosViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestAppPermissions()

        qosFactory = QosFactory(savedInstanceState)

//        bypassLoginForDebug(savedInstanceState)

        val loginButton = findViewById<Button>(R.id.next_button)
        val cancelButton = findViewById<Button>(R.id.cancel_button)

        val username = findViewById<TextInputEditText>(R.id.username_edit_text)
        val password = findViewById<TextInputEditText>(R.id.password_edit_text)

        val mobileIdText = findViewById<Button>(R.id.mobile_id)

        val mobileId = getMobileID()



        mobileIdText.text = mobileId
        mobileIdText.setOnClickListener{
            val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(MOBILE_ID_KEY, mobileId)
            clipboard.setPrimaryClip(clip)
            AndroidUtils.makeRawToast(this,"Copied!")
        }

        loginButton.setOnClickListener {

            val user = username.text.toString()
            val pass = password.text.toString()

            if (pass.isBlank() || user.isBlank()) {
                Toast.makeText(this, "Please insert your credentials", Toast.LENGTH_SHORT).show()
            } else {
                model.login(user, pass, mobileId)
                model.observe(this) {
                    val intent = Intent(this, DashboardActivity::class.java)

                    //TODO: Debate if intent is really needed
                    intent.putExtra(TOKEN, it.userToken)
                    intent.putExtra(USER, it.username)

                    /**Launch Refresh Token Worker**/
                    scheduleRefreshTokenWorker(it.username,it.userToken,it.username)

                    startActivity(intent)

                    finish()
                }
            }

        }

        cancelButton.setOnClickListener {
            username.setText("")
            password.setText("")
        }


    }

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

        if (permissionsToGrant.isNotEmpty())
            requestPermissions(permissionsToGrant.toTypedArray(), APP_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if(requestCode == APP_PERMISSIONS && grantResults[0] != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "Please Grant All the permissions", Toast.LENGTH_SHORT).show()
        }
        else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    //<editor-fold name="AUX FUNCTIONS">
    private fun getMobileID () : String {

        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        var mobileId = sharedPref.getString( MOBILE_ID_KEY , "")

        if(mobileId.isNullOrEmpty() && sharedPref != null){
            with (sharedPref.edit()) {
                mobileId = UUID.randomUUID().toString()
                putString(MOBILE_ID_KEY, mobileId )
                apply()
            }
        }

        return mobileId ?: ""
    }

    //</editor-fold>

    //<editor-fold name="DEBUG TOOLS">

    private fun bypassLoginForDebug(savedInstanceState: Bundle?){
        qosFactory = QosFactory(savedInstanceState)
        val user = User(
            regId = QosApp.sessionId,
            username = "username",
            timestamp = System.currentTimeMillis() + (60 * 1000).toLong(),
            loggedOut = false,
            credentials = Base64.encodeToString("username:userDto.userToken".toByteArray(charset("UTF-8")), Base64.DEFAULT).replace("\n", "")
        )


        asyncTask({ QosApp.db.userDao().insert(user) }){

            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra(USER, user.username)

            startActivity(intent)
        }

    }

    //</editor-fold>
}


