package com.isel_5gqos.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputEditText
import com.isel_5gqos.Common.QoSApp
import com.isel_5gqos.Common.db.asyncTask
import com.isel_5gqos.models.QosViewModel
import com.isel_5gqos.R
import com.isel_5gqos.dtos.SessionDto
import com.isel_5gqos.factories.QosFactory
import java.sql.Date
import java.sql.Timestamp

const val USER = "USER"
const val TOKEN = "TOKEN"

class MainActivity : AppCompatActivity() {

    private lateinit var qosFactory: QosFactory
    private val model:QosViewModel by lazy {
        ViewModelProviders.of(this,qosFactory)[QosViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        qosFactory = QosFactory(savedInstanceState)

        val loginButton = findViewById<Button>(R.id.next_button)
        val cancelButton = findViewById<Button>(R.id.cancel_button)

        val username = findViewById<TextInputEditText>(R.id.username_edit_text)
        val password = findViewById<TextInputEditText>(R.id.password_edit_text)

        val session = SessionDto(
            id = QoSApp.sessionId,
            sessionName = "test",
            username = "ricardo.silva@isel.pt",
            beginDate = Timestamp(System.currentTimeMillis()),
            endDate = Timestamp(System.currentTimeMillis() + (60*1000).toLong())
        )

        asyncTask({ QoSApp.db.sessionDao().insert(session) }, {})

        loginButton.setOnClickListener {
            val user = username.text.toString()
            val pass = password.text.toString()

            if (pass.isBlank() || user.isBlank()) {
                Toast.makeText(this, "Please insert your credentials", Toast.LENGTH_SHORT).show()
            } else {
                model.login(user,pass)
                model.observe(this) {
                    val intent = Intent(this, DashboardActivity::class.java)

                    intent.putExtra(TOKEN, it.userToken)
                    intent.putExtra(USER, it.username)

                    startActivity(intent)
                }
            }
        }

        cancelButton.setOnClickListener {
            username.setText("")
            password.setText("")
        }

    }
}


