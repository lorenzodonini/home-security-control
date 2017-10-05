package com.donini.tech.homesec.ble

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.RemoteException
import android.util.Log
import org.altbeacon.beacon.*
import java.util.*

class BleBeaconController(private val context: Context): BeaconConsumer {
    enum class BleBeaconType(val layout: String) {
        IBEACON("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"),
        ALTBEACON(BeaconParser.ALTBEACON_LAYOUT),
        EDDYSTONEURL(BeaconParser.EDDYSTONE_URL_LAYOUT),
        EDDYSTONEUDI(BeaconParser.EDDYSTONE_UID_LAYOUT)
    }

    private val TAG = "BleBeaconController"
    private val REGION_ID = "HomeSecRegion"
    private val beaconManager: BeaconManager = BeaconManager.getInstanceForApplication(context)
    private var beacons: MutableList<Beacon> = arrayListOf()
    private var regions: MutableList<Region> = arrayListOf()

    init {
        beaconManager.bind(this)
    }

    fun getCurrentBeacons(): List<Beacon> {
        return beacons
    }

    fun addBeaconType(type: BleBeaconType) {
        Log.d(TAG, type.layout)
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(type.layout))
    }

    fun startMonitoring(uuid: String?, major: Int?, minor: Int?) {
        try {
            val region = Region(REGION_ID,
                    if (uuid != null) Identifier.parse(uuid) else null,
                    if (major != null) Identifier.fromInt(major) else null,
                    if (minor != null) Identifier.fromInt(minor) else null)
            if (regions.find {
                r -> r.uniqueId == region.uniqueId && r.id1 == region.id1 && r.id2 == region.id2 && r.id3 == region.id3
            } == null) {
                regions.add(region)
            }
            beaconManager.startMonitoringBeaconsInRegion(region)
        } catch (e: RemoteException) {
            Log.i(TAG, "Couldn't start monitoring beacons in region")
        }
    }

    fun stopMonitoring(uuid: String?, major: Int?, minor: Int?) {
        val region = Region(REGION_ID,
                if (uuid != null) Identifier.parse(uuid) else null,
                if (major != null) Identifier.fromInt(major) else null,
                if (minor != null) Identifier.fromInt(minor) else null)
        val index = regions.indexOfFirst {
            r -> r.uniqueId == region.uniqueId && r.id1 == region.id1 && r.id2 == region.id2 && r.id3 == region.id3
        }
        if (index >= 0) {
            regions.removeAt(index)
        }
        beaconManager.stopMonitoringBeaconsInRegion(region)
        beaconManager.stopRangingBeaconsInRegion(region)
    }

    fun unbind() {
        beaconManager.unbind(this)
    }

    // Beacon Consumer
    override fun onBeaconServiceConnect() {
        Log.i(TAG, "Beacon service connected")
        beaconManager.addMonitorNotifier(object : MonitorNotifier {
            override fun didEnterRegion(region: Region) {
                Log.i(TAG, "New beacon in region ${region.uniqueId}")
                beaconManager.startRangingBeaconsInRegion(region)
            }

            override fun didExitRegion(region: Region) {
                Log.i(TAG, "No longer see a beacon in region ${region.uniqueId}")
                beaconManager.stopRangingBeaconsInRegion(region)
            }

            override fun didDetermineStateForRegion(state: Int, region: Region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: $state")
            }
        })
        beaconManager.addRangeNotifier { beacons, region ->
            for (beacon in beacons) {
                Log.w(TAG, "[${beacon.bluetoothAddress} - ${beacon.bluetoothName}] distance: ${beacon.distance}")
            }
        }
    }

    override fun unbindService(p0: ServiceConnection?) {
        context.unbindService(p0)
    }

    override fun bindService(p0: Intent?, p1: ServiceConnection?, p2: Int): Boolean {
        return context.bindService(p0, p1, p2)
    }

    override fun getApplicationContext(): Context {
        return context
    }
}