package com.isel_5gqos.utils.mp_android_chart_utils

import android.graphics.Color
import android.graphics.drawable.Drawable
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.badge.BadgeDrawable

class ChartUtils {

    companion object{

         fun initThroughputDataLine(): LineData {

            val rxValues = java.util.ArrayList<Entry>()
            val txValues = java.util.ArrayList<Entry>()

            rxValues.add(Entry(0f, 0f))
            txValues.add(Entry(0f, 0f))

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

         fun initServingCellData(): LineData {

            val rssiValue = java.util.ArrayList<Entry>()
            rssiValue.add(Entry(0f, 0f))
            val rssi = LineDataSet(rssiValue, "RSSI")
            rssi.lineWidth = 3f
            rssi.circleRadius = 1f
            rssi.highLightColor = Color.rgb(163, 145, 3)
            rssi.setDrawValues(false)


            val rsrpValue = java.util.ArrayList<Entry>()
            rsrpValue.add(Entry(0f, 0f))
            val rsrp = LineDataSet(rsrpValue, "RSRP")
            rsrp.lineWidth = 3f
            rsrp.circleRadius = 1f
            rsrp.highLightColor = Color.rgb(0, 255, 0)
            rsrp.color = ColorTemplate.VORDIPLOM_COLORS[1]
            rsrp.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[1])
            rsrp.setDrawValues(false)

            val rsqrValue = java.util.ArrayList<Entry>()
            rsqrValue.add(Entry(0f, 0f))
            val rsqr = LineDataSet(rsqrValue, "RSQR")
            rsqr.lineWidth = 3f
            rsqr.circleRadius = 1f
            rsqr.highLightColor = Color.rgb(0, 0, 255)
            rsqr.color = ColorTemplate.VORDIPLOM_COLORS[2]
            rsqr.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[2])
            rsqr.setDrawValues(false)


            val rssnrValue = java.util.ArrayList<Entry>()
            rssnrValue.add(Entry(0f, 0f))
            val rssnr = LineDataSet(rssnrValue, "RSSNR")
            rssnr.lineWidth = 3f
            rssnr.circleRadius = 1f
            rssnr.highLightColor = Color.rgb(255, 0, 0)
            rssnr.color = ColorTemplate.VORDIPLOM_COLORS[0]
            rssnr.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[0])
            rssnr.setDrawValues(false)


            val sets = java.util.ArrayList<ILineDataSet>()
            sets.add(rssi)
            sets.add(rsrp)
            sets.add(rsqr)
            sets.add(rssnr)

            return LineData(sets)
        }

         fun initStrongestNeighborData(): LineData {

            val rssiGsmData = java.util.ArrayList<Entry>()
            rssiGsmData.add(Entry(0f, 0f))
            val rssiGsm = LineDataSet(rssiGsmData, "RSSI GSM")
            rssiGsm.lineWidth = 3f
            rssiGsm.circleRadius = 1f
            rssiGsm.highLightColor = Color.rgb(163, 145, 3)
            rssiGsm.setDrawValues(false)


            val rssiWcdmaData = java.util.ArrayList<Entry>()
            rssiWcdmaData.add(Entry(0f, 0f))
            val rssiWcdma = LineDataSet(rssiWcdmaData, "RSSI WCDMA")
            rssiWcdma.lineWidth = 3f
            rssiWcdma.circleRadius = 1f
            rssiWcdma.highLightColor = Color.rgb(0, 255, 0)
            rssiWcdma.color = ColorTemplate.VORDIPLOM_COLORS[1]
            rssiWcdma.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[1])
            rssiWcdma.setDrawValues(false)

            val rsrpLteData = java.util.ArrayList<Entry>()
            rsrpLteData.add(Entry(0f, 0f))
            val rsrpLte = LineDataSet(rsrpLteData, "RSRP LTE")
            rsrpLte.lineWidth = 3f
            rsrpLte.circleRadius = 1f
            rsrpLte.highLightColor = Color.rgb(0, 0, 255)
            rsrpLte.color = ColorTemplate.VORDIPLOM_COLORS[2]
            rsrpLte.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[2])
            rsrpLte.setDrawValues(false)


            val sets = java.util.ArrayList<ILineDataSet>()
            sets.add(rssiGsm)
            sets.add(rssiWcdma)
            sets.add(rsrpLte)

            return LineData(sets)
        }

         fun initNumberOfCells(): LineData {

            val numberOfCells = java.util.ArrayList<Entry>()

            numberOfCells.add(Entry(0f, 0f))

            val servingCellTechNumberData = java.util.ArrayList<Entry>()

            servingCellTechNumberData.add(Entry(0f, 0f))
            val servingCellTechNumber = LineDataSet(servingCellTechNumberData, "Number(cells with same tech as serving)")
            servingCellTechNumber.lineWidth = 3f
            servingCellTechNumber.circleRadius = 1f
            servingCellTechNumber.highLightColor = Color.rgb(255, 0, 0)
            servingCellTechNumber.color = ColorTemplate.VORDIPLOM_COLORS[0]
            servingCellTechNumber.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[0])
            servingCellTechNumber.setDrawValues(false)


            val sets = java.util.ArrayList<ILineDataSet>()
            sets.add(servingCellTechNumber)

            return LineData(sets)
        }

         fun initLineChart(lineChart: LineChart, lineInitData: LineData, isNegative: Boolean = false, granularity: Float = 1f, drawable: Drawable) {

            lineChart.background = drawable
            // disable description text
            lineChart.description.isEnabled = false

            // enable scaling and dragging
            lineChart.isDragEnabled = true
            lineChart.setScaleEnabled(true)
            lineChart.setDrawGridBackground(false)

            // if disabled, scaling can be done on x- and y-axis separately
            lineChart.setPinchZoom(true)

            val xAxis = lineChart.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.axisLineWidth = 1.5f
            xAxis.setDrawGridLines(false)
            xAxis.setAvoidFirstLastClipping(true)
            xAxis.isEnabled = true
            xAxis.granularity = granularity

            val yAxis = lineChart.axisLeft
            yAxis.axisLineWidth = 1.5f
            yAxis.setDrawGridLines(true)
            yAxis.axisMaximum = if (isNegative) 0f else 10f
            yAxis.axisMinimum = if (isNegative) -10f else 0f

            lineChart.axisRight.isEnabled = false

            lineChart.data = lineInitData
        }

    }
}