package com.example.agroanalyst.data

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiMeteo {
    @GET("solo")
    suspend fun solo(@Query("lat") lat: Double, @Query("lon") lon: Double): Response<SoloResposta>

    @GET("/")
    suspend fun ping(): Response<ResponseBody>
}
