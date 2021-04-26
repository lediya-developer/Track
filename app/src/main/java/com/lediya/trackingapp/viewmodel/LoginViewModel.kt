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
import com.lediya.trackingapp.data.model.TripIdRequest
import com.lediya.trackingapp.enums.ResultType
import com.lediya.trackingapp.utility.Utils
import kotlinx.coroutines.launch
import com.lediya.trackingapp.manager.SharedPreferencesManager
import org.json.JSONObject

class LoginViewModel (application: Application) : AndroidViewModel(application) {
    var truckNumber  = MutableLiveData<String>()
    var truckPhoneNumber  = MutableLiveData<String>()
    private val _loginResult = MutableLiveData<Event<ResultImp>>()
    val loginResult: LiveData<Event<ResultImp>>
        get() = _loginResult
    private var context: Context = application
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun validationTruckNumber(){
        if(!truckNumber.value.isNullOrBlank() && !truckPhoneNumber.value.isNullOrBlank()){
            _loginResult.value=Event(ResultImp(ResultType.PENDING))
            signInUser()
        }else if(truckNumber.value.isNullOrBlank()){
            _loginResult.value=Event(ResultImp(ResultType.FAILURE, "Truck Number is Null"))
        }else if(truckPhoneNumber.value.isNullOrBlank()){
            _loginResult.value=Event(ResultImp(ResultType.FAILURE, "Otp is Null"))
        }
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun signInUser()= viewModelScope.launch {
        if(Utils.isSafeForAPICall(context)){
            _loginResult.postValue(Event(ResultImp(ResultType.PENDING)))
            val validateData = TripIdRequest(truckPhoneNumber.value,truckNumber.value)
            val result = RestClient.getInstance().validateTruckNumber(validateData)
            if(result.isSuccessful){
                if(result.body()!=null){
                    result.body()?.data?.let {
                        SharedPreferencesManager.getInstance(context).setStringSharedObject(SharedPreferencesManager.KEY_ROUTE_ID,it.routeId)
                        _loginResult.postValue(Event(ResultImp(ResultType.SUCCESS)))
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