package com.isel_5gqos.activities.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.isel_5gqos.R
import kotlinx.android.synthetic.main.fragment_session_details_dialog.*
import kotlin.math.pow


class FragmentSessionDetailsDialog:DialogFragment() {
    public lateinit var dialog: AlertDialog
    private lateinit var dialogView: View
    private var nrOfClicks = 1

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
            val inflatedView = inflater.inflate(resources.getLayout(R.layout.delete_session_alert_dialog),null)
            val confirmButton = inflatedView.findViewById<Button>(R.id.session_delete_confirm_button)
            confirmButton.setOnClickListener {dialog.dismiss()}
            val deleteButton = inflatedView.findViewById<Button>(R.id.session_delete_cancel_button)
            deleteButton.setOnClickListener { dialog.dismiss() }


            dialog.setView(inflatedView)
            dialog.setCanceledOnTouchOutside(false)
            dialog.window!!.setBackgroundDrawableResource(R.drawable.white_background_gradient_blue_500_purple_500_red_border_round_20)
            dialog.show()

        }
    }
}