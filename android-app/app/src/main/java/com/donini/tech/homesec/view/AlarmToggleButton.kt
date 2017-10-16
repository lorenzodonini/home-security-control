package com.donini.tech.homesec.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.donini.tech.homesec.R
import kotlinx.android.synthetic.main.alarm_toggle_button.view.*

class AlarmToggleButton @JvmOverloads constructor(context: Context,
                                                  attrs: AttributeSet,
                                                  defStyleAttr: Int = 0,
                                                  defStyleRes: Int = 0): RelativeLayout(context, attrs, defStyleAttr, defStyleRes) {
    enum class State(val backgroundResource: Int) {
        ON(R.drawable.toggle_on),
        PENDING(R.drawable.toggle_pending),
        OFF(R.drawable.toggle_off),
        UNKNOWN(R.drawable.toggle_unknown)
    }

    var state = State.OFF
    set(value) {
        setBackgroundResource(value.backgroundResource)
        if (value == State.UNKNOWN) {
            disabledIndicator.visibility = View.VISIBLE
        } else {
            disabledIndicator.visibility = View.GONE
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.alarm_toggle_button, this, true)
    }

    fun setClickListener(onClickListener: (view: View) -> Unit) {
        alarmButton.setOnClickListener { v -> onClickListener(v) }
    }

    fun setClickListener(onClickListener: OnClickListener) {
        alarmButton.setOnClickListener(onClickListener)
    }
}