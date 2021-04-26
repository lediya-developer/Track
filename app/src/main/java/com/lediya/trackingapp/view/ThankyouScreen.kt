package com.lediya.trackingapp.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.DataBindingUtil
import com.lediya.trackingapp.R
import com.lediya.trackingapp.databinding.ThankyouScreenBinding
import com.lediya.trackingapp.utility.Constants

class ThankyouScreen : AppCompatActivity() {
    lateinit var binding:ThankyouScreenBinding
    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.thankyou_screen)
        val boolean= intent.extras?.getBoolean(Constants.BOOLEAN)
            if (boolean!=null){
                if(boolean){
                    binding.thankImageView.setImageDrawable(getDrawable(R.drawable.ic_tick))
                    binding.textDes.text = getString(R.string.trip)
                }else{
                    binding.thankImageView.setImageDrawable(getDrawable(R.drawable.ic_cancel))
                    binding.textDes.text = getString(R.string.cancel_trip)
                }
            }
        binding.button.setOnClickListener{
            val intent = Intent(this@ThankyouScreen, LoginScreen::class.java)
            finish()
            startActivity(intent)
        }
        }

    }
