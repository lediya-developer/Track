package com.lediya.trackingapp

import android.app.Application
import android.content.Context
import com.lediya.trackingapp.manager.GoogleApiHelper
import com.lediya.trackingapp.manager.SharedPreferencesManager

class TrackAppApplication :Application(){
    lateinit  var context: Context
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
    companion object {
        fun isActivityVisible(): Boolean {
            return activityVisible
        }

        fun activityResumed() {
            activityVisible = true
        }

        fun activityPaused() {
            activityVisible = false
        }
        fun activatedGoogleApiHelper(context: Context): GoogleApiHelper {
            if (googleApiHelper == null) {
                googleApiHelper = GoogleApiHelper(context)
            }
            return googleApiHelper as GoogleApiHelper
        }
        fun isConnected(): Boolean? {
            return if(googleApiHelper!=null){
               googleApiHelper?.isConnected
            }else{
                false
            }
        }
        var activityVisible = false
        var  googleApiHelper:GoogleApiHelper? = null
    }

}

