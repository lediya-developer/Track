package com.lediya.trackingapp.view

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.lediya.trackingapp.R
import com.lediya.trackingapp.TrackAppApplication
import com.lediya.trackingapp.databinding.ActivityHomeBinding
import com.lediya.trackingapp.enums.ResultType
import com.lediya.trackingapp.manager.GoogleApiHelper
import com.lediya.trackingapp.manager.LocationUpdateService
import com.lediya.trackingapp.manager.MyBroadcastReceiver
import com.lediya.trackingapp.manager.SharedPreferencesManager
import com.lediya.trackingapp.utility.Constants
import com.lediya.trackingapp.view.alertdialog.NegativeAlertDialog
import com.lediya.trackingapp.viewmodel.HomeViewModel
import java.io.IOException
import java.util.*


class HomeScreen : BaseActivity(), OnMapReadyCallback, NegativeAlertDialog.NegativeAlertDialogListener{
    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var mMap: GoogleMap
    private val locationRequestCode = 1000
    private var locationRequest: LocationRequest? = null
    private var mCurrentLocation: Location? = null
    private var  started = false
    private var  checkPermission = false
    private var isEnd = false
    private val REQUEST_CHECK_SETTINGS = 0x1
    private val LOCATION_WORK_TAG: String ="LocationWorker"
    private lateinit var googleApiHelper:GoogleApiHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_home)
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        viewModel.data.value= SharedPreferencesManager.getInstance(this).getTripDetails()
        started = SharedPreferencesManager.getInstance(this).getBooleanSharedObject(SharedPreferencesManager.KEY_ROUTE_START)
        binding.locationName.text =viewModel.data.value?.name
        setTextChange()
        onObserver()
        checkLocationPermission()
    }
    private fun setTextChange(){
        if(started){
            changeView()
            startFrequentLocationService()
        }else{
            binding.layout.visibility = View.VISIBLE
            binding.layout2.visibility = View.GONE

        }
    }

    override fun onStart() {
        super.onStart()
        locationReceiver.let {
            LocalBroadcastManager.getInstance(this).registerReceiver(
                it,  IntentFilter(GoogleApiHelper.ACTION_LOCATION_BROADCAST)
            )
        };
    }
    override fun onResume() {
        super.onResume()
        TrackAppApplication.activityResumed()
    }

    override fun onPause() {
        super.onPause()
        TrackAppApplication.activityPaused()
    }

    override fun onStop() {
        super.onStop()
        locationReceiver.let { LocalBroadcastManager.getInstance(this).unregisterReceiver(it) }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val point: CameraUpdate = CameraUpdateFactory.newLatLng(LatLng(13.067439, 80.237617))
        mMap.moveCamera(point)
        mMap.animateCamera(point)
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun onStartTrip(view: View) {
        if(!checkPermission){
            checkLocationPermission()
        }else{
            showLoading()
            viewModel.startTrip()
        }


    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun onCancelTrip(view: View) {
        isEnd = false
     //   showConnectivityFailureWarning(getString(R.string.terminate_trip),this)
        endTerminateTrip()

    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun onEndTrip(view: View) {
        isEnd = true
       // showConnectivityFailureWarning(getString(R.string.end_trip),this)
        endTerminateTrip()

    }
    private fun checkLocationPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.  ACCESS_BACKGROUND_LOCATION),
                locationRequestCode)
        } else{
            locationTurnOn()
        }

    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1000 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationTurnOn()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun onObserver(){
        viewModel.startResult.observe(this@HomeScreen, Observer { event ->
            event.getContentIfNotHandled()?.let { result ->
                when (result.resultType) {
                    ResultType.SUCCESS -> {
                        hideLoading()
                        changeView()
                        startFrequentLocationService()
                    }
                    ResultType.FAILURE -> {
                        hideLoading()
                        showErrorDialog(result.message)
                    }
                    ResultType.PENDING -> {
                        showLoading()
                    }
                }
            }
        })
        viewModel.endResult.observe(this@HomeScreen, Observer { event ->
            event.getContentIfNotHandled()?.let { result ->
                when (result.resultType) {
                    ResultType.SUCCESS -> {
                        hideLoading()
                        SharedPreferencesManager.getInstance(this).setBooleanSharedObject(SharedPreferencesManager.KEY_SERVICE_START,true)
                        stopFrequentLocationService()
                        startThankingActivity(true)
                    }
                    ResultType.FAILURE -> {
                        hideLoading()
                        showErrorDialog(result.message)
                    }
                    ResultType.PENDING -> {
                        showLoading()
                    }
                }
            }
        })
        viewModel.terminateResult.observe(this@HomeScreen, Observer { event ->
            event.getContentIfNotHandled()?.let { result ->
                when (result.resultType) {
                    ResultType.SUCCESS -> {
                        hideLoading()
                        SharedPreferencesManager.getInstance(this).setBooleanSharedObject(SharedPreferencesManager.KEY_SERVICE_START,true)
                        stopFrequentLocationService()
                        startThankingActivity(false)
                    }
                    ResultType.FAILURE -> {
                        hideLoading()
                        showErrorDialog(result.message)
                    }
                    ResultType.PENDING -> {
                        showLoading()
                    }
                }
            }
        })
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.startButton.isEnabled = true
        window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.startButton.isEnabled = false
        window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }
    private fun locationTurnOn(){
        googleApiHelper = TrackAppApplication.activatedGoogleApiHelper(this)
        googleApiHelper.connect()
        locationRequest = LocationRequest.create()
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest?.interval = 30 * 1000.toLong()
        locationRequest?.fastestInterval = 5 * 1000.toLong()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest!!)
        builder.setAlwaysShow(true)
        val result =
            LocationServices.SettingsApi.checkLocationSettings(googleApiHelper.googleApiClient, builder.build())
        result.setResultCallback { result ->
            val status: Status = result.status

            when (status.getStatusCode()) {
                LocationSettingsStatusCodes.SUCCESS -> {
                    Log.i("", "All location settings are satisfied.")
                }
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    Log.i(
                        "",
                        "Location settings are not satisfied. Show the user a dialog to" + "upgrade location settings "
                    )

                    try {
                        status.startResolutionForResult(
                            this@HomeScreen,
                            REQUEST_CHECK_SETTINGS
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e("Applicationsett", e.toString())
                    }
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    Log.i(
                        "",
                        "Location settings are inadequate, and cannot be fixed here. Dialog " + "not created."
                    )
                    Toast.makeText(
                        application,
                        "Location settings are inadequate, and cannot be fixed here",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
     //   googleApiHelper.setConnectionListener(this)
    }
    private fun setCurrentLocation(){
        mCurrentLocation?.let {
        val latLng =  LatLng(it.latitude, it.longitude)
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.title("Current Position")
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            mMap.clear()
            mMap.addMarker(markerOptions)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11f))
            val coordinatesValue = mutableListOf<Double>()
            coordinatesValue.add(it.latitude)
            coordinatesValue.add(it.longitude)
            viewModel.coordinates.value = coordinatesValue
            getCurrentLocationName()

        }
    }
   private fun getCurrentLocationName(){
       val geocoder = Geocoder(this, Locale.ENGLISH)
       try {
           val addresses: List<Address> = mCurrentLocation?.let { geocoder.getFromLocation(it.latitude, it.longitude, 1) } as List<Address>
           val returnedAddress: Address = addresses[0]
           if(addresses[0].locality!=null&& !addresses[0].locality.isNullOrEmpty()&&!addresses[0].locality.isNullOrBlank()) {
               viewModel.locationName.value = returnedAddress.locality
           }else if(addresses[0].subAdminArea!=null&& !addresses[0].subAdminArea.isNullOrEmpty()&&!addresses[0].subAdminArea.isNullOrBlank()){
               viewModel.locationName.value = returnedAddress.subAdminArea
           }else if(addresses[0].adminArea!=null&& !addresses[0].adminArea.isNullOrEmpty()&&!addresses[0].adminArea.isNullOrBlank()){
               viewModel.locationName.value = returnedAddress.adminArea
           }
           Toast.makeText(this,"City Name:"+viewModel.locationName.value,Toast.LENGTH_LONG).show()
           checkPermission = true
       } catch (e: IOException) {
           e.printStackTrace()
       }
   }
   private var locationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
       override  fun onReceive(context: Context?, intent: Intent) {
           val b: Bundle? = intent.getBundleExtra(GoogleApiHelper.EXTRA_LOCATION)
           mCurrentLocation = b?.getParcelable<Parcelable>(GoogleApiHelper.EXTRA_LOCATION) as Location
           setCurrentLocation()
       }
   }
    private fun startFrequentLocationService(){
        val frequentTimeStr = SharedPreferencesManager.getInstance(this).getStringSharedObject(SharedPreferencesManager.KEY_FREQUENT_TIME)
        if (!frequentTimeStr.isNullOrBlank()) {
            val time =frequentTimeStr.split("*")
            val frequentTimeInt = time[0].toLong()
            if(!time[1].isBlank()){
                when(time[1]){
                    "s" -> frequentTimeInt
                    "m" -> frequentTimeInt * 60
                    "h" -> frequentTimeInt * 60 * 60
                    "d" -> frequentTimeInt * 24 * 60 * 60
                }
                val intent = Intent(this, MyBroadcastReceiver::class.java)
                PendingIntent.getBroadcast(this, 0, intent,PendingIntent.FLAG_CANCEL_CURRENT)
                val pendingIntent = PendingIntent.getBroadcast(
                        this.applicationContext, 234324243, intent, 0)
                val   alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() +frequentTimeInt*1000, pendingIntent)
            }

        }
    }
    private fun stopFrequentLocationService(){
        val myService = Intent(this, LocationUpdateService::class.java)
        stopService(myService)
    }
    private fun startThankingActivity(boolean: Boolean){
        SharedPreferencesManager.getInstance(this).clearSharedPreference()
        val intent = Intent(this, ThankyouScreen::class.java)
        intent.putExtra(Constants.BOOLEAN,boolean)
        startActivity(intent)
        finish()
    }
    private fun changeView(){
        binding.layout.visibility = View.GONE
        binding.layout2.visibility = View.VISIBLE
        val strings =viewModel.data.value?.name?.split("-")
        if(strings!=null){
            binding.locationNameStr.text = strings[0]
            binding.endLocationNameStr.text = strings[1]
        }
    }
    private fun endTerminateTrip(){
        if(isEnd){
            showLoading()
            viewModel.endTrip()

        }else{
            showLoading()
            viewModel.terminateTrip()
        }
    }
    override fun negativeActionButtonClicked() {
       // endTerminateTrip()
    }

    override fun positiveActionButtonClicked() {

    }

    override fun closeButtonClicked() {

    }
}