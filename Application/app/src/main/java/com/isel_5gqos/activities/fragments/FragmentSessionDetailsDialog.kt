package com.isel_5gqos.activities.fragments

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.isel_5gqos.R
import com.isel_5gqos.common.DEFAULT_SESSION_ID
import com.isel_5gqos.common.ThroughputIndex
import com.isel_5gqos.models.TestViewModel
import kotlinx.android.synthetic.main.fragment_main_session.*
import kotlinx.android.synthetic.main.fragment_session_details_dialog.*
import kotlinx.android.synthetic.main.fragment_session_details_dialog.session_details_throughput_chart
import java.lang.Long


class FragmentSessionDetailsDialog : DialogFragment() {
    public lateinit var dialog: AlertDialog
    private lateinit var dialogView: View
    private var nrOfClicks = 1

    private val testModel by lazy {
        ViewModelProvider(this)[TestViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView =
            LayoutInflater.from(context).inflate(R.layout.fragment_session_details_dialog, null)

        dialog = AlertDialog.Builder(requireContext(), R.style.FullScreenDialog)
            .setView(dialogView)
            .create()


        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = dialogView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btn_back.setOnClickListener { dismiss() }
        btn_delete.setOnClickListener {
            val dialog = AlertDialog
                .Builder(requireContext())
                .create()

            val inflater = LayoutInflater.from(requireContext())
            val inflatedView = inflater.inflate(resources.getLayout(R.layout.delete_session_alert_dialog), null)
            val confirmButton = inflatedView.findViewById<Button>(R.id.session_delete_confirm_button)
            confirmButton.setOnClickListener { dialog.dismiss() }
            val deleteButton = inflatedView.findViewById<Button>(R.id.session_delete_cancel_button)
            deleteButton.setOnClickListener { dialog.dismiss() }


            dialog.setView(inflatedView)
            dialog.setCanceledOnTouchOutside(false)
            dialog.window!!.setBackgroundDrawableResource(R.drawable.white_background_gradient_blue_500_purple_500_red_border_round_20)
            dialog.show()


        }
    }

    //<editor-fold name="AUX FUNCTIONS">

    //</editor-fold>
}