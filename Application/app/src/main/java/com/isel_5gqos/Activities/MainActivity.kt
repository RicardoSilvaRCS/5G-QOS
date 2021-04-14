package com.isel_5gqos.Activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.isel_5gqos.Models.Models.InternetModel
import com.isel_5gqos.Models.Models.QosModel
import com.isel_5gqos.R


class MainActivity : AppCompatActivity() {


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

        val model = QosModel()
        val loginButton = findViewById<Button>(R.id.next_button)
        val cancelButton = findViewById<Button>(R.id.cancel_button)

        val username = findViewById<TextInputEditText>(R.id.username_edit_text)
        val password = findViewById<TextInputEditText>(R.id.password_edit_text)

        loginButton.setOnClickListener {
            model.login(username.text.toString(),password.text.toString())
        }

        cancelButton.setOnClickListener{
            username.setText("")
            password.setText("")
        }

    }
}


