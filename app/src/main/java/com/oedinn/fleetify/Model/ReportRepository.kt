package com.oedinn.fleetify.Model

import com.oedinn.fleetify.API.ApiConfig
import retrofit2.Response

class ReportRepository {

    private val api = ApiConfig.getApiService()

    suspend fun getAllReports(userId: String): List<Report> {
        return api.getAllReports(userId)
    }

    suspend fun getVehicleList(): List<Vehicle> {
        return api.getVehicleList()
    }

    suspend fun uploadLaporan(laporanData: LaporanData): Response<Unit> {
        return api.uploadLaporan(laporanData)
    }
}