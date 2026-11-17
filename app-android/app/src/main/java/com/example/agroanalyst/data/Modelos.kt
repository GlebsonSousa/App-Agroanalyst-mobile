package com.example.agroanalyst.data

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

// ===== meteoserver (resposta: tudo anulável) =====
data class SoloResposta(
    @SerializedName("solo_local") val soloLocal: SoloLocalDto?,
    @SerializedName("solo_preciso") val soloPreciso: SoloPrecisoDto?,
    val chuva: ChuvaDto?,
    val recomendacoes: List<RecomendacaoDto>?,
    // Esquema variável por cultura ("argila_range_percent" x "argila_range_mm"): cada item é lido como objeto genérico.
    @SerializedName("culturas_db") val culturasDb: Map<String, JsonObject>?,
    @SerializedName("metodo_busca") val metodoBusca: String?
)

data class SoloLocalDto(
    @SerializedName("tipo_solo") val tipoSolo: String?,
    val textura: String?,
    val drenagem: String?,
    val ph: Double?,
    val fertilidade: String?
)

// Números chegam como texto ("0.0"): convertidos na apresentação.
data class SoloPrecisoDto(
    @SerializedName("ph_preciso") val phPreciso: String?,
    @SerializedName("materia_organica_percent") val materiaOrganica: String?,
    @SerializedName("argila_percent") val argila: String?
)

data class ChuvaDto(
    @SerializedName("cidade_proxima") val cidadeProxima: String?,
    val estado: String?,
    @SerializedName("soma_chuva_mensal") val somaChuvaMensal: List<MesChuvaDto>?,
    @SerializedName("chuva_total_anual_mm") val chuvaTotalAnualMm: Double?
)

data class MesChuvaDto(
    val mes: Int?,
    @SerializedName("nome_mes") val nomeMes: String?,
    @SerializedName("ano_mes") val anoMes: String?,
    @SerializedName("soma_mm") val somaMm: Double?
)

data class RecomendacaoDto(val nome: String?, val score: Int?, val categoria: String?)

// ===== API AgroAnalyst Mobile: envio (não anulável) =====
data class RegistroEnvio(val nome: String, val email: String, val senha: String)
data class LoginEnvio(val email: String, val senha: String)
data class PlantioEnvio(
    val nome: String, val cultura: String, val areaHa: Double,
    val produtividadeSacasHa: Double, val lat: Double, val lon: Double,
    val fotoId: String?
)
data class FreteEnvio(
    val plantioId: String, val destinoId: String, val sacas: Int,
    val precoSaca: Double, val modal: String, val capacidadeT: Double,
    val kmPorLitro: Double?, val eixos: Int?, val prioridade: Int
)
typealias VendaEnvio = FreteEnvio
data class StatusEnvio(val status: String)

// ===== resposta: TODOS anuláveis =====
data class MensagemResposta(val mensagem: String?)
data class LoginResposta(val token: String?, val nome: String?)
data class UploadResposta(val fileId: String?)
data class PlantioDto(
    val id: String?, val nome: String?, val cultura: String?,
    val areaHa: Double?, val produtividadeSacasHa: Double?,
    val sacasEstimadas: Double?, val lat: Double?, val lon: Double?,
    val fotoId: String?,
    @SerializedName("criado_em") val criadoEm: String?
)
data class DestinoDto(
    val id: String?, val nome: String?, val tipo: String?, val cidade: String?, val uf: String?,
    val lat: Double?, val lon: Double?, val precoSaca: Double?, val grao: String?
)
data class GraoDto(val id: String?, val nome: String?, val kgPorSaca: Double?)
data class FreteDto(
    val distanciaKm: Double?, val duracaoH: Double?, val viagens: Int?,
    val freteTotal: Double?, val receitaTotal: Double?, val margem: Double?,
    val fretePorSaca: Double?, val liquidoSaca: Double?
)
data class VendaDto(
    val id: String?, val plantioId: String?, val destinoId: String?, val destinoNome: String?,
    val destinoLat: Double?, val destinoLon: Double?, val sacas: Int?, val precoSaca: Double?,
    val modal: String?, val distanciaKm: Double?, val duracaoH: Double?, val viagens: Int?,
    val freteTotal: Double?, val receitaTotal: Double?, val margem: Double?, val status: String?,
    @SerializedName("criado_em") val criadoEm: String?
)
