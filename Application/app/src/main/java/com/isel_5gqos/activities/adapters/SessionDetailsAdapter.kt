package com.isel_5gqos.activities.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.card.MaterialCardView
import com.isel_5gqos.R
import com.isel_5gqos.activities.fragments.FragmentSessionDetailsDialog
import com.isel_5gqos.common.DEFAULT_SESSION_ID
import com.isel_5gqos.common.ServingCellIndex
import com.isel_5gqos.common.ThroughputIndex
import com.isel_5gqos.common.db.entities.Session
import com.isel_5gqos.models.TestViewModel
import kotlinx.android.synthetic.main.fragment_controlled_session.*
import kotlinx.android.synthetic.main.fragment_main_session.*
import kotlinx.android.synthetic.main.fragment_session_details_dialog.*
import java.lang.Long.max
import java.text.SimpleDateFormat
import java.util.*

class SessionDetailsAdapter(
    private val sessions: List<Session>,
    private val parentFragmentManager: FragmentManager,
    private val lifecycleOwner: LifecycleOwner,
    private val chartBackground: Drawable,
    private val displayMetrics: DisplayMetrics
) : RecyclerView.Adapter<SessionDetailsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionDetailsViewHolder {
        val sessionLayout = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.session_details_recycler_view_adapter, null) as LinearLayout

        return SessionDetailsViewHolder(
            sessionLayout,
            parentFragmentManager,
            lifecycleOwner,
            chartBackground,
            parent.context,
            displayMetrics
        )
    }

    override fun onBindViewHolder(holder: SessionDetailsViewHolder, position: Int) {
        holder.bindTo(sessions[position])
    }

    override fun getItemCount(): Int = sessions.size
}


class SessionDetailsViewHolder(
    private val view: LinearLayout,
    private val parentFragmentManager: FragmentManager,
    private val lifecycleOwner: LifecycleOwner,
    private val chartBackground: Drawable,
    private val context: Context,
    private val displayMetrics: DisplayMetrics
) : RecyclerView.ViewHolder(view) {
    private lateinit var session: Session

    private val sessionNameTextView = view.findViewById<TextView>(R.id.session_details_name)
    private val sessionStartDateTextView = view.findViewById<TextView>(R.id.session_start_date)
    private val sessionDurationTextView = view.findViewById<TextView>(R.id.session_duration)

    fun bindTo(session: Session) {

        this.session = session

        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm")

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = session.beginDate

        sessionNameTextView.text = session.sessionName
        sessionStartDateTextView.text = formatter.format(calendar.time).toString()
        sessionDurationTextView.text = "${(session.endDate - session.beginDate) / 1000} sec."
        view.layoutParams = LinearLayout.LayoutParams((displayMetrics.widthPixels * 0.9).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    init {
        val card = view.findViewById<MaterialCardView>(R.id.session_card)
        card.setOnClickListener {
            val dialog = FragmentSessionDetailsDialog(session,chartBackground,lifecycleOwner)
            dialog.show(parentFragmentManager, "DIALOG_TAG")
            val dialogView = LayoutInflater
                .from(context)
                .inflate(R.layout.fragment_session_details_dialog, null)


        }
    }




    //</editor-fold>


}