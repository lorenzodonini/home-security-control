package com.donini.tech.homesec

import com.donini.tech.homesec.api.ApiController

class HomeManager {
    interface AlarmDelegate {
        fun onAlarmActivated()
        fun onAlarmDeactivated()
        fun onAlarmTriggered()
    }

    enum class AlarmStatus {
        INACTIVE, ACTIVE, SAFE, TRIGGERED
    }

    private var alarmActive = false
    private val apiController = ApiController()
    private var alarmDelegate: AlarmDelegate? = null

    fun isAlarmActive(): Boolean {
        alarmActive = apiController.getAlarm()
        return alarmActive
    }

    fun checkIn() {

    }

    fun checkOut() {

    }

    fun setAlarmActive(active: Boolean) {

    }

    fun setAlarmDelegate(delegate: AlarmDelegate) {
        alarmDelegate = delegate
    }

    fun getLiveStream() {

    }

    fun getCurrentEvents() {

    }

    fun getPastEvents() {

    }
}