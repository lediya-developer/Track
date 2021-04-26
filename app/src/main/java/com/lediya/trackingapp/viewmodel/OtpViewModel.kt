package com.lediya.trackingapp.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lediya.trackingapp.communication.RestClient
import com.lediya.trackingapp.data.Event
import com.lediya.trackingapp.data.ResultImp
import com.lediya.trackingapp.data.model.TripDetailRequest
import com.lediya.trackingapp.enums.ResultType
import com.lediya.trackingapp.utility.Utils
import kotlinx.coroutines.launch
import com.lediya.trackingapp.manager.SharedPreferencesManager
import org.json.JSONObject

class OtpViewModel (application: Application) : AndroidViewModel(application) {
    private val _loginResult = MutableLiveData<Event<ResultImp>>()
    val loginResult: LiveData<Event<ResultImp>>
        get() = _loginResult
    private var context: Context = application
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun tripDetailRequest()= viewModelScope.launch {
        if(Utils.isSafeForAPICall(context)){
            _loginResult.postValue(Event(ResultImp(ResultType.PENDING)))
            val validateData = TripDetailRequest("1234",SharedPreferencesManager.getInstance(context)
                    .getStringSharedObject(SharedPreferencesManager.KEY_ROUTE_ID))
            val result = RestClient.getInstance().validateOtp(validateData)
            if(result.isSuccessful){
                if(result.body()!=null){
                    result.body()?.data?.let {
                        SharedPreferencesManager.getInstance(context)
                            .setTripDetails(it)
                    }
                    _loginResult.postValue(Event(ResultImp(ResultType.SUCCESS)))
                }else{
                    _loginResult.postValue(Event(ResultImp(ResultType.FAILURE)))
                }
            }else{
                try {
                    val errorBody = result.errorBody()?.string()
                    if (errorBody != null) {
                        val jObjError = JSONObject(errorBody)
                        val msg = jObjError.getString("message")
                        _loginResult.postValue(Event(ResultImp(ResultType.FAILURE,msg)))
                    } else{
                        _loginResult.postValue(Event(ResultImp(ResultType.FAILURE)))
                    }

                } catch (e: Exception) {
                    _loginResult.postValue(Event(ResultImp(ResultType.FAILURE)))
                }
            }
        }else{
            _loginResult.value=Event(ResultImp(ResultType.FAILURE, "Network Error Failure"))
        }
    }

}