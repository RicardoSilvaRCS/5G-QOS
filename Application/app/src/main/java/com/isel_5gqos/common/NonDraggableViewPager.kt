package com.isel_5gqos.common

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager


class NonDraggableViewPager(context: Context,attrs:AttributeSet): ViewPager(context,attrs) {
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        // returning false will not propagate the swipe event
        return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }
}