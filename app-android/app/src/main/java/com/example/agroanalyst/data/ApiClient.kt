package com.example.agroanalyst.data

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    const val BASE_URL = "https://app-agroanalyst-mobile.onrender.com/"
    const val BASE_URL_METEO = "https://meteoserver-stfv.onrender.com/"

    private val gson = GsonBuilder().serializeNulls().create()

    private fun http(comToken: Boolean, leitura: Long = 60): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(leitura, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .apply {
            if (comToken) addInterceptor { cadeia ->
                val token = Sessao.token
                val pedido = if (token == null) cadeia.request()
                else cadeia.request().newBuilder().header("Authorization", "Bearer $token").build()
                cadeia.proceed(pedido)
            }
        }
        .build()

    private fun retrofit(url: String, comToken: Boolean, leitura: Long = 60) = Retrofit.Builder()
        .baseUrl(url)
        .client(http(comToken, leitura))
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val agro: ApiAgro by lazy { retrofit(BASE_URL, true).create(ApiAgro::class.java) }
    val meteo: ApiMeteo by lazy { retrofit(BASE_URL_METEO, false, 100).create(ApiMeteo::class.java) }

    fun urlFoto(fileId: String) = "${BASE_URL}upload/$fileId"
}
