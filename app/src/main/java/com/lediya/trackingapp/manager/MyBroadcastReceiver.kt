package com.lediya.trackingapp.manager

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import com.lediya.trackingapp.TrackAppApplication
import com.lediya.trackingapp.communication.RestClient
import com.lediya.trackingapp.data.model.StartTripId
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
            GlobalScope.launch {
                val connect = TrackAppApplication.isConnected()
                if(connect!=null) {
                    if (connect) {
                        if (isMyServiceRunning(context, LocationUpdateService::class.java)) {
                        } else {
                            val background = Intent(context, LocationUpdateService::class.java)
                            context.startService(background)
                        }
                        val locationName =
                            SharedPreferencesManager.getInstance(context).getStringSharedObject(
                                SharedPreferencesManager.KEY_LOCATION_NAME
                            )
                        val locationStr =
                            SharedPreferencesManager.getInstance(context).getStringSharedObject(
                                SharedPreferencesManager.KEY_LOCATION
                            )
                        val routeId =
                            SharedPreferencesManager.getInstance(context).getStringSharedObject(
                                SharedPreferencesManager.KEY_ROUTE_ID
                            )
                        if (!locationStr.isNullOrBlank() && !locationName.isNullOrBlank()) {
                            val strings = locationStr.split(",")
                            val coordinatesValue = mutableListOf<Double>()
                            coordinatesValue.add(strings[0].toDouble())
                            coordinatesValue.add(strings[1].toDouble())
                            val startTripId = StartTripId(coordinatesValue, locationName, routeId)
                            val result = RestClient.getInstance().updateLocation(startTripId)
                            if (result.isSuccessful) {
                                Log.e(
                                    MyBroadcastReceiver::class.java.simpleName,
                                    "Location is Updated to server"
                                )
                            } else {
                                Log.e(
                                    MyBroadcastReceiver::class.java.simpleName,
                                    "Location is not Updated"
                                )
                            }
                        }
                    }
                    setAlarm(context)
                }
            }
    }
    private fun setAlarm(context: Context) {
        val frequentTimeStr = SharedPreferencesManager.getInstance(context).getStringSharedObject(SharedPreferencesManager.KEY_FREQUENT_TIME)
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
                val intent = Intent(context, MyBroadcastReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context, 234324243, intent, 0
                )
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() +frequentTimeInt * 1000,
                        pendingIntent
                    )
                } else if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT && Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                    alarmManager.setExact(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() +frequentTimeInt * 1000,
                        pendingIntent
                    )
                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() +frequentTimeInt * 1000, pendingIntent)
                }
            }

        }
    }
    private fun isMyServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services = activityManager.getRunningServices(Int.MAX_VALUE)
        if (services != null) {
            for (i in services.indices) {
                if (serviceClass.name == services[i].service.className && services[i].pid != 0) {
                    return true
                }
            }
        }
        return false
    }
}