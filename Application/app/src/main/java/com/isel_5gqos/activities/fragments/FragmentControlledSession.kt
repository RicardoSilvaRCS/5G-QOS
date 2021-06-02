package com.isel_5gqos.activities.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.isel_5gqos.R
import com.isel_5gqos.activities.adapters.SessionDetailsAdapter
import com.isel_5gqos.common.DEFAULT_SESSION_ID
import com.isel_5gqos.common.USER
import com.isel_5gqos.common.db.asyncTask
import com.isel_5gqos.factories.QosFactory
import com.isel_5gqos.factories.TestFactory
import com.isel_5gqos.models.QosViewModel
import com.isel_5gqos.models.TestViewModel
import com.isel_5gqos.utils.android_utils.AndroidUtils
import com.isel_5gqos.utils.publisher_subscriber.MessageEvent
import com.isel_5gqos.utils.publisher_subscriber.SessionMessageEvent
import com.isel_5gqos.utils.publisher_subscriber.SessionMessageTypeEnum
import com.isel_5gqos.utils.publisher_subscriber.StringMessageEvent
import kotlinx.android.synthetic.main.fragment_controlled_session.*
import kotlinx.android.synthetic.main.fragment_main_session.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class FragmentControlledSession : Fragment() {

    private lateinit var testFactory: TestFactory
    private val testModel by lazy {
        ViewModelProvider(this,testFactory).get(TestViewModel::class.java)
    }
    private lateinit var loadingDialog :AlertDialog
    private lateinit var inflater:LayoutInflater
    private lateinit var qosFactory: QosFactory
    private val qosViewModel by lazy {
        ViewModelProvider(this, qosFactory).get(QosViewModel::class.java)
    }

    //<editor-fold name="EVENTS">

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_controlled_session, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        qosFactory = QosFactory(savedInstanceState)
        val username = requireActivity().intent.getStringExtra(USER) ?: ""

        loadingDialog = AlertDialog.Builder(requireContext()).create()
        inflater = LayoutInflater.from(requireContext())

        testFactory = TestFactory(savedInstanceState,username)
        testModel.getLastSession().observe(requireActivity()) {
            if (!checkIfLayoutIsAvailable() || it == null) return@observe
            if (it.endDate != 0L) return@observe
            create_session.isGone = true
            end_session.visibility = View.VISIBLE
        }

        create_session.setOnClickListener { createSessionView ->
            if (!checkIfLayoutIsAvailable()) return@setOnClickListener
            loadingDialog = AndroidUtils.makeLoadingDialog(requireContext(),"Creating...")
            loadingDialog.show()

            EventBus.getDefault().post(SessionMessageEvent(SessionMessageTypeEnum.START_SESSION))

            end_session.isGone = true
            createSessionView.visibility = View.VISIBLE
        }

        end_session.setOnClickListener { endSessionView ->
            if (!checkIfLayoutIsAvailable()) return@setOnClickListener
            loadingDialog = AndroidUtils.makeLoadingDialog(requireContext(),"Ending...")
            loadingDialog.show()
            EventBus.getDefault().post(SessionMessageEvent(SessionMessageTypeEnum.STOP_SESSION))
            endSessionView.isGone = true
            create_session.visibility = View.VISIBLE
        }

        btn_ping.setOnClickListener {
//            val fragmentSessionDetailsDialog =
//            fragmentSessionDetailsDialog.show(parentFragmentManager,"taguinha")
        }

        val metrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(metrics)

        testModel.getCompletedSessions().observe(requireActivity()) {
            if(sessions_recycler_view.childCount == it.size) return@observe
            Log.v("RV","recycler view updated")
            sessions_recycler_view.adapter = SessionDetailsAdapter(
                sessions = it,
                parentFragmentManager = parentFragmentManager,
                lifecycleOwner = requireActivity(),
                chartBackground = resources.getDrawable(R.drawable.white_background_round_20),
                displayMetrics = metrics
            )
            sessions_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        }
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }
    //</editor-fold>

    //<editor-fold name="AUX FUNCTIONS">
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCreatedSession(messageEvent: MessageEvent){
        if(messageEvent !is StringMessageEvent)return
        if(messageEvent.message != DEFAULT_SESSION_ID)
            testModel.updateSessionStartDate(messageEvent.message)
        loadingDialog.dismiss()
    }

    private fun checkIfLayoutIsAvailable() = this.isResumed
    //</editor-fold>

}