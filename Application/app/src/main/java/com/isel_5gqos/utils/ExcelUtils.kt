package com.isel_5gqos.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import com.isel_5gqos.common.TAG
import com.isel_5gqos.common.db.entities.RadioParameters
import com.isel_5gqos.common.db.entities.ThroughPut
import com.isel_5gqos.utils.android_utils.AndroidUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


enum class ExcelSheetNamesEnum(val sheetName:String) {
    RADIO_PARAMETERS("RadioParameters"),
    THROUGHPUT("Throughput")
}

class ExcelUtils {
    companion object {
        fun <T> exportToExcel(
            context: Context,
            filename: String,
            sheetsMap: Map<String,Triple<List<T>,(Sheet) -> Unit,(Row,T) -> Unit>>
        ) {
            val workBook = HSSFWorkbook()
            var sheet: Sheet

            sheetsMap.forEach { (sheetName, triple) ->
                sheet = workBook.createSheet(sheetName)
                val (list,makeHeaderRow,makeRow) = triple

                makeHeaderRow(sheet)

                list.forEachIndexed { index, radioParametersDto ->
                    val row = sheet.createRow(index + 1)
                    makeRow(row, radioParametersDto)
                }
            }


            val folder = File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}${File.separator}QoS5G")

            if(!folder.exists())
                folder.mkdir()

            val file =
                File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}${File.separator}QoS5G${File.separator}${DateUtils.getDateByFormat("dd_MMM_yyyy__HH_mm")}_${filename}.xls")
            var fileOutputStream: FileOutputStream? = null

            try {
                fileOutputStream = FileOutputStream(file)
                workBook.write(fileOutputStream)
                Log.e(TAG, "Writing file$file")
                AndroidUtils.makeRawToast(context, "Done")
            } catch (e: IOException) {
                Log.e(TAG, "Error writing Exception: ", e)
                AndroidUtils.makeRawToast(context, "Error")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save file due to Exception: ", e)
                AndroidUtils.makeRawToast(context, "Error")
            } finally {
                try {
                    fileOutputStream?.close()
                } catch (ex: Exception) {
                    AndroidUtils.makeRawToast(context, "Error closing")
                    ex.printStackTrace()
                }
            }
        }

        fun makeRadioParametersHeaderRow(sheet: Sheet) {
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("no")
            headerRow.createCell(1).setCellValue("tech")
            headerRow.createCell(2).setCellValue("arfcn")
            headerRow.createCell(3).setCellValue("rssi")
            headerRow.createCell(4).setCellValue("rsrp")
            headerRow.createCell(5).setCellValue("cId")
            headerRow.createCell(6).setCellValue("psc")
            headerRow.createCell(7).setCellValue("pci")
            headerRow.createCell(8).setCellValue("rssnr")
            headerRow.createCell(9).setCellValue("rsrq")
            headerRow.createCell(10).setCellValue("netDataType")
            headerRow.createCell(11).setCellValue("latitude")
            headerRow.createCell(12).setCellValue("longitude")
            headerRow.createCell(13).setCellValue("timestamp")
        }

        fun makeRadioParametersRow(row: Row, radioParameters: RadioParameters ) {
            row.createCell(0).setCellValue(radioParameters.no.toDouble())
            row.createCell(1).setCellValue(radioParameters.tech)
            row.createCell(2).setCellValue(radioParameters.arfcn.toDouble())
            row.createCell(3).setCellValue(radioParameters.rssi.toDouble())
            row.createCell(4).setCellValue(radioParameters.rsrp.toDouble())
            row.createCell(5).setCellValue(radioParameters.cId.toDouble())
            row.createCell(6).setCellValue(radioParameters.psc.toDouble())
            row.createCell(7).setCellValue(radioParameters.pci.toDouble())
            row.createCell(8).setCellValue(radioParameters.rssnr.toDouble())
            row.createCell(9).setCellValue(radioParameters.rsrq.toDouble())
            row.createCell(10).setCellValue(radioParameters.netDataType)
            row.createCell(11).setCellValue(radioParameters.latitude)
            row.createCell(12).setCellValue(radioParameters.longitude)
            row.createCell(13).setCellValue(radioParameters.timestamp.toString())
        }

        fun makeThroughputHeaderRow(sheet: Sheet){
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("txResult")
            headerRow.createCell(1).setCellValue("rxResult")
            headerRow.createCell(2).setCellValue("latitude")
            headerRow.createCell(3).setCellValue("longitude")
        }

        fun makeThroughputRow(row: Row,throughPut: ThroughPut) {
            row.createCell(0).setCellValue(throughPut.txResult.toString())
            row.createCell(1).setCellValue(throughPut.rxResult.toString())
            row.createCell(2).setCellValue(throughPut.latitude)
            row.createCell(3).setCellValue(throughPut.longitude)
        }
    }
}