package com.example.agroanalyst.data

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiAgro {
    @GET("/")
    suspend fun ping(): Response<okhttp3.ResponseBody>

    @POST("usuarios/registrar") suspend fun registrar(@Body corpo: RegistroEnvio): Response<MensagemResposta>
    @POST("usuarios/login") suspend fun login(@Body corpo: LoginEnvio): Response<LoginResposta>

    @GET("plantios") suspend fun plantios(): Response<List<PlantioDto>>
    @POST("plantios") suspend fun criarPlantio(@Body corpo: PlantioEnvio): Response<PlantioDto>
    @GET("plantios/{id}") suspend fun plantio(@Path("id") id: String): Response<PlantioDto>
    @PUT("plantios/{id}") suspend fun atualizarPlantio(@Path("id") id: String, @Body corpo: PlantioEnvio): Response<PlantioDto>
    @DELETE("plantios/{id}") suspend fun excluirPlantio(@Path("id") id: String): Response<MensagemResposta>

    // A parte multipart PRECISA se chamar "foto".
    @Multipart
    @POST("upload")
    suspend fun enviarFoto(@Part foto: MultipartBody.Part): Response<UploadResposta>

    @GET("destinos") suspend fun destinos(): Response<List<DestinoDto>>
    @GET("graos") suspend fun graos(): Response<List<GraoDto>>
    @POST("frete/calcular") suspend fun calcularFrete(@Body corpo: FreteEnvio): Response<FreteDto>

    @GET("vendas") suspend fun vendas(): Response<List<VendaDto>>
    @POST("vendas") suspend fun criarVenda(@Body corpo: VendaEnvio): Response<VendaDto>
    @GET("vendas/{id}") suspend fun venda(@Path("id") id: String): Response<VendaDto>
    @PATCH("vendas/{id}/status") suspend fun mudarStatus(@Path("id") id: String, @Body corpo: StatusEnvio): Response<VendaDto>
}
