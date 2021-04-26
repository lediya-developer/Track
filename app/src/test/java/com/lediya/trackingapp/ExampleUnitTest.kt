package com.lediya.trackingapp

import android.content.Context
import android.os.Build
import com.lediya.trackingapp.view.HomeScreen
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lediya.trackingapp.manager.SharedPreferencesManager
import org.robolectric.Robolectric
import androidx.test.core.app.ApplicationProvider
import com.lediya.trackingapp.view.LoginScreen


@RunWith(AndroidJUnit4::class)
@Config(maxSdk = Build.VERSION_CODES.P, minSdk = Build.VERSION_CODES.P)
class ExampleUnitTest {
    private lateinit var  activity: LoginScreen
    private lateinit var context: Context
    @Before
    fun setup() {
        activity = Robolectric.setupActivity(LoginScreen::class.java)
        context = ApplicationProvider.getApplicationContext<Context>()
    }
    @Test
    fun tripDetailsFalse(){
         val data = SharedPreferencesManager.getInstance(activity).getTripDetails()
         assertEquals(
                null,
                data)

    }
    @Test
    fun tripDetailsTrue(){
        val data = SharedPreferencesManager.getInstance(activity).getTripDetails()
        assertNotEquals(
                null,
                data
        )
    }

}