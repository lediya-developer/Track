package com.lediya.trackingapp.data.model

data class TripDetails(
        val data: Data,
        val message: String
)

data class Data(
        val _id: String,
        val actualEndDateTime: Any,
        val actualStartDateTime: Any,
        val actualTimeDiff: Any,
        val assignedAdmin: AssignedAdmin,
        val assignedCarrier: AssignedCarrier,
        val createdAt: String,
        val delay: Int,
        val destination: String,
        val distance: Int,
        val driverPhone: Long,
        val expectedEndDateTime: Int,
        val expectedStartDateTime: Int,
        val expectedTimeDiff: Int,
        val name: String,
        val otp: Int,
        val routeStatus: Int,
        val source: String,
        val status: Int,
        val tripId: Int,
        val truckNo: String,
        val updatedAt: String
)

data class AssignedAdmin(
        val _id: String,
        val email: String,
        val name: String
)

data class AssignedCarrier(
        val _id: String,
        val email: String,
        val name: String
)