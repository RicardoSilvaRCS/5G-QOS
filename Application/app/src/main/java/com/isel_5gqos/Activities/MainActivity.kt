package com.isel_5gqos.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputEditText
import com.isel_5gqos.Models.Models.QosModel
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

        //val model = InternetModel()

        /**model.observe(this) {
            //text.text = it.result
            text.text = it.avg.toString()
        }**/

        //model.getResults()

        val loginButton = findViewById<Button>(R.id.next_button)
        val cancelButton = findViewById<Button>(R.id.cancel_button)

        val username = findViewById<TextInputEditText>(R.id.username_edit_text)
        val password = findViewById<TextInputEditText>(R.id.password_edit_text)

        username.addTextChangedListener {
            if(it!!.isNotEmpty() && password.text!!.isNotEmpty())
                loginButton.isEnabled = true
        }

        username.addTextChangedListener {
            if(it!!.isNotEmpty() && username.text!!.isNotEmpty())
                loginButton.isEnabled = true
        }

        loginButton.isEnabled = false

        loginButton.setOnClickListener {
            val user = username.text.toString()
            val pass = password.text.toString()

            val login = model.login(user, pass)

            if(login){
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

        cancelButton.setOnClickListener{
            username.setText("")
            password.setText("")
        }

    }
}


