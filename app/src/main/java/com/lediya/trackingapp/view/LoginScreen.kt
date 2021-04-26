package com.lediya.trackingapp.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.lediya.trackingapp.R
import com.lediya.trackingapp.databinding.ActivityLogicScreenBinding
import com.lediya.trackingapp.enums.ResultType
import com.lediya.trackingapp.manager.SharedPreferencesManager
import com.lediya.trackingapp.view.alertdialog.NegativeAlertDialog
import com.lediya.trackingapp.viewmodel.LoginViewModel


class LoginScreen: BaseActivity() , NegativeAlertDialog.NegativeAlertDialogListener{
    private lateinit var  binding: ActivityLogicScreenBinding
    private lateinit var viewModel: LoginViewModel
    private  val  OPTIMIZATION_REQUEST =0
    private  val  IGNORE_OPTIMIZATION_REQUEST =1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = SharedPreferencesManager.getInstance(this).getTripDetails()
        if (data != null) {
            if(data.equals("")){
                setContentView()
            }else{
                onValidateSuccess()
            }
        }else{
            setContentView()
        }
    }
    private fun setContentView(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_logic_screen)
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        onObserver()
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun onContinueClick(view: View) {
        if(!SharedPreferencesManager.getInstance(this).getBooleanSharedObject(SharedPreferencesManager.KEY_OPTIMIZE)){
            checkBatteryOptimized()
        }else{
            validation()
        }
    }
    private fun onObserver(){
        viewModel.loginResult.observe(this@LoginScreen, Observer { event ->
            event.getContentIfNotHandled()?.let { result ->
                when (result.resultType) {
                    ResultType.SUCCESS -> {
                        hideLoading()
                        onOtpScreen()
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
        binding.continueButton.isEnabled = true
        window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.continueButton.isEnabled = false
        window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        window?.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }
    private fun  onValidateSuccess(){
        val intent = Intent(this, HomeScreen::class.java)
        startActivity(intent)
        finish()
    }
    private fun  onOtpScreen(){
        val intent = Intent(this, OtpScreen::class.java)
        startActivity(intent)
        finish()
    }
    /**
     * return true if in App's Battery settings "Not optimized" and false if "Optimizing battery use"
     */
    private fun isIgnoringBatteryOptimizations(): Boolean {
        val pwrm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = applicationContext.packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return pwrm.isIgnoringBatteryOptimizations(name)
        }
        return true
    }
    private fun checkBattery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            if (isIgnoringBatteryOptimizations())
                intent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            else {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
            }
            startActivityForResult(intent, OPTIMIZATION_REQUEST)
        }else{
            validation()
        }
    }
    private fun checkBatteryOptimized(){
        if (!isIgnoringBatteryOptimizations() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showConnectivityFailureWarning(getString(R.string.connectivity_error_message),this)
        }else{
            validation()
        }
    }

    override fun negativeActionButtonClicked() {
        checkBattery()
    }

    override fun positiveActionButtonClicked() {
        showSuccessDialog("Enable Battery Optimize")
    }

    override fun closeButtonClicked() {

    }
    private fun validation(){
        SharedPreferencesManager.getInstance(this).setBooleanSharedObject(
                SharedPreferencesManager.KEY_OPTIMIZE,
                true
        )
        showLoading()
        viewModel.truckNumber.value = binding.truckNumberText.text.toString()
        viewModel.truckPhoneNumber.value = binding.phoneText.text.toString()
        viewModel.validationTruckNumber()
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (requestCode == OPTIMIZATION_REQUEST) {
            val isIgnoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(
                    packageName
            )
            if (isIgnoringBatteryOptimizations) {
                validation()
            } else {
                val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                startActivityForResult(intent, IGNORE_OPTIMIZATION_REQUEST)
            }
        }else if(requestCode == IGNORE_OPTIMIZATION_REQUEST){
          /*val isIgnoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(
                    packageName)*/
            if (isIgnoringBatteryOptimizations()) {
                validation()
            }
        }
    }

}