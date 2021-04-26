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
import com.lediya.trackingapp.data.model.Data
import com.lediya.trackingapp.data.model.StartTripId
import com.lediya.trackingapp.enums.ResultType
import com.lediya.trackingapp.manager.SharedPreferencesManager
import com.lediya.trackingapp.utility.Utils
import kotlinx.coroutines.launch
import org.json.JSONObject

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    var data = MutableLiveData<Data>()
    var locationName = MutableLiveData<String>()
    var coordinates = MutableLiveData<List<Double>>()
    private val _startResult = MutableLiveData<Event<ResultImp>>()
    val startResult: LiveData<Event<ResultImp>>
        get() = _startResult
    private val _endResult = MutableLiveData<Event<ResultImp>>()
    val endResult: LiveData<Event<ResultImp>>
        get() = _endResult
    private val _terminateResult = MutableLiveData<Event<ResultImp>>()
    val terminateResult: LiveData<Event<ResultImp>>
        get() = _terminateResult
    private var context: Context = application
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun startTrip()= viewModelScope.launch {
        if(Utils.isSafeForAPICall(context)){
            _startResult.postValue(Event(ResultImp(ResultType.PENDING)))
            val startTripId = StartTripId(coordinates.value,locationName.value,data.value?._id)
            val result = RestClient.getInstance().startTrip(startTripId)
            if(result.isSuccessful){
                if(result.body()!=null){
                    result.body()?.data?.let {
                        SharedPreferencesManager.getInstance(context).setBooleanSharedObject(SharedPreferencesManager.KEY_ROUTE_START,true)
                        SharedPreferencesManager.getInstance(context).setStringSharedObject(SharedPreferencesManager.KEY_FREQUENT_TIME, it.GPS_FREQUENT)
                    }
                    _startResult.postValue(Event(ResultImp(ResultType.SUCCESS)))
                }else{
                    _startResult.postValue(Event(ResultImp(ResultType.FAILURE)))
                }
            }else{
                try {
                    val errorBody = result.errorBody()?.string()
                    if (errorBody != null) {
                        val jObjError = JSONObject(errorBody)
                        val msg = jObjError.getString("message")
                        _startResult.postValue(Event(ResultImp(ResultType.FAILURE,msg)))
                    } else{
                        _startResult.postValue(Event(ResultImp(ResultType.FAILURE)))
                    }

                } catch (e: Exception) {
                    _startResult.postValue(Event(ResultImp(ResultType.FAILURE)))
                }
            }
        }else{
            _startResult.value=Event(ResultImp(ResultType.FAILURE, "Network Error Failure"))
        }
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun endTrip()= viewModelScope.launch {
        if(Utils.isSafeForAPICall(context)){
            _endResult.postValue(Event(ResultImp(ResultType.PENDING)))
            val startTripId = StartTripId(coordinates.value,locationName.value,data.value?._id)
            val result = RestClient.getInstance().endTrip(startTripId)
            if(result.isSuccessful){
                if(result.body()!=null){
                    _endResult.postValue(Event(ResultImp(ResultType.SUCCESS,result.body()?.message)))
                }else{
                    _endResult.postValue(Event(ResultImp(ResultType.FAILURE)))
                }
            }else{
                try {
                    val errorBody = result.errorBody()?.string()
                    if (errorBody != null) {
                        val jObjError = JSONObject(errorBody)
                        val msg = jObjError.getString("message")
                        _endResult.postValue(Event(ResultImp(ResultType.FAILURE,msg)))
                    } else{
                        _endResult.postValue(Event(ResultImp(ResultType.FAILURE)))
                    }

                } catch (e: Exception) {
                    _endResult.postValue(Event(ResultImp(ResultType.FAILURE)))
                }
            }
        }else{
            _endResult.value=Event(ResultImp(ResultType.FAILURE, "Network Error Failure"))
        }
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun terminateTrip()= viewModelScope.launch {
        if(Utils.isSafeForAPICall(context)){
            _terminateResult.postValue(Event(ResultImp(ResultType.PENDING)))
            val startTripId = StartTripId(coordinates.value,locationName.value,data.value?._id)
            val result = RestClient.getInstance().terminateTrip(startTripId)
            if(result.isSuccessful){
                if(result.body()!=null){
                    _terminateResult.postValue(Event(ResultImp(ResultType.SUCCESS,result.body()?.message)))
                }else{
                    _terminateResult.postValue(Event(ResultImp(ResultType.FAILURE)))
                }
            }else{
                try {
                    val errorBody = result.errorBody()?.string()
                    if (errorBody != null) {
                        val jObjError = JSONObject(errorBody)
                        val msg = jObjError.getString("message")
                        _terminateResult.postValue(Event(ResultImp(ResultType.FAILURE,msg)))
                    } else{
                        _terminateResult.postValue(Event(ResultImp(ResultType.FAILURE)))
                    }

                } catch (e: Exception) {
                    _terminateResult.postValue(Event(ResultImp(ResultType.FAILURE)))
                }
            }
        }else{
            _terminateResult.value=Event(ResultImp(ResultType.FAILURE, "Network Error Failure"))
        }
    }
}