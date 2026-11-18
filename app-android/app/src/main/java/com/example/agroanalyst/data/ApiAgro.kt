package com.example.agroanalyst.data

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiAgro {
    @GET("/") suspend fun ping(): Response<ResponseBody>

    @GET("publico/precos") suspend fun precos(): Response<List<PrecoDto>>
    @GET("publico/precos/{cultura}/serie")
    suspend fun serie(@Path("cultura") cultura: String, @Query("dias") dias: Int): Response<List<SeriePontoDto>>
    @GET("publico/producao")
    suspend fun producao(@Query("cultura") cultura: String, @Query("nivel") nivel: String): Response<List<ProducaoUfDto>>
    @GET("publico/producao/top")
    suspend fun top(@Query("cultura") cultura: String, @Query("n") n: Int): Response<List<TopMunicipioDto>>
    @GET("publico/clima") suspend fun clima(@Query("lat") lat: Double, @Query("lon") lon: Double): Response<ClimaDto>
    @GET("publico/pracas") suspend fun pracas(): Response<List<PracaDto>>
    @GET("publico/cidades") suspend fun cidades(@Query("q") q: String): Response<List<CidadeDto>>
    @GET("publico/rota")
    suspend fun rota(
        @Query("origemLat") origemLat: Double, @Query("origemLon") origemLon: Double,
        @Query("destinoLat") destinoLat: Double, @Query("destinoLon") destinoLon: Double,
        @Query("modal") modal: String
    ): Response<RotaDto>
    @GET("locais") suspend fun locais(): Response<List<LocalDto>>
    @POST("locais") suspend fun criarLocal(@Body corpo: LocalEnvio): Response<LocalDto>
    @DELETE("locais/{id}") suspend fun excluirLocal(@Path("id") id: String): Response<MensagemResposta>
    @POST("publico/frete/calcular") suspend fun calcularFrete(@Body corpo: FreteEnvio): Response<FreteDto>

    @POST("usuarios/registrar") suspend fun registrar(@Body corpo: RegistroEnvio): Response<MensagemResposta>
    @POST("usuarios/login") suspend fun login(@Body corpo: LoginEnvio): Response<LoginResposta>

    @GET("plantios") suspend fun plantios(): Response<List<PlantioDto>>
    @POST("plantios") suspend fun criarPlantio(@Body corpo: PlantioEnvio): Response<PlantioDto>
    @GET("plantios/{id}") suspend fun plantio(@Path("id") id: String): Response<PlantioDto>
    @PUT("plantios/{id}") suspend fun atualizarPlantio(@Path("id") id: String, @Body corpo: PlantioEnvio): Response<PlantioDto>
    @DELETE("plantios/{id}") suspend fun excluirPlantio(@Path("id") id: String): Response<MensagemResposta>
    @GET("plantios/{id}/fretes") suspend fun fretes(@Path("id") id: String): Response<List<FreteSalvoDto>>
    @POST("plantios/{id}/fretes") suspend fun salvarFrete(@Path("id") id: String, @Body corpo: FreteEnvio): Response<FreteSalvoDto>

    @Multipart
    @POST("upload")
    suspend fun enviarFoto(@Part foto: MultipartBody.Part): Response<UploadResposta>
}
