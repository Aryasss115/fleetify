package com.oedinn.fleetify.Model

import okhttp3.MultipartBody

data class Report(
    val reportId: String,
    val vehicleId: String,
    val vehicleLicenseNumber: String,
    val vehicleName: String,
    val note: String,
    val photo: String,
    val createdAt: String,
    val createdBy: String,
    val reportStatus: String
)

// Vehicle data class
data class Vehicle(
    val vehicleId: String,
    val licenseNumber: String,
    val type: String
)

data class LaporanData(
    val vehicleId: String,
    val note: String,
    val userId: String,
    val photo: MultipartBody.Part
)

