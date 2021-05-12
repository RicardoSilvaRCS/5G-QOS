package com.isel_5gqos.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.isel_5gqos.R
import kotlinx.android.synthetic.main.fragment_controlled_session.*

class FragmentControlledSession: Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_controlled_session,container,false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        create_session.setOnClickListener {
            it.isGone = true
            end_ession.visibility = View.VISIBLE
        }
        end_ession.setOnClickListener {
            it.isGone = true
            create_session.visibility = View.VISIBLE
        }
    }
}