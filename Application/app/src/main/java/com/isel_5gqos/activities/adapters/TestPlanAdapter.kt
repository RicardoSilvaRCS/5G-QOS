package com.isel_5gqos.activities.adapters

import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.isel_5gqos.R
import com.isel_5gqos.activities.fragments.FragmentTestPlanDetailsDialog
import com.isel_5gqos.common.db.entities.TestPlan
import com.isel_5gqos.common.enums.TestPlanStatesEnum

class TestPlanAdapter(
    private val testPlans: List<TestPlan>,
    private val parentFragmentManager: FragmentManager,
    private val context: Context,
    private val displayMetrics: DisplayMetrics
) : RecyclerView.Adapter<TestPlanViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestPlanViewHolder {
        val testPlanLayout = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.test_plan_recycler_view_adapter, null) as LinearLayout

        return TestPlanViewHolder(testPlanLayout, parentFragmentManager, context, displayMetrics)
    }

    override fun onBindViewHolder(holder: TestPlanViewHolder, position: Int) {
        holder.bindTo(testPlans[position])
    }

    override fun getItemCount(): Int = testPlans.size
}

class TestPlanViewHolder(
    private val view: LinearLayout,
    private val parentFragmentManager: FragmentManager,
    private val context: Context,
    private val displayMetrics: DisplayMetrics
) : RecyclerView.ViewHolder(view) {

    private val testPlanNameTextView = view.findViewById<TextView>(R.id.test_plan_name)
    private val testPlanStartDateTextView = view.findViewById<TextView>(R.id.test_plan_start_date)
    private val testPlanTypeTextView = view.findViewById<TextView>(R.id.test_plan_type)
    fun bindTo(testPlan: TestPlan) {

        testPlanNameTextView.text = testPlan.name
        testPlanStartDateTextView.text = testPlan.startDate
        testPlanTypeTextView.text = testPlan.testPlanState
        view.layoutParams = LinearLayout.LayoutParams((displayMetrics.widthPixels * 0.9).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)

        if (testPlan.testPlanState == TestPlanStatesEnum.FINISHED.toString())
            view
                .findViewById<MaterialCardView>(R.id.test_plan_card)
                .setOnClickListener {
                    val dialog = FragmentTestPlanDetailsDialog(testPlan,displayMetrics)
                    dialog.show(parentFragmentManager, "DIALOG_TAG")
                    val dialogView = LayoutInflater
                        .from(context)
                        .inflate(R.layout.fragment_session_details_dialog, null)

                }
    }
}