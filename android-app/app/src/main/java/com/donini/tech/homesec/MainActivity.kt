package com.donini.tech.homesec

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ToggleButton
import com.donini.tech.homesec.ble.BleBeacon
import com.donini.tech.homesec.ble.BleBeaconController
import kotlinx.android.synthetic.main.content_main.*
import org.altbeacon.beacon.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, BleBeaconController.IBleBeaconListener {
    val TAG = "HomeSec"
    private val PERMISSION_REQUEST_COARSE_LOCATION = 1
    private val BLE_SERVICE_ID = "15ead454-c858-4a23-bb60-19e5cf1bcf2f"
    private var beaconController: BleBeaconController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        // Checking if bluetooth is enabled first
        checkBluetooth()

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.setDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        //Custom logic setup
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Home Control needs location access")
                builder.setMessage("Please grand location access so this app can detect beacons")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener { dialogInterface: DialogInterface? ->
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_COARSE_LOCATION)
                }
                builder.show()
            }
        }

        beaconController = BleBeaconController(this)
        beaconController?.addBeaconType(BleBeaconController.BleBeaconType.IBEACON)

        alarmToggleButton.setClickListener { Log.i("LOL", "Clicked") }
        scanToggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                this.startScan()
            } else {
                this.stopScan()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        beaconController?.unbind()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG,"Coarse location permission granted")
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Functionality limited")
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener { }
                    builder.show()
                }
                return
            }
        }
    }

    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun checkBluetooth() {
        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Bluetooth needs to be enabled for this application to work")
                builder.setMessage("Enable bluetooth now?")
                builder.setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { _, _ ->
                    val btAdapter = BluetoothAdapter.getDefaultAdapter()
                    if (!btAdapter.isEnabled) {
                        btAdapter.enable()
                    }
                })
                builder.setNegativeButton(android.R.string.no, DialogInterface.OnClickListener { _, _ ->
                    finish()
                    System.exit(0)
                })
                builder.setOnDismissListener {
                    finish()
                    System.exit(0)
                }
                builder.show()
            }
        } catch (e: RuntimeException) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Bluetooth is not available")
            builder.setMessage("This device does not support Bluetooth LE. This app will be closed.")
            builder.setPositiveButton(android.R.string.ok, null)
            builder.setOnDismissListener {
                finish()
                System.exit(0)
            }
        }
    }

    private fun startScan() {
        beaconController?.setBeaconListener(this)
        beaconController?.startMonitoring(BLE_SERVICE_ID, 0, 0)
    }

    private fun stopScan() {
        beaconController?.setBeaconListener(null)
        beaconController?.stopMonitoring(BLE_SERVICE_ID, 0, 0)
    }

    //Beacon Listener callbacks
    override fun onBeaconAppear(beacon: BleBeacon) {
        Log.i("HomeManager", "New Beacon: ${beacon.beacon.bluetoothAddress} - ${beacon.beacon.distance}")
    }

    override fun onDistanceUpdate(beacon: BleBeacon) {
        Log.i("HomeManager", "Update Beacon: ${beacon.beacon.bluetoothAddress} - ${beacon.beacon.distance}")
    }

    override fun onBeaconDisappear(beacon: BleBeacon) {
        Log.i("HomeManager", "Beacon disappeared: ${beacon.beacon.bluetoothAddress} - ${beacon.beacon.distance}")
    }
}
