package com.isel_5gqos.activities.adapters

import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.isel_5gqos.R
import com.isel_5gqos.common.db.entities.TestPlanResult

class TestAdapter(
    private val testPlanResults: List<TestPlanResult>,
    private val displayMetrics: DisplayMetrics
): RecyclerView.Adapter<TestViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
         val layout = LayoutInflater
             .from(parent.context)
             .inflate(R.layout.test_plan_details_recycler_view_adapter,null) as LinearLayout

        return TestViewHolder(layout,displayMetrics)
    }

    override fun onBindViewHolder(holder: TestViewHolder, position: Int) {
        holder.bindTo(testPlanResults[position])
    }

    override fun getItemCount(): Int = testPlanResults.size
}


class TestViewHolder(
    private val view:LinearLayout,
    private val displayMetrics: DisplayMetrics
):RecyclerView.ViewHolder(view){

    private val testIdTextView = view.findViewById<TextView>(R.id.test_id)
    private val testTypeDateTextView = view.findViewById<TextView>(R.id.test_type)
    private val isTestReportedDurationTextView = view.findViewById<TextView>(R.id.test_reported)

    fun bindTo(testResult:TestPlanResult){
        view.layoutParams = LinearLayout.LayoutParams((displayMetrics.widthPixels * 0.65).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)
        testIdTextView.text = "Test Id ${testResult.testId}"
        testTypeDateTextView.text = "Type: ${testResult.type}"
        isTestReportedDurationTextView.text = if(testResult.isReported) "Status: Reported" else "Status: Not Reported"
    }
}