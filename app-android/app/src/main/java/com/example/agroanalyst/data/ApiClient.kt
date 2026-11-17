package com.example.agroanalyst.data

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Duas instâncias independentes: o JWT só vai para a API própria (instância `agro`),
 * nunca para o meteoserver de terceiros. Timeouts longos por causa do cold start do Render.
 */
object ApiClient {
    // Local: "http://10.0.2.2:3000/" (exige usesCleartextTraffic no Manifest).
    const val BASE_URL = "https://agroanalyst-api.onrender.com/"
    const val BASE_URL_METEO = "https://meteoserver-stfv.onrender.com/"

    // serializeNulls: fotoId = null precisa chegar ao servidor ("remover a foto").
    private val gson = GsonBuilder().serializeNulls().create()

    private fun http(comToken: Boolean): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .apply {
            if (comToken) addInterceptor { cadeia ->
                // Token lido a cada chamada, não capturado na construção.
                val token = Sessao.token
                val req = if (token == null) cadeia.request()
                else cadeia.request().newBuilder().header("Authorization", "Bearer $token").build()
                cadeia.proceed(req)
            }
        }
        .build()

    private fun retrofit(url: String, comToken: Boolean) = Retrofit.Builder()
        .baseUrl(url)
        .client(http(comToken))
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val agro: ApiAgro by lazy { retrofit(BASE_URL, true).create(ApiAgro::class.java) }
    val meteo: ApiMeteo by lazy { retrofit(BASE_URL_METEO, false).create(ApiMeteo::class.java) }
}

fun urlFoto(fileId: String) = "${ApiClient.BASE_URL}upload/$fileId"
