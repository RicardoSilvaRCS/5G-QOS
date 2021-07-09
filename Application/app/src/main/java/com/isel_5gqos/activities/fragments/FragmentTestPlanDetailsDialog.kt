package com.isel_5gqos.activities.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.isel_5gqos.R
import com.isel_5gqos.activities.adapters.TestAdapter
import com.isel_5gqos.common.db.entities.TestPlan
import com.isel_5gqos.factories.QosFactory
import com.isel_5gqos.models.QosViewModel
import com.isel_5gqos.models.observeOnce
import kotlinx.android.synthetic.main.fragment_test_plan_details_dialog.*

class FragmentTestPlanDetailsDialog(private val testPlan: TestPlan,private val metrics: DisplayMetrics):DialogFragment() {
    lateinit var dialog: AlertDialog
    private lateinit var dialogView: View
    private lateinit var qosFactory: QosFactory
    private val qosModel by lazy {
        ViewModelProvider(this,qosFactory)[QosViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView =
            LayoutInflater.from(context).inflate(R.layout.fragment_test_plan_details_dialog, null)

        dialog = AlertDialog.Builder(requireContext(), R.style.FullScreenDialog)
            .setView(dialogView)
            .create()


        dialogView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (metrics.heightPixels * 0.75).toInt())

        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = dialogView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        qosFactory = QosFactory(savedInstanceState)
        test_plan_details_title.text = testPlan.name
        test_plan_details_date.text = testPlan.startDate

        qosModel.getTestsByTestPlanId(testPlan.id).observeOnce(requireActivity()){
            val metrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(metrics)

            test_plan_details_recycler_view.adapter = TestAdapter(it,metrics)
            test_plan_details_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        }

    }
}