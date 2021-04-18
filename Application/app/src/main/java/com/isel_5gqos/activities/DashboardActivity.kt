package com.isel_5gqos.activities

import android.app.ActionBar
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.isel_5gqos.models.InternetViewModel
import com.isel_5gqos.R

class DashboardActivity : AppCompatActivity() {
    private val model by lazy {
        ViewModelProviders.of(this)[InternetViewModel::class.java]
    }

    var nrOfTests = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard2)

        val userName = intent.getStringExtra(USER)
        val password = intent.getStringExtra(PASS)
        val tries = findViewById<TextView>(R.id.tries)
        findViewById<Button>(R.id.button).setOnClickListener {
            model.getResults("google.com", 25)
            tries.text = (++nrOfTests).toString()
        }

        val linearLayout = findViewById<LinearLayout>(R.id.results)

        linearLayout.orientation = LinearLayout.HORIZONTAL

        model.observe(this) {
            if(it.pingInfos.size > 0) {
                val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,ActionBar.LayoutParams.WRAP_CONTENT)
                layoutParams.setMargins(5,0,0,0)
                val layoutParamsV = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,ActionBar.LayoutParams.WRAP_CONTENT)
                layoutParamsV.setMargins(0,5,0,0)

                val verticalLayout = LinearLayout(this)
                verticalLayout.orientation = LinearLayout.VERTICAL

                val urlText = TextView(this)
                urlText.text = "URL:"
                val urlValue = TextView(this)
                urlValue.text = it.url
                urlValue.layoutParams = layoutParams
                val textHorizLayout = LinearLayout(this)
                textHorizLayout.layoutParams = layoutParamsV
                textHorizLayout.addView(urlText)
                textHorizLayout.addView(urlValue)

                val avgText = TextView(this)
                avgText.text = "AVG:"
                val avgValue = TextView(this)
                avgValue.text = it.avg.toString()
                avgValue.layoutParams = layoutParams
                val avgHorizLayout = LinearLayout(this)
                avgHorizLayout.layoutParams = layoutParamsV
                avgHorizLayout.addView(avgText)
                avgHorizLayout.addView(avgValue)

                val maxText = TextView(this)
                maxText.text = "MAX:"
                val maxValue = TextView(this)
                maxValue.text = it.maxMs.toString()
                maxValue.layoutParams = layoutParams
                val maxHorizLayout = LinearLayout(this)
                maxHorizLayout.layoutParams = layoutParamsV
                maxHorizLayout.addView(maxText)
                maxHorizLayout.addView(maxValue)

                val minText = TextView(this)
                minText.text = "MIN:"
                val minValue = TextView(this)
                minValue.text = it.minMs.toString()
                minValue.layoutParams = layoutParams
                val minHorizLayout = LinearLayout(this)
                minHorizLayout.layoutParams = layoutParamsV
                minHorizLayout.addView(minText)
                minHorizLayout.addView(minValue)

                val packetText = TextView(this)
                packetText.text = "PACKETS:"
                val packetValue = TextView(this)
                packetValue.text = it.pingInfos.size.toString()
                packetValue.layoutParams = layoutParams
                val packetHorizLayout = LinearLayout(this)
                packetHorizLayout.layoutParams = layoutParamsV
                packetHorizLayout.addView(packetText)
                packetHorizLayout.addView(packetValue)

                verticalLayout.addView(textHorizLayout)
                verticalLayout.addView(avgHorizLayout)
                verticalLayout.addView(maxHorizLayout)
                verticalLayout.addView(minHorizLayout)
                verticalLayout.addView(packetHorizLayout)

                linearLayout.addView(verticalLayout)
            }
        }

        val person = findViewById<TextView>(R.id.person)
        person.text = "${userName} ${password}"
    }
}