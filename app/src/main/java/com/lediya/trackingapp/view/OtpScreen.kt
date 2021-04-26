package com.lediya.trackingapp.view


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.lediya.trackingapp.R
import com.lediya.trackingapp.databinding.ActivityOtpScreenBinding
import com.lediya.trackingapp.enums.ResultType
import com.lediya.trackingapp.viewmodel.OtpViewModel


class OtpScreen : BaseActivity(){
    private lateinit var binding: ActivityOtpScreenBinding
    private lateinit var viewModel: OtpViewModel
    private lateinit var mgr: InputMethodManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView()

    }
    private fun setContentView(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_otp_screen)
        viewModel = ViewModelProviders.of(this).get(OtpViewModel::class.java)
        onObserver()

    }

    override fun onResume() {
        super.onResume()
        binding.squareField.requestFocus()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun onContinueClick(view: View) {
        if(!binding.squareField.text.isNullOrBlank()){
            val text =binding.squareField.otpValue
            if (text!=null) {
                if(text.equals("1234")){
                    showLoading()
                    viewModel.tripDetailRequest()
                }else{
                    showErrorDialog("Enter the vaild OTP")
                }
            }else{
                showErrorDialog("Enter the OTP")
            }
        }else{
            showErrorDialog("Enter the OTP")
        }

    }
    private fun onObserver(){
        viewModel.loginResult.observe(this@OtpScreen, Observer { event ->
            event.getContentIfNotHandled()?.let { result ->
                when (result.resultType) {
                    ResultType.SUCCESS -> {
                        hideLoading()
                        onValidateSuccess()
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
    private fun  onValidateSuccess(){
        val intent = Intent(this, HomeScreen::class.java)
        startActivity(intent)
        finish()
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
}