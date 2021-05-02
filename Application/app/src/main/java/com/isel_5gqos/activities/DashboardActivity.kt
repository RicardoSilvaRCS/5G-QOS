package com.isel_5gqos.activities

import android.app.ActionBar
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.enums.SelectionMode
import com.anychart.enums.TreeFillingMethod
import com.isel_5gqos.QosApp
import com.isel_5gqos.R
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.dtos.WrapperDto
import com.isel_5gqos.models.InternetViewModel
import com.isel_5gqos.models.TestViewModel
import com.isel_5gqos.utils.anyChartUtils.customTreeDataEntry
import com.isel_5gqos.workers.scheduleRadioParametersBackgroundWork
import com.isel_5gqos.workers.scheduleThroughPutBackgroundWork


class DashboardActivity : AppCompatActivity() {
    private val model by lazy {
        ViewModelProviders.of(this)[InternetViewModel::class.java]
    }

    private val testModel by lazy {
        ViewModelProviders.of(this)[TestViewModel::class.java]
    }

    var nrOfTests = 0
    val workers = mutableListOf<WorkRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard2)

        val username = intent.getStringExtra(USER)?.toString() ?: ""

        /**Create new Session*/

        val createButton = findViewById<Button>(R.id.createSession)
        val deleteButton = findViewById<Button>(R.id.endSession)

        createButton.setOnClickListener {
            finishWorkers()
            testModel.startSession(username)

            scheduleThroughPutBackgroundWork(sessionId = testModel.value.id )
            setupRadioParametersBackgroundWorker(sessionId = testModel.value.id, saveToDb = true)

            Log.v(TAG, "Started session")
        }

        deleteButton.setOnClickListener {
            testModel.endSession()

            Log.v(TAG, "Finished session")

            finishWorkers()
            setupRadioParametersBackgroundWorker(DEFAULT_SESSION_ID, false)
        }

        val tries = findViewById<TextView>(R.id.tries)

        findViewById<Button>(R.id.ping).setOnClickListener {
            model.getResults("google.com", 25)
            tries.text = (++nrOfTests).toString()
        }

        val linearLayout = findViewById<LinearLayout>(R.id.results)

        linearLayout.orientation = LinearLayout.HORIZONTAL

        /**Linear layout to make ping results view*/
        model.observe(this) {
            if (it.pingInfos.size > 0) {
                val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT)
                layoutParams.setMargins(5, 0, 0, 0)
                val layoutParamsV = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT)
                layoutParamsV.setMargins(0, 5, 0, 0)

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

        /**Start real Time Session*/
        startDefaultSession(username)


        testModel.observe(this) {

            if(!it.radioParameters.radioParametersDtos.isEmpty()){
                updateGraph(it.radioParameters)
            }

        }

        val person = findViewById<TextView>(R.id.person)
        person.text = username
    }

    private fun startDefaultSession(username: String) {
        asyncTask({testModel.startDefaultSession(username)}){
           setupRadioParametersBackgroundWorker(DEFAULT_SESSION_ID, false)
        }
    }

    private fun setupRadioParametersBackgroundWorker(sessionId: String, saveToDb: Boolean = false) {
        val request = scheduleRadioParametersBackgroundWork(sessionId, saveToDb)
        workers.add(request)
        WorkManager.getInstance(QosApp.msWebApi.ctx)
            // requestId is the WorkRequest id
            .getWorkInfoByIdLiveData(request.id)
            .observe(this, Observer { workInfo: WorkInfo? ->
                if (workInfo != null) {
                    val progress = workInfo.progress

                    if (progress.getBoolean(PROGRESS, false)){
                        testModel.updateRadioParameters(sessionId, this)
                    }

                }
            })
    }



    private fun updateGraph(wrapperDto: WrapperDto) {
        val tableView = findViewById<AnyChartView>(R.id.graph)
        val table = AnyChart.treeMap()
        val neighbors = "Neighbors"
        val no = "No"
        val tech = "Tech"
        val arfcn = "ARFCN"
        val rssi = "RSSI/RSRP"
        val cid = "CID/PSC/PCI"
        val data = mutableListOf<DataEntry>()
        data.add(customTreeDataEntry(neighbors, null, neighbors))
        data.add(customTreeDataEntry(no, neighbors, no))
        data.add(customTreeDataEntry(tech, neighbors, tech))
        data.add(customTreeDataEntry(arfcn, neighbors, arfcn))
        data.add(customTreeDataEntry(rssi, neighbors, rssi))
        data.add(customTreeDataEntry(cid, neighbors, cid))

        wrapperDto.radioParametersDtos.forEachIndexed { index, value->
            data.add(customTreeDataEntry("$value$index", no, (index + 1).toString()))
            data.add(customTreeDataEntry("$value$index", tech, value.tech.toString()))
            data.add(customTreeDataEntry("$value$index", arfcn, value.arfcn.toString()))
            data.add(customTreeDataEntry("$value$index", rssi, value.rssi.toString()))
            data.add(customTreeDataEntry("$value$index", cid, value.getCellId()))
        }

        table.data(data, TreeFillingMethod.AS_TABLE)

        table.labels().useHtml(true)
        table.labels().fontColor("#212121")
        table.labels().fontSize(12.0)
        table.labels().format(
            """function() {
                          return this.getData('product');
                      }"""
        )

        table.padding(10.0, 10.0, 10.0, 20.0)
        table.maxDepth(2.0)
        table.selectionMode(SelectionMode.NONE)
        table.headers().format(
            "function() {\n" +
                    "    return this.getData('product');\n" +
                    "  }"
        );

        tableView.setChart(table)
    }

    override fun onDestroy() {
        super.onDestroy()
        finishWorkers()
    }

    private fun finishWorkers (){
        workers.forEach {
            WorkManager.getInstance(QosApp.msWebApi.ctx).cancelWorkById(it.id)
        }
    }
}