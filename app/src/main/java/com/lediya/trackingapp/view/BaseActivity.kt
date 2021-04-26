package com.lediya.trackingapp.view

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.lediya.trackingapp.R
import com.lediya.trackingapp.view.alertdialog.NegativeAlertDialog
import java.util.*


/**
 * Base activity for application
 */
open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return super.onCreateView(name, context, attrs)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

  /*  fun hideLoading() {
        val progressbar = findViewById<ConstraintLayout>(R.id.progress_bar)
        progressbar?.visibility = View.GONE
        window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    fun showLoading() {
        val progressbar = findViewById<ConstraintLayout>(R.id.progress_bar)
        progressbar?.visibility = View.VISIBLE
        window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }*/

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResourcesLocale(context: Context, locale: Locale): Context? {
        val configuration: Configuration = context.resources.configuration
        configuration.setLocale(locale)
        val updatedContext = context.createConfigurationContext(configuration)
        onConfigurationChanged(configuration)
        return updatedContext
    }

    @Suppress("DEPRECATION")
    private fun updateResourcesLocaleLegacy(
        context: Context,
        locale: Locale
    ): Context? {
        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration
        configuration.locale = locale
        resources.updateConfiguration(configuration, resources.displayMetrics)
        onConfigurationChanged(configuration)
        return context
    }

    override fun attachBaseContext(newBase: Context?) {
        newBase?.let {
            super.attachBaseContext(newBase)
        }
    }
    fun showErrorDialog(message: String?) {
        val msg = message ?: getString(R.string.an_error_occurred)
        val alert = NegativeAlertDialog(this, R.style.AppDialogTheme)
        alert.createCancelOKWarning(msg)
        alert.show()
    }

    fun showConnectivityFailureWarning(message:String,negativeAlertDialogListener: NegativeAlertDialog.NegativeAlertDialogListener) {
        val alert = NegativeAlertDialog(this, R.style.AppDialogTheme)
        alert.createCancelOKWarning(message, negativeAlertDialogListener)
        alert.show()
    }
    private fun showTimeOutWarning() {
        val alert = NegativeAlertDialog(this, R.style.AppDialogTheme)
        alert.createCancelOKWarning(getString(R.string.timeout_error_message), null)
        alert.show()
    }
    fun showSuccessDialog(message: String?) {
        val msg = message ?: getString(R.string.an_error_occurred)
        val alert = NegativeAlertDialog(this, R.style.AppDialogTheme)
        alert.createSuccessDialog(msg)
        alert.show()
    }
}