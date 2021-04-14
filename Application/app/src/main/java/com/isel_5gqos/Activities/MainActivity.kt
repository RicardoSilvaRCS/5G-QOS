package com.isel_5gqos.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputEditText
import com.isel_5gqos.Models.InternetViewModel
import com.isel_5gqos.Models.QosModel
import com.isel_5gqos.R

const val USER = "USER"
const val PASS = "PASS"

class MainActivity : AppCompatActivity() {

    val model by lazy {
        ViewModelProviders.of(this)[QosModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //val text = findViewById<TextView>(R.id.text)

        val model2 = InternetViewModel()

        model2.observe(this) {
        }

        model2.getResults("google.com",25)

        val loginButton = findViewById<Button>(R.id.next_button)
        val cancelButton = findViewById<Button>(R.id.cancel_button)

        val username = findViewById<TextInputEditText>(R.id.username_edit_text)
        val password = findViewById<TextInputEditText>(R.id.password_edit_text)

        loginButton.setOnClickListener {
            val user = username.text.toString()
            val pass = password.text.toString()

            if(pass.isNullOrBlank() || user.isNullOrBlank()) {
                Toast.makeText(this, "Please insert your credentials", Toast.LENGTH_SHORT).show()

            }
            else{
                if(model.login(user, pass)){
                    val intent = Intent(this,DashboardActivity::class.java)

                    intent.putExtra(USER,username.text.toString())
                    intent.putExtra(PASS,password.text.toString())

                    startActivity(intent)
                } else {
                    username.setText("")
                    password.setText("")
                    Toast.makeText(this,"Invalid Credentials",Toast.LENGTH_SHORT).show()
                }
            }
        }

        cancelButton.setOnClickListener{
            username.setText("")
            password.setText("")
        }

    }
}


