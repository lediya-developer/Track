package com.lediya.trackingapp.data.model

data class StartTripResponse(
    val data: StartData,
    val message: String
)

data class StartData(
    val GPS_FREQUENT: String
)