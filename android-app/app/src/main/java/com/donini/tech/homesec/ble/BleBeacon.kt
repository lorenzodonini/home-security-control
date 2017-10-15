package com.donini.tech.homesec.ble

import org.altbeacon.beacon.Beacon
import org.joda.time.Instant

class BleBeacon(val beacon: Beacon) {
    val timestamp = Instant()

    override fun hashCode(): Int {
        return beacon.bluetoothAddress.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return (other as? BleBeacon)?.beacon?.bluetoothAddress == beacon.bluetoothAddress
    }
}