package com.isel_5gqos.utils.anyChartUtils

import com.anychart.chart.common.dataentry.TreeDataEntry

fun customTreeDataEntry(id:String?, parent: String?, value:String?): CustomTreeDataEntry {
    val customTreeDataEntry = CustomTreeDataEntry(id,parent)
    customTreeDataEntry.setValue("product",value)
    return customTreeDataEntry
}

class CustomTreeDataEntry(id:String?, parent:String?): TreeDataEntry(id,parent)