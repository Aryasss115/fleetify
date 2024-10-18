package com.oedinn.fleetify.API

import com.oedinn.fleetify.Model.LaporanData
import com.oedinn.fleetify.Model.Report
import com.oedinn.fleetify.Model.Vehicle
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

    interface ApiService {
        @GET("read_all_laporan")
        suspend fun getAllReports(
            @Query("userId") userId: String
        ): List<Report>

        @GET("list_vehicle")
        suspend fun getVehicleList(): List<Vehicle>

        @POST("add_laporan")
        suspend fun uploadLaporan(
            @Body laporanData: LaporanData
        ): Response<Unit>
    }