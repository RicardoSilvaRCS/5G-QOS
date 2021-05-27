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

            /* .setTitle("Bro, you sure?")
             .setMessage("Sure sure?")
             .setPositiveButton("I'm sure") {dialogInterface,_ ->
                 dialogInterface.dismiss()
             }
             .setNegativeButton("Fuck that"){dialogInterface,_->
                 dialogInterface.dismiss()
             }
             .setNeutralButton("Not sure"){disalogInterface,_-> AndroidUtils.makeBurnedToast(requireContext(),"Pussy")}
             .show()*/
            val animSet = AnimationSet(true)
            animSet.interpolator = DecelerateInterpolator()
            animSet.fillAfter = true
            animSet.isFillEnabled = true

            val inflater = LayoutInflater.from(requireContext())
            val inflatedView = inflater.inflate(resources.getLayout(R.layout.delete_session_alert_dialog),null)
            val confirmButton = inflatedView.findViewById<Button>(R.id.session_delete_confirm_button)
            confirmButton.setOnClickListener {
                val power = (-1.0).pow(nrOfClicks.toDouble()).toFloat()
                Log.v("powerValue",power.toString())
                Log.v("powerValue",nrOfClicks.toString())
                val animRotate = RotateAnimation(
                    0.0f, (-18000.0f * nrOfClicks++ * power) ,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f
                )

                animRotate.duration = 600000
                animRotate.fillAfter = true
                animSet.addAnimation(animRotate)

                inflatedView.animation = animRotate
                inflatedView.startAnimation(animSet)
            }
            val deleteButton = inflatedView.findViewById<Button>(R.id.session_delete_cancel_button)
            deleteButton.setOnClickListener { }


            val animRotate = RotateAnimation(
                0.0f, -18000.0f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f
            )

            animRotate.duration = 600000
            animRotate.fillAfter = true
            animSet.addAnimation(animRotate)

            dialog.setView(inflatedView)
            dialog.setCanceledOnTouchOutside(false)
            dialog.window!!.setBackgroundDrawableResource(R.drawable.white_background_round_20)
            dialog.show()

            inflatedView.startAnimation(animSet)
        }
//        val metrics = resources.displayMetrics
//        val width = metrics.widthPixels
//        val height = metrics.heightPixels
//
//        dialog.window!!.setLayout((width*0.75).toInt(),(height * 0.75).toInt())
//        text.text = "O Ricky boy é um gay"
    }
}