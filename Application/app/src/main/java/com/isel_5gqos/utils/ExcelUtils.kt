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


class ExcelUtils {
    companion object {
        fun <T> exportToExcel(
            context: Context,
            filename: String,
            sheetsMap: Map<String,Triple<List<T>,(Sheet) -> Unit,(Row,T) -> Unit>>
        ) {
            val workBook = HSSFWorkbook()
            var sheet: Sheet

            sheetsMap.forEach { (sheetName, makers) ->
                sheet = workBook.createSheet(sheetName)
                val (list,makeHeaderRow,makeRow) = makers

                makeHeaderRow(sheet)

                list.forEachIndexed { index, radioParametersDto ->
                    val row = sheet.createRow(index + 1)
                    makeRow(row, radioParametersDto)
                }
            }


            // Setting Value and Style to the cell

            val file =
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "${filename}_${System.currentTimeMillis()}.xls")
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
            headerRow.createCell(0).setCellValue("regId") //TODO sair
            headerRow.createCell(1).setCellValue("no")
            headerRow.createCell(2).setCellValue("tech")
            headerRow.createCell(3).setCellValue("arfcn")
            headerRow.createCell(4).setCellValue("rssi")
            headerRow.createCell(5).setCellValue("rsrp")
            headerRow.createCell(6).setCellValue("cId")
            headerRow.createCell(7).setCellValue("psc")
            headerRow.createCell(8).setCellValue("pci")
            headerRow.createCell(9).setCellValue("rssnr")
            headerRow.createCell(10).setCellValue("rsrq")
            headerRow.createCell(11).setCellValue("netDataType")
            headerRow.createCell(12).setCellValue("isServingCell") //TODO sair
            headerRow.createCell(13).setCellValue("numberOfCellsWithTheSameTechAsServing") //TODO sair
            headerRow.createCell(14).setCellValue("latitude")
            headerRow.createCell(15).setCellValue("longitude")
            headerRow.createCell(16).setCellValue("sessionId") //TODO sair
            headerRow.createCell(17).setCellValue("timestamp")
        }

        fun makeRadioParametersRow(row: Row, radioParameters: RadioParameters ) {
            row.createCell(0).setCellValue(radioParameters.regId) //TODO sair
            row.createCell(1).setCellValue(radioParameters.no.toDouble())
            row.createCell(2).setCellValue(radioParameters.tech)
            row.createCell(3).setCellValue(radioParameters.arfcn.toDouble())
            row.createCell(4).setCellValue(radioParameters.rssi.toDouble())
            row.createCell(5).setCellValue(radioParameters.rsrp.toDouble())
            row.createCell(6).setCellValue(radioParameters.cId.toDouble())
            row.createCell(7).setCellValue(radioParameters.psc.toDouble())
            row.createCell(8).setCellValue(radioParameters.pci.toDouble())
            row.createCell(9).setCellValue(radioParameters.rssnr.toDouble())
            row.createCell(10).setCellValue(radioParameters.rsrq.toDouble())
            row.createCell(11).setCellValue(radioParameters.netDataType)
            row.createCell(12).setCellValue(radioParameters.isServingCell) //TODO sair
            row.createCell(13).setCellValue(radioParameters.numbOfCellsWithSameTechAsServing.toDouble()) //TODO sair
            row.createCell(14).setCellValue(radioParameters.latitude)
            row.createCell(15).setCellValue(radioParameters.longitude)
            row.createCell(16).setCellValue(radioParameters.sessionId) //TODO sair
            row.createCell(17).setCellValue(radioParameters.timestamp.toString())
        }

        fun makeThroughputHeaderRow(sheet: Sheet){
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("regId") //TODO sair
            headerRow.createCell(1).setCellValue("txResult")
            headerRow.createCell(2).setCellValue("rxResult")
            headerRow.createCell(3).setCellValue("latitude")
            headerRow.createCell(4).setCellValue("longitude")
            headerRow.createCell(5).setCellValue("sessionId") //TODO sair
        }

        fun makeThroughputRow(row: Row,throughPut: ThroughPut) {
            row.createCell(0).setCellValue(throughPut.regId) //TODO sair
            row.createCell(1).setCellValue(throughPut.txResult.toString())
            row.createCell(2).setCellValue(throughPut.rxResult.toString())
            row.createCell(3).setCellValue(throughPut.latitude)
            row.createCell(4).setCellValue(throughPut.longitude)
            row.createCell(5).setCellValue(throughPut.sessionId) //TODO sair
        }
    }
}