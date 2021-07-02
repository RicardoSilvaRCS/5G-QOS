package com.isel_5gqos.activities

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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

        qosFactory = QosFactory(savedInstanceState)

//        bypassLoginForDebug(savedInstanceState)

        val loginButton = findViewById<Button>(R.id.next_button)
        val cancelButton = findViewById<Button>(R.id.cancel_button)

        val username = findViewById<TextInputEditText>(R.id.username_edit_text)
        val password = findViewById<TextInputEditText>(R.id.password_edit_text)

        val mobileIdInput = findViewById<TextInputLayout>(R.id.mobile_id)
        val mobileIdInputText = findViewById<TextInputEditText>(R.id.mobile_id_edit_text)

        var serialNumber = AndroidUtils.getPreferences(MOBILE_ID_KEY,applicationContext)

        if(!serialNumber.isNullOrEmpty()){
            mobileIdInput.visibility = TextInputEditText.INVISIBLE
            mobileIdInputText.visibility = TextInputEditText.INVISIBLE
            mobileIdInputText.setText(serialNumber)
        }

        loginButton.setOnClickListener {

            val user = username.text.toString()
            val pass = password.text.toString()
            if(serialNumber.isNullOrEmpty()){
                serialNumber = mobileIdInputText.text.toString()
            }

            if (pass.isBlank() || user.isBlank() || serialNumber.isNullOrEmpty()) {
                Toast.makeText(this, "Please insert your credentials", Toast.LENGTH_SHORT).show()
            } else {
                model.login(user, pass, serialNumber!!)
                model.observe(this) {

                    if(it.userToken.isEmpty()) return@observe

                    val intent = Intent(this, DashboardActivity::class.java)

                    intent.putExtra(TOKEN, it.userToken)
                    intent.putExtra(USER, it.username)

                    AndroidUtils.setPreferences(MOBILE_ID_KEY,serialNumber,applicationContext)
                    AndroidUtils.setPreferences(TOKEN_FOR_WORKER,it.userToken,applicationContext)

                    /**Launch Refresh Token Worker**/
                    scheduleRefreshTokenWorker(it.username,it.deviceId)

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

    //<editor-fold name="AUX FUNCTIONS">

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


