package com.isel_5gqos.activities

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.isel_5gqos.QosApp
import com.isel_5gqos.R
import com.isel_5gqos.common.DEFAULT_SESSION_ID
import com.isel_5gqos.common.TAG
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.dtos.RadioParametersDto
import com.isel_5gqos.dtos.ThroughPutDto
import com.isel_5gqos.jobs.scheduleRadioParametersJob
import com.isel_5gqos.jobs.scheduleThroughPutJob
import com.isel_5gqos.models.InternetViewModel
import com.isel_5gqos.models.TestViewModel
import com.isel_5gqos.utils.mp_android_chart_utils.ChartItem
import com.isel_5gqos.utils.mp_android_chart_utils.LineChartItem


class DashboardActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener, OnChartValueSelectedListener {

    private val model by lazy {
        ViewModelProviders.of(this)[InternetViewModel::class.java]
    }

    private val testModel by lazy {
        ViewModelProviders.of(this)[TestViewModel::class.java]
    }

    private val jobs = mutableListOf<JobInfo>()

    /**ThroughPut Ui elements**/
    private lateinit var chart: LineChart
    private lateinit var seekBarX: SeekBar
    private lateinit var seekBarY: SeekBar
    private lateinit var tvX: TextView
    private lateinit var tvY: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard2)

//        chart = findViewById(R.id.chart1)
//        seekBarX = findViewById(R.id.seekBar1)
//        seekBarY = findViewById(R.id.seekBar2)
//        tvX = findViewById(R.id.tvXMax)
//        tvY = findViewById(R.id.tvYMax)

        val username = intent.getStringExtra(USER)?.toString() ?: ""

        /**Create new Session*/

        val createButton = findViewById<Button>(R.id.createSession)
        val deleteButton = findViewById<Button>(R.id.endSession)

        createButton.setOnClickListener {
            cancelAllJobs()

            testModel.startSession(username)
            testModel.observe(this) {
                //jobs.add(scheduleThroughPutJob(sessionId = testModel.value.id))
                jobs.add(scheduleRadioParametersJob(sessionId = testModel.value.id, true))

                testModel.registerRadioParametersChanges(testModel.value.id).observe(this) {
                    val radioParameters = RadioParametersDto.convertRadioParametersToDto(it)
                }

                Log.v(TAG, "Started session")
            }

        }

        deleteButton.setOnClickListener {
            cancelAllJobs()

//            jobs.add(scheduleRadioParametersJob(DEFAULT_SESSION_ID, false))
            jobs.add(scheduleThroughPutJob(DEFAULT_SESSION_ID))
        }


        /**Start real Time Session*/
        startDefaultSession(username)

        testModel.registerRadioParametersChanges(DEFAULT_SESSION_ID).observe(this) {
            val radioParametersDto = RadioParametersDto.convertRadioParametersToDto(it)
        }


        //initializethroughPutCharRepresentation()

        val lv = findViewById<ListView>(R.id.layoutListView)
        val list: ArrayList<ChartItem> = ArrayList()



        var throughPutChart = LineChartItem(generateDataLine(mutableListOf()),QosApp.msWebApi.ctx)
        list.add(throughPutChart)
        var cda = ChartDataAdapter(QosApp.msWebApi.ctx,list)

        lv.adapter = cda
        testModel.registerThroughPutChanges(DEFAULT_SESSION_ID).observe(this) {

            val throughPuts = ThroughPutDto.convertThroughPutToDto(it)

            throughPutChart.setmChartData(generateDataLine(throughPuts))
            cda.notifyDataSetInvalidated()
            lv.invalidate()
//            cda = ChartDataAdapter(QosApp.msWebApi.ctx,list)
//            lv.adapter = cda
//            lv.invalidate()

//            val list = throughPuts.mapIndexed { index, throughput ->
//                  Pair(index,throughput.rxResult.toInt())
////                seekBarX.setProgress(index,false)
////                seekBarY.setProgress(throughput.rxResult.toInt(),false)
//            }

//            setData(throughPuts)
//            chart.invalidate()
        }

    }

    private fun cancelAllJobs() {

        jobs.forEach {
            QosApp.msWebApi.ctx.getSystemService(JobScheduler::class.java).cancel(it.id)
        }

        jobs.clear()
    }

    private fun startDefaultSession(username: String) {
        asyncTask({ testModel.startDefaultSession(username) }) {
            jobs.add(scheduleRadioParametersJob(sessionId = DEFAULT_SESSION_ID, saveToDb = false))
            jobs.add(scheduleThroughPutJob(sessionId = DEFAULT_SESSION_ID))
        }
    }

    /**Initializing UI ThroughPut graphic**/
    private fun initializethroughPutCharRepresentation() {

        seekBarX.setOnSeekBarChangeListener(this)

        seekBarY.max = 180;
        seekBarY.setOnSeekBarChangeListener(this)

        chart.setBackgroundColor(Color.WHITE)
        // disable description text
        chart.description.isEnabled = false
        // enable touch gestures
        chart.setTouchEnabled(true)
        chart.setOnChartValueSelectedListener(this)
        chart.setDrawGridBackground(false)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)


        val xAxis = chart.xAxis
        xAxis.enableGridDashedLine(10f, 10f, 0f)


        val yAxis = chart.axisLeft
        chart.axisRight.isEnabled = false
        yAxis.enableGridDashedLine(10f, 10f, 0f)
        yAxis.axisMaximum = 200f
        yAxis.axisMinimum = -50f

        // // Create Limit Lines // //
        val llXAxis = LimitLine(9f, "Index 10")
        llXAxis.lineWidth = 4f
        llXAxis.enableDashedLine(10f, 10f, 0f)
        llXAxis.labelPosition = LimitLabelPosition.RIGHT_BOTTOM
        llXAxis.textSize = 10f
        //llXAxis.typeface = tfRegular

        val ll1 = LimitLine(150f, "Upper Limit")
        ll1.lineWidth = 4f
        ll1.enableDashedLine(10f, 10f, 0f)
        ll1.labelPosition = LimitLabelPosition.RIGHT_TOP
        ll1.textSize = 10f
        //ll1.typeface = tfRegular

        val ll2 = LimitLine(-30f, "Lower Limit")
        ll2.lineWidth = 4f
        ll2.enableDashedLine(10f, 10f, 0f)
        ll2.labelPosition = LimitLabelPosition.RIGHT_BOTTOM
        ll2.textSize = 10f
        //ll2.typeface = tfRegular

        yAxis.setDrawLimitLinesBehindData(true)
        xAxis.setDrawLimitLinesBehindData(true)


        yAxis.addLimitLine(ll1)
        yAxis.addLimitLine(ll2)

        chart.animateX(1500)

        val l = chart.legend
        l.form = LegendForm.LINE

    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        tvX.text = seekBarX.progress.toString()
        tvY.text = seekBarY.progress.toString()

//        setData(seekBarX.progress, seekBarY.progress);

        chart.invalidate();
    }

    private fun setData(throughPutDtos: List<ThroughPutDto>) {

        val rxValues = mutableListOf<Entry>()
        val txValues = mutableListOf<Entry>()

        throughPutDtos.forEachIndexed { index, throughPutDto ->
            rxValues.add(Entry(index.toFloat(),throughPutDto.rxResult.toFloat()))
            txValues.add(Entry(index.toFloat(),throughPutDto.txResult.toFloat()))
        }

        val set1: LineDataSet
        val set2: LineDataSet
        if (chart.data != null &&
            chart.data.dataSetCount > 0
        ) {
            set1 = chart.data.getDataSetByIndex(0) as LineDataSet
            set1.values = rxValues
            set1.notifyDataSetChanged()
//            set2 = chart.data.getDataSetByIndex(0) as LineDataSet
//            set2.values = rxValues
//            set2.notifyDataSetChanged()
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
            // create a dataset and give it a type
            set1 = LineDataSet(rxValues, "Rx")
            set1.setDrawIcons(false)

            // draw dashed line
            set1.enableDashedLine(10f, 5f, 0f)

            // black lines and points
            set1.color = Color.BLACK
            set1.setCircleColor(Color.BLACK)

            // line thickness and point size
            set1.lineWidth = 1f
            set1.circleRadius = 3f

            // draw points as solid circles
            set1.setDrawCircleHole(false)

            // customize legend entry
            set1.formLineWidth = 1f
            set1.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
            set1.formSize = 15f

            // text size of values
            set1.valueTextSize = 9f

            // draw selection line as dashed
            set1.enableDashedHighlightLine(10f, 5f, 0f)

            // set the filled area
            set1.setDrawFilled(true)
            set1.fillFormatter = IFillFormatter { dataSet, dataProvider -> chart.axisLeft.axisMinimum }


            set1.fillColor = Color.BLACK

            val dataSets: ArrayList<ILineDataSet> = ArrayList()
            dataSets.add(set1) // add the data sets

            // create a data object with the data sets
            val data = LineData(dataSets)

            // set data
            chart.data = data
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        Log.i(TAG, e.toString());
        Log.i(TAG, "low: " + chart.lowestVisibleX + ", high: " + chart.highestVisibleX);
        Log.i(TAG, "xMin: " + chart.xChartMin + ", xMax: " + chart.xChartMax + ", yMin: " + chart.yChartMin + ", yMax: " + chart.yChartMax);
    }

    override fun onNothingSelected() {}

    /**END**/


    /**Multiple chart layout**/

    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return Line data
     */
        private fun generateDataLine(throughPutDtos: List<ThroughPutDto>): LineData? {

            val rxValues = java.util.ArrayList<Entry>()
            val txValues = java.util.ArrayList<Entry>()


            if(throughPutDtos.isEmpty()) {
                rxValues.add(Entry(0f,0f))
                txValues.add(Entry(0f,0f))
            }
            else {

                throughPutDtos.forEachIndexed { index, throughPutDto ->

                    rxValues.add(Entry(index.toFloat(),throughPutDto.rxResult.toFloat()))
                    txValues.add(Entry(index.toFloat(),throughPutDto.txResult.toFloat()))

                }
            }

            val rx = LineDataSet(rxValues, "RX Kbit/s")
            rx.lineWidth = 2.5f
            rx.circleRadius = 1f
            rx.highLightColor = Color.rgb(244, 117, 117)
            rx.setDrawValues(false)

            val tx = LineDataSet(txValues, "TX Kbit/s")
            tx.lineWidth = 2.5f
            tx.circleRadius = 1f
            tx.highLightColor = Color.rgb(244, 117, 117)
            tx.color = ColorTemplate.VORDIPLOM_COLORS[0]
            tx.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[0])
            tx.setDrawValues(false)
        
        
            val sets = java.util.ArrayList<ILineDataSet>()
            sets.add(rx)
            sets.add(tx)

            return LineData(sets)
        }

        /** adapter that supports 3 different item types  */
        private class ChartDataAdapter internal constructor(context: Context?, objects: List<ChartItem?>?) :
            ArrayAdapter<ChartItem?>(context!!, 0, objects!!) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                return getItem(position)!!.getView(position, convertView, context)
            }

            override fun getItemViewType(position: Int): Int {
                // return the views type
                val ci = getItem(position)
                return ci?.itemType ?: 0
            }

            override fun getViewTypeCount(): Int {
                return 3 // we have 3 different item-types
            }
        }
    /**END**/


}