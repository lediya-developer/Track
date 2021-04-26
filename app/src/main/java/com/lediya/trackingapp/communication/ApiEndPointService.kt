package com.lediya.trackingapp.communication

import com.lediya.trackingapp.data.model.*
import retrofit2.Response

import retrofit2.http.*

interface ApiEndPointService {

    @PUT("validateTruckNo")
    suspend fun validateTruckNumber(@Body truckDetails:TripIdRequest): Response<RouteId>
    @PUT("validateOTP")
    suspend fun validateOtp(@Body truckDetails:TripDetailRequest): Response<TripDetails>
    @PUT("start")
    suspend fun startTrip(@Body tripId:StartTripId): Response<StartTripResponse>
    @PUT("updateMilestone")
    suspend fun updateLocation(@Body tripId:StartTripId): Response<StartTripResponse>
    @PUT("end")
    suspend fun endTrip(@Body tripId:StartTripId): Response<StartTripResponse>
    @PUT("terminate")
    suspend fun terminateTrip(@Body tripId:StartTripId): Response<StartTripResponse>
}