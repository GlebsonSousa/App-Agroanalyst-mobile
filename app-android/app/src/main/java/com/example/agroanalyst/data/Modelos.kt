package com.example.agroanalyst.data

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class SoloResposta(
    @SerializedName("solo_local") val soloLocal: SoloLocalDto?,
    @SerializedName("solo_preciso") val soloPreciso: SoloPrecisoDto?,
    val chuva: ChuvaDto?,
    val recomendacoes: List<RecomendacaoDto>?,
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

data class PrecoDto(
    val cultura: String?, val valor: Double?, val unidade: String?, val variacaoDia: Double?,
    val data: String?, val fonte: String?, val stale: Boolean?
)

data class SeriePontoDto(val data: String?, val valor: Double?)

data class ProducaoUfDto(
    val uf: String?, val codigoIbge: String?, val valorProducao: Double?, val quantidade: Double?,
    val area: Double?, val rendimento: Double?, val ano: Int?
)

data class TopMunicipioDto(
    val municipio: String?, val uf: String?, val valorProducao: Double?, val quantidade: Double?, val ano: Int?
)

data class ClimaDiaDto(val data: String?, val max: Double?, val min: Double?, val chuva: Double?)
data class ClimaDto(val hoje: ClimaDiaDto?, val proximos: List<ClimaDiaDto>?)

data class PracaDto(val nome: String?, val tipo: String?, val uf: String?, val lat: Double?, val lon: Double?)

data class SementeDto(
    val geradoEm: String?,
    val precos: List<PrecoDto>?,
    val series: Map<String, List<SeriePontoDto>>?,
    val producao: Map<String, List<ProducaoUfDto>>?,
    val top: Map<String, List<TopMunicipioDto>>?
)

data class RegistroEnvio(val nome: String, val email: String, val senha: String)
data class LoginEnvio(val email: String, val senha: String)
data class PlantioEnvio(
    val nome: String, val cultura: String, val areaHa: Double,
    val produtividadeSacasHa: Double, val lat: Double, val lon: Double,
    val fotoId: String?
)
data class FreteEnvio(
    val origemLat: Double, val origemLon: Double, val destino: String, val grao: String,
    val sacas: Int, val precoSaca: Double, val modal: String,
    val capacidadeT: Double?, val kmPorLitro: Double?, val eixos: Int?
)

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
data class FreteDto(
    val distanciaKm: Double?, val duracaoH: Double?, val viagens: Int?,
    val freteTotal: Double?, val receitaTotal: Double?, val margem: Double?,
    val fretePorSaca: Double?, val liquidoSaca: Double?
)
data class FreteSalvoDto(
    val id: String?, val destino: String?, val grao: String?, val sacas: Int?,
    val precoSaca: Double?, val modal: String?, val resultado: FreteDto?,
    @SerializedName("criado_em") val criadoEm: String?
)
