package com.donini.tech.homesec.ble

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.RemoteException
import android.util.Log
import org.altbeacon.beacon.*
import org.joda.time.Duration

class BleBeaconController(private val context: Context): BeaconConsumer {
    /**
     * Beacon Listener interface
     */
    interface IBleBeaconListener {
        fun onBeaconAppear(beacon: BleBeacon)
        fun onDistanceUpdate(beacon: BleBeacon)
        fun onBeaconDisappear(beacon: BleBeacon)
    }

    /**
     * Beacon format type enum
     */
    enum class BleBeaconType(val layout: String) {
        IBEACON("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"),
        ALTBEACON(BeaconParser.ALTBEACON_LAYOUT),
        EDDYSTONEURL(BeaconParser.EDDYSTONE_URL_LAYOUT),
        EDDYSTONEUDI(BeaconParser.EDDYSTONE_UID_LAYOUT)
    }

    /**
     * Class content
     */
    private val TAG = "BleBeaconController"
    private val REGION_ID = "HomeSecRegion"
    private val DEFAULT_TIMEOUT: Long = 10000
    private val beaconManager: BeaconManager = BeaconManager.getInstanceForApplication(context)
//    private var beacons: MutableMap<String, BleBeacon> = hashMapOf()
    private var beacons: MutableSet<BleBeacon> = hashSetOf()
    private var regions: MutableList<Region> = arrayListOf()
    private var beaconListener: IBleBeaconListener? = null
    var beaconTimeout = Duration.millis(DEFAULT_TIMEOUT)

    init {
        beaconManager.bind(this)
    }

    fun getCurrentBeacons(): List<BleBeacon> {
        return beacons.toList()
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

    fun setBeaconListener(beaconListener: IBleBeaconListener?) {
        this.beaconListener = beaconListener
    }

    private fun updateBeacons(newBeacons: Collection<Beacon>) {
        //Cleanup old beacons
        for (beacon in beacons.iterator()) {
            if (beacon.timestamp.plus(beaconTimeout).isBeforeNow) {
                beacons.remove(beacon)
                beaconListener?.onBeaconDisappear(beacon)
            }
        }
        //Add/update beacons
        for (beacon in newBeacons) {
            val newBeacon = BleBeacon(beacon)
            val updating = beacons.remove(newBeacon)
            beacons.add(newBeacon)
            if (updating) {
                beaconListener?.onDistanceUpdate(newBeacon)
            } else {
                beaconListener?.onBeaconAppear(newBeacon)
            }
        }
    }

    // Beacon Consumer
    override fun onBeaconServiceConnect() {
        Log.i(TAG, "Beacon service connected")
        val defaultRegion = Region(REGION_ID, null, null, null)
        beaconManager.stopRangingBeaconsInRegion(defaultRegion)
        beaconManager.stopMonitoringBeaconsInRegion(defaultRegion)
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
            updateBeacons(beacons)
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