package com.isel_5gqos.activities

import android.app.ActionBar
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.enums.SelectionMode
import com.anychart.enums.TreeFillingMethod
import com.isel_5gqos.Jobs.scheduleRadioParametersJob
import com.isel_5gqos.Jobs.scheduleThroughPutJob
import com.isel_5gqos.QosApp
import com.isel_5gqos.R
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.dtos.RadioParametersDto
import com.isel_5gqos.dtos.WrapperDto
import com.isel_5gqos.models.InternetViewModel
import com.isel_5gqos.models.TestViewModel
import com.isel_5gqos.utils.any_chart_utils.customTreeDataEntry


class DashboardActivity : AppCompatActivity() {

    private val model by lazy {
        ViewModelProviders.of(this)[InternetViewModel::class.java]
    }

    private val testModel by lazy {
        ViewModelProviders.of(this)[TestViewModel::class.java]
    }

    private val jobs = mutableListOf<JobInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard2)

        val username = intent.getStringExtra(USER)?.toString() ?: ""

        /**Create new Session*/

        val createButton = findViewById<Button>(R.id.createSession)
        val deleteButton = findViewById<Button>(R.id.endSession)

        createButton.setOnClickListener {
            cancelAllJobs()

            testModel.startSession(username)

            //jobs.add(scheduleThroughPutJob(sessionId = testModel.value.id))
            jobs.add(scheduleRadioParametersJob(sessionId = testModel.value.id, true ))

            testModel.registerRadioParametersChanges(testModel.value.id).observe(this) {
                val radioParameters = RadioParametersDto.convertRadioParametersToDto(it)
                updateGraph(radioParameters)
            }

            Log.v(TAG, "Started session")
        }

        deleteButton.setOnClickListener {
            cancelAllJobs()

            jobs.add(scheduleRadioParametersJob(DEFAULT_SESSION_ID, false))
        }


        val tries = findViewById<TextView>(R.id.tries)

        /**Start real Time Session*/
        startDefaultSession(username)
        jobs.add(scheduleRadioParametersJob(DEFAULT_SESSION_ID, false))


        testModel.registerRadioParametersChanges(DEFAULT_SESSION_ID).observe(this) {
                val radioParametersDto = mutableListOf<RadioParametersDto>()
                asyncTask({ radioParametersDto.addAll(RadioParametersDto.convertRadioParametersToDto(it))}){
                    updateGraph(radioParametersDto)
                }
        }

        val person = findViewById<TextView>(R.id.person)
        person.text = username
    }

    private fun cancelAllJobs() {

        jobs.forEach{
            QosApp.msWebApi.ctx.getSystemService(JobScheduler::class.java).cancel(it.id)
        }

        jobs.clear()
    }

    private fun startDefaultSession(username: String) {
        asyncTask({testModel.startDefaultSession(username)}){
            scheduleRadioParametersJob(sessionId = DEFAULT_SESSION_ID, saveToDb = false )
        }
    }

    private fun updateGraph(radioParametersDto: List<RadioParametersDto> ) {
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

        radioParametersDto.forEachIndexed { index, value->
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

}