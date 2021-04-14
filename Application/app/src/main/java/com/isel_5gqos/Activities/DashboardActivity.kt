package com.isel_5gqos.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.isel_5gqos.R

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard2)

        val userName = intent.getStringExtra(USER)
        val password = intent.getStringExtra(PASS)


        val person = findViewById<TextView>(R.id.person)
        person.text = "${userName} ${password}"
    }
}