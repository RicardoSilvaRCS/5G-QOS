package com.isel_5gqos.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
import com.isel_5gqos.workers.scheduleRefreshTokenWorker

const val USER = "USER"
const val TOKEN = "TOKEN"

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

        loginButton.setOnClickListener {

            val user = username.text.toString()
            val pass = password.text.toString()

            if (pass.isBlank() || user.isBlank()) {
                Toast.makeText(this, "Please insert your credentials", Toast.LENGTH_SHORT).show()
            } else {
                model.login(user, pass)
                model.observe(this) {
                    val intent = Intent(this, DashboardActivity::class.java)
                    //TODO: Debate if intent is really needed
                    intent.putExtra(TOKEN, it.userToken)
                    intent.putExtra(USER, it.username)

                    /**Launch Refresh Token Worker**/
                    scheduleRefreshTokenWorker(it.userToken,it.username)

                    startActivity(intent)
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


    /**DEBUG TOOLS**/

    private fun bypassLoginForDebug(savedInstanceState: Bundle?){
        qosFactory = QosFactory(savedInstanceState)
        val user = User(
            regId = QosApp.sessionId,
            username = "username",
            token = "userDto.userToken",
            timestamp = System.currentTimeMillis() + (60 * 1000).toLong(),
            loggedOut = false
        )


        asyncTask({ QosApp.db.userDao().insert(user) }){

            val intent = Intent(this, DashboardActivity::class.java)

            intent.putExtra(TOKEN, user.token)
            intent.putExtra(USER, user.username)

            startActivity(intent)
        }

    }

    /**END**/
}


