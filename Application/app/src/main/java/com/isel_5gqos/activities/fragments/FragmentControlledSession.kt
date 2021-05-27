package com.isel_5gqos.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.isel_5gqos.R
import com.isel_5gqos.factories.QosFactory
import com.isel_5gqos.models.QosViewModel
import com.isel_5gqos.models.TestViewModel
import kotlinx.android.synthetic.main.fragment_controlled_session.*


class FragmentControlledSession : Fragment() {

    private val testModel by lazy {
        ViewModelProvider(this).get(TestViewModel::class.java)
    }

    private lateinit var qosFactory: QosFactory
    private val qosViewModel by lazy {
        ViewModelProvider(this, qosFactory).get(QosViewModel::class.java)
    }
    //<editor-fold name="EVENTS">

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_controlled_session, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        qosFactory = QosFactory(savedInstanceState)

        testModel.getLastSession().observe(requireActivity()) {
            if (!checkIfLayoutIsAvailable() || it.isEmpty()) return@observe
            val controlledSession = it.firstOrNull()?:return@observe
            if (controlledSession.endDate != 0L) return@observe
            create_session.isGone = true
            end_session.visibility = View.VISIBLE
        }

        create_session.setOnClickListener { createSessionView ->
            if(!checkIfLayoutIsAvailable()) return@setOnClickListener
            qosViewModel.getLoggedUser().observe(requireActivity()) {
                testModel.startSession(it.username) {
                    createSessionView.isGone = true
                    end_session.visibility = View.VISIBLE
                }
            }
        }

        end_session.setOnClickListener { endSessionView ->
            if(!checkIfLayoutIsAvailable()) return@setOnClickListener
            testModel.endSessionById(requireActivity())
            endSessionView.isGone = true
            create_session.visibility = View.VISIBLE
        }

        btn_ping.setOnClickListener {
            val fragmentSessionDetailsDialog = FragmentSessionDetailsDialog()
            fragmentSessionDetailsDialog.show(parentFragmentManager,"taguinha")
        }
    }

    //</editor-fold>

    //<editor-fold name="AUX FUNCTIONS">

    private fun checkIfLayoutIsAvailable() = this.isResumed
    //</editor-fold>

}