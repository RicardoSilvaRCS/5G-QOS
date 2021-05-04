package com.isel_5gqos.activities

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.TreeDataEntry
import com.anychart.core.ui.Title
import com.anychart.enums.*
import com.isel_5gqos.QosApp
import com.isel_5gqos.R
import com.isel_5gqos.common.*
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.dtos.RadioParametersDto
import com.isel_5gqos.jobs.scheduleRadioParametersJob
import com.isel_5gqos.models.InternetViewModel
import com.isel_5gqos.models.TestViewModel
import java.util.*
import kotlin.collections.ArrayList


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
            testModel.observe(this) {
                //jobs.add(scheduleThroughPutJob(sessionId = testModel.value.id))
                jobs.add(scheduleRadioParametersJob(sessionId = testModel.value.id, true ))

                testModel.registerRadioParametersChanges(testModel.value.id).observe(this) {
                    val radioParameters = RadioParametersDto.convertRadioParametersToDto(it)
                    updateGraph(radioParameters)
                }

                Log.v(TAG, "Started session")
            }

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
                radioParametersDto.addAll(RadioParametersDto.convertRadioParametersToDto(it))
                updateGraph(radioParametersDto)
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

    /*private fun updateGraph(radioParametersDto: List<RadioParametersDto> ) {
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
        )

        tableView.setChart(table)
    }*/
    private fun updateGraph(radioParametersDto: List<RadioParametersDto> ) {
        val anyChartView = findViewById<AnyChartView>(R.id.graph)
//        anyChartView.setProgressBar(findViewById(R.id.progress_bar))

        val treeMap = AnyChart.treeMap()

        val data: MutableList<DataEntry> = ArrayList()
        data.add(CustomTreeDataEntry("Products by Revenue", null, "Products by Revenue"))
        data.add(CustomTreeDataEntry("Fruits", "Products by Revenue", "Fruits", 692000))
        data.add(CustomTreeDataEntry("Vegetables", "Products by Revenue", "Vegetables", 597000))
        data.add(CustomTreeDataEntry("Dairy", "Products by Revenue", "Dairy", 1359000))
        data.add(CustomTreeDataEntry("Meat", "Products by Revenue", "Meat", 596000))
        data.add(CustomTreeDataEntry("Apples", "Fruits", "Apples", 138000))
        data.add(CustomTreeDataEntry("Oranges", "Fruits", "Oranges", 22000))
        data.add(CustomTreeDataEntry("Bananas", "Fruits", "Bananas", 88000))
        data.add(CustomTreeDataEntry("Melons", "Fruits", "Melons", 77000))
        data.add(CustomTreeDataEntry("Apricots", "Fruits", "Apricots", 48000))
        data.add(CustomTreeDataEntry("Plums", "Fruits", "Plums", 48000))
        data.add(CustomTreeDataEntry("Pineapples", "Fruits", "Pineapples", 41000))
        data.add(CustomTreeDataEntry("Cherries", "Fruits", "Cherries", 39000))
        data.add(CustomTreeDataEntry("Tangerines", "Fruits", "Tangerines", 32000))
        data.add(CustomTreeDataEntry("Potato", "Vegetables", "Potato", 189000))
        data.add(CustomTreeDataEntry("Eggplants", "Vegetables", "Eggplants", 94000))
        data.add(CustomTreeDataEntry("Tomatoes", "Vegetables", "Tomatoes", 63000))
        data.add(CustomTreeDataEntry("Cucumbers", "Vegetables", "Cucumbers", 43000))
        data.add(CustomTreeDataEntry("Cabbage", "Vegetables", "Cabbage", 30000))
        data.add(CustomTreeDataEntry("Carrot", "Vegetables", "Carrot", 29000))
        data.add(CustomTreeDataEntry("Squash", "Vegetables", "Squash", 26000))
        data.add(CustomTreeDataEntry("Capsicums", "Vegetables", "Capsicums", 23000))
        data.add(CustomTreeDataEntry("Milk", "Dairy", "Milk", 154000))
        data.add(CustomTreeDataEntry("Curd", "Dairy", "Curd", 142000))
        data.add(CustomTreeDataEntry("Cheese", "Dairy", "Cheese", 43000))
        data.add(CustomTreeDataEntry("Yogurt", "Dairy", "Yogurt", 38000))
        data.add(CustomTreeDataEntry("Kefir", "Dairy", "Kefir", 32000))
        data.add(CustomTreeDataEntry("Mutton", "Meat", "Mutton", 154000))
        data.add(CustomTreeDataEntry("Beef", "Meat", "Beef", 142000))
        data.add(CustomTreeDataEntry("Pork", "Meat", "Pork", 43000))
        data.add(CustomTreeDataEntry("Veal", "Meat", "Veal", 38000))

        treeMap.data(data, TreeFillingMethod.AS_TABLE)

        val title: Title = treeMap.title()
        title.enabled(true)
        title.useHtml(true)
        title.padding(0.0, 0.0, 20.0, 0.0)
        title.text(
            "Top ACME Products by Revenue<br/>' +\n" +
                    "      '<span style=\"color:#212121; font-size: 13px;\">(average sales during the year, in $)</span>"
        )

        treeMap.colorScale().ranges(
            arrayOf(
                "{ less: 25000 }",
                "{ from: 25000, to: 30000 }",
                "{ from: 30000, to: 40000 }",
                "{ from: 40000, to: 50000 }",
                "{ from: 50000, to: 100000 }",
                "{ greater: 100000 }"
            )
        )

        treeMap.colorScale().colors(
            arrayOf(
                "#ffee58", "#fbc02d", "#f57f17", "#c0ca33", "#689f38", "#2e7d32"
            )
        )

        treeMap.padding(10.0, 10.0, 10.0, 20.0)
        treeMap.maxDepth(2.0)
        treeMap.hovered().fill("#bdbdbd", 1.0)
        treeMap.selectionMode(SelectionMode.NONE)

        treeMap.legend().enabled(true)
        treeMap.legend()
            .padding(0.0, 0.0, 0.0, 20.0)
            .position(Orientation.RIGHT)
            .align(Align.TOP)
            .itemsLayout(LegendLayout.VERTICAL)

        treeMap.labels().useHtml(true)
        treeMap.labels().fontColor("#212121")
        treeMap.labels().fontSize(12.0)
        treeMap.labels().format(
            "function() {\n" +
                    "      return this.getData('product');\n" +
                    "    }"
        )

        treeMap.headers().format(
            ("function() {\n" +
                    "    return this.getData('product');\n" +
                    "  }")
        )

        treeMap.tooltip()
            .useHtml(true)
            .titleFormat("{%product}")
            .format(
                ("function() {\n" +
                        "      return '<span style=\"color: #bfbfbf\">Revenue: </span>$' +\n" +
                        "        anychart.format.number(this.value, {\n" +
                        "          groupsSeparator: ' '\n" +
                        "        });\n" +
                        "    }")
            )

        anyChartView.setChart(treeMap)
    }

    private class CustomTreeDataEntry : TreeDataEntry {
        internal constructor(id: String?, parent: String?, product: String?, value: Int?) : super(id, parent, value) {
            setValue("product", product)
        }

        internal constructor(id: String?, parent: String?, product: String?) : super(id, parent) {
            setValue("product", product)
        }
    }


}