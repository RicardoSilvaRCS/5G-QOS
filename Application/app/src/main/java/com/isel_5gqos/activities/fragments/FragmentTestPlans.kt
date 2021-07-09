package com.isel_5gqos.activities.fragments

import android.os.Bundle
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.isel_5gqos.R
import com.isel_5gqos.activities.adapters.TestPlanAdapter
import com.isel_5gqos.factories.QosFactory
import com.isel_5gqos.models.QosViewModel
import kotlinx.android.synthetic.main.fragment_test_plans.*

class FragmentTestPlans : Fragment() {

    private lateinit var qosFactory: QosFactory
    private val qosModel by lazy {
        ViewModelProvider(this, qosFactory)[QosViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_test_plans, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        qosFactory = QosFactory(savedInstanceState)

        qosModel.getAllTestPlans().observe(requireActivity()) {
            val metrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(metrics)
            test_plan_recycler_view.adapter =
                TestPlanAdapter(
                    testPlans = it,
                    parentFragmentManager = parentFragmentManager,
                    context = requireContext(),
                    displayMetrics = metrics
                )
            test_plan_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        }

    }
}