package com.lediya.trackingapp.manager

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.lediya.trackingapp.R
import com.lediya.trackingapp.data.model.Data


/**
 * Shared preferences manager for the application
 */
class SharedPreferencesManager(context: Context) {
    private var sharedPreference: SharedPreferences? = null


    companion object {
        const val KEY_TRIP = "KEY_TRIP"
        const val KEY_ROUTE_ID ="KEY_ROUTE_ID"
        const val KEY_ROUTE_START ="KEY_ROUTE_START"
        const val KEY_FREQUENT_TIME ="KEY_FREQUENT_TIME"
        const val KEY_SERVICE_START ="KEY_SERVICE_START"
        const val KEY_LOCATION ="KEY_LOCATION"
        const val KEY_LOCATION_NAME = "KEY_LOCATION_NAME"
        const val KEY_OPTIMIZE = "KEY_OPTIMIZE"
        private var instance: SharedPreferencesManager? = null

        fun getInstance(context: Context): SharedPreferencesManager {
            if (instance == null) {
                instance = SharedPreferencesManager(context)
            }
            return instance as SharedPreferencesManager
        }
    }

    init {
        sharedPreference = context.getSharedPreferences(
            context.getString(R.string.track_shared_preferences),
            Context.MODE_PRIVATE
        )
    }



    /**
     * set inspection timings value as string in shared preferences
     */
    fun setTripDetails(inspectionTimings: Data) {
        val editor = sharedPreference?.edit()
        val gson = Gson()
        val json = gson.toJson(inspectionTimings)
        editor?.putString(KEY_TRIP, json)
        editor?.apply()
    }

    /**
     * function to get inspection timings from shared preference
     */
    fun getTripDetails(): Data? {
        val gson = Gson()
        val json = sharedPreference?.getString(KEY_TRIP, "")
        return if(json!=null){
            gson.fromJson(json, Data::class.java)
        }else{
            null
        }

    }

    /**
     * set String shared preference object
     */
    fun setStringSharedObject(key: String, value: String?) {
        val editor = sharedPreference?.edit()
        if(value!=null){
            editor?.putString(key, value)
            editor?.apply()
        }
    }

    /**
     * function to get string shared preference object
     */
    fun getStringSharedObject(key: String): String? {
        return if (sharedPreference != null) {
            sharedPreference!!.getString(key, "")
        } else ""
    }

    /**
     * set boolean shared preference object
     */
    fun setBooleanSharedObject(key: String, item: Boolean) {
        val editor = sharedPreference?.edit()
        editor?.putBoolean(key, item)
        editor?.apply()
    }

    /**
     * function to get boolean shared preference object
     */
    fun getBooleanSharedObject(key: String): Boolean {
        return if (sharedPreference != null) {
            sharedPreference!!.getBoolean(key, false)
        }
        else false
    }
     fun clearSharedPreference(){
         val editor = sharedPreference?.edit()
         editor?.clear()
         editor?.apply()
     }
}