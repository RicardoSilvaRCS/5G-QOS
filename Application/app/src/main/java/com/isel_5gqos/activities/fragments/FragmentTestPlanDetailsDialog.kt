package com.isel_5gqos.activities.fragments

import android.app.Dialog
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.isel_5gqos.R
import com.isel_5gqos.activities.adapters.TestAdapter
import com.isel_5gqos.common.db.entities.TestPlan
import com.isel_5gqos.common.utils.android_utils.AndroidUtils
import com.isel_5gqos.factories.QosFactory
import com.isel_5gqos.models.QosViewModel
import com.isel_5gqos.models.observeOnce
import kotlinx.android.synthetic.main.fragment_test_plan_details_dialog.*

class FragmentTestPlanDetailsDialog(private val testPlan: TestPlan, private val metrics: DisplayMetrics) : DialogFragment() {
    lateinit var dialog: AlertDialog
    private lateinit var dialogView: View
    private lateinit var qosFactory: QosFactory
    private val qosModel by lazy {
        ViewModelProvider(this, qosFactory)[QosViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView =
            LayoutInflater.from(context).inflate(R.layout.fragment_test_plan_details_dialog, null)

        dialogView.layoutParams = RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, metrics.heightPixels)

        dialog = AlertDialog.Builder(requireContext(), R.style.FullScreenDialog)
            .setView(dialogView)
            .setNegativeButton(R.string.back) { _, _ -> dismiss() }
            .create()

        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = dialogView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        qosFactory = QosFactory(savedInstanceState)
        test_plan_details_title.text = testPlan.name
        test_plan_details_date.text = testPlan.startDate

        qosModel.getTestsByTestPlanId(testPlan.id).observeOnce(requireActivity()) {
            val metrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(metrics)

            test_plan_details_recycler_view.adapter = TestAdapter(it, metrics)
            test_plan_details_recycler_view.layoutManager = LinearLayoutManager(requireContext())
            test_plan_details_recycler_view.addItemDecoration(
                object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                        with(outRect) {
                            if (parent.getChildAdapterPosition(view) == 0) {
                                top = 8
                            }
                            left = 8
                            right = 8
                            bottom = 16
                        }
                    }
                }
            )
        }

        btn_delete_test_plan_details_dialog_fragment.setOnClickListener {
            val loadingDialog = AndroidUtils.makeLoadingDialog(requireContext(), "Deleting...")
            loadingDialog.show()
            qosModel.deleteTestPlanById(testPlan.id) {
                loadingDialog.dismiss()
                dialog.dismiss()
            }
        }
    }
}