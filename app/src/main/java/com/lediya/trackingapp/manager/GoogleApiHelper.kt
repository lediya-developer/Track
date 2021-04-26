package com.lediya.trackingapp.manager

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.lediya.trackingapp.TrackAppApplication
import java.io.IOException
import java.util.*


class GoogleApiHelper(context: Context) : GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,LocationListener {
    private val context: Context = context
    var googleApiClient: GoogleApiClient? = null
        private set
    private var connectionBundle: Bundle? = null
    var locationRequest = LocationRequest()
    lateinit var currentLocation:Location
    var checkPermission = false
    private var connectionListener: ConnectionListener? = null
    fun setConnectionListener(connectionListener: ConnectionListener) {
        this.connectionListener = connectionListener
        if (this.connectionListener != null && isConnected) {
            connectionListener.onConnected(connectionBundle)
        }
    }
    fun connect() {
        if (googleApiClient != null) {
            googleApiClient?.connect()
        }
    }

    fun disconnect() {
        if (googleApiClient != null && googleApiClient?.isConnected!!) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
            googleApiClient?.disconnect()
        }
    }

    val isConnected: Boolean
        get() = googleApiClient != null && googleApiClient!!.isConnected

    private fun buildGoogleApiClient() {
        googleApiClient = GoogleApiClient.Builder(context)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API).build()
    }
    private fun locationRequest(){
        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 30 * 1000.toLong()
        locationRequest.fastestInterval = 5 * 1000.toLong()
    }

    override fun onConnected(bundle: Bundle?) {
        connectionBundle = bundle
        locationRequest()
        locationRequestCheck()
        Log.d(GoogleApiHelper.TAG, "Connected to Google API")
        if (connectionListener != null) {
            connectionListener!!.onConnected(bundle)
        }
    }

    override fun onConnectionSuspended(i: Int) {
        Log.d(TAG, "onConnectionSuspended: googleApiClient.connect()")
        googleApiClient?.connect()
        if (connectionListener != null) {
            connectionListener?.onConnectionSuspended(i)
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(
            TAG,
            "onConnectionFailed: connectionResult = $connectionResult"
        )
        if (connectionListener != null) {
            connectionListener?.onConnectionFailed(connectionResult)
        }
    }

    interface ConnectionListener {


        fun onConnectionFailed(connectionResult: ConnectionResult)
        fun onConnectionSuspended(i: Int)
        fun onConnected(bundle: Bundle?)
    }
    companion object {
        private val TAG = GoogleApiHelper::class.java.simpleName
            val ACTION_LOCATION_BROADCAST =
                GoogleApiHelper::class.java.name + "LocationBroadcast"
            const val EXTRA_LOCATION = "extra_location"
    }

    init {
        buildGoogleApiClient()
        connect()
    }

    override fun onLocationChanged(location: Location) {
        currentLocation = location
        SharedPreferencesManager.getInstance(context).setStringSharedObject(SharedPreferencesManager.KEY_LOCATION,""+location.latitude+","+location.longitude)
        getCurrentLocationName()
        if(TrackAppApplication.isActivityVisible()){
            sendMessageToUI()
        }
    }
    private fun locationRequestCheck(){
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Enable Permissions", Toast.LENGTH_LONG).show();
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
            googleApiClient, locationRequest, this)
    }
    private fun sendMessageToUI() {
        Log.d(TAG, "Sending info...")
        val intent = Intent(ACTION_LOCATION_BROADCAST)
        val bundle = Bundle()
        bundle.putParcelable(EXTRA_LOCATION, currentLocation)
        intent.putExtra(EXTRA_LOCATION, bundle)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

    }
    private fun getCurrentLocationName() {
            val geocoder = Geocoder(context, Locale.ENGLISH)
            try {
                val addresses: List<Address> = currentLocation.let { geocoder.getFromLocation(it.latitude, it.longitude, 1) } as List<Address>
                val returnedAddress: Address = addresses[0]
                Log.e("Test","City :- "+addresses[0].locality)
                Log.e("Test","admin Area:- "+addresses[0].adminArea)
                if(addresses[0].locality!=null&& !addresses[0].locality.isNullOrEmpty()&&!addresses[0].locality.isNullOrBlank()){
                    SharedPreferencesManager.getInstance(context).setStringSharedObject(SharedPreferencesManager.KEY_LOCATION_NAME, returnedAddress.locality)

                }else if(addresses[0].subAdminArea!=null&& !addresses[0].subAdminArea.isNullOrEmpty()&&!addresses[0].subAdminArea.isNullOrBlank()) {
                    SharedPreferencesManager.getInstance(context).setStringSharedObject(SharedPreferencesManager.KEY_LOCATION_NAME, returnedAddress.subAdminArea)
                }
                else{
                    SharedPreferencesManager.getInstance(context).setStringSharedObject(SharedPreferencesManager.KEY_LOCATION_NAME, returnedAddress.adminArea)
                }
                checkPermission = true
            } catch (e: IOException) {
                e.printStackTrace()
            }
    }
}