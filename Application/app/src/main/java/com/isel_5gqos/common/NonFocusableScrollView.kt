package com.isel_5gqos.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ScrollView

class NestedFocusingScrollView : ScrollView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun isNestedScrollingEnabled(): Boolean = true

    override fun onStartNestedScroll(child: View?, target: View?, nestedScrollAxes: Int): Boolean {
        val started = super.onStartNestedScroll(child, target, nestedScrollAxes)
        if(started) {
            this.isVerticalScrollBarEnabled = false
            this.isHorizontalScrollBarEnabled = false
        }
        return started;
    }

    override fun onStopNestedScroll(target: View?) {
        super.onStopNestedScroll(target)
        this.isVerticalScrollBarEnabled = true
        this.isHorizontalScrollBarEnabled = true
    }

}