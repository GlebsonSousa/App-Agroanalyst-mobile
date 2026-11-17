package com.example.agroanalyst.ui

import com.example.agroanalyst.data.SoloResposta
import java.util.Locale

private val BR = Locale("pt", "BR")

fun Double.formatar(casas: Int = 1): String = String.format(BR, "%,.${casas}f", this)
fun Double.reais(casas: Int = 2): String = "R$ " + formatar(casas)
fun Double.coordenada(): String = String.format(BR, "%.4f", this).replace('-', '−')
fun Double.variacao(): String = (if (this > 0) "+" else "") + formatar(2) + "%"

fun SoloResposta.phExibido(): Double? =
    soloPreciso?.phPreciso?.toDoubleOrNull()?.takeIf { it > 0.0 } ?: soloLocal?.ph

fun String?.limpo(): String =
    this?.trim()?.replace(Regex("\\s+"), " ")?.replaceFirstChar { it.uppercase() } ?: "—"

fun String.humanizar(): String = replace('_', ' ').replaceFirstChar { it.uppercase() }

fun String.dataCurta(): String {
    val p = split("-")
    return if (p.size == 3) "${p[2]}/${p[1]}" else this
}

fun cortesProducao(valores: List<Double>): List<Double> {
    val v = valores.sorted()
    if (v.isEmpty()) return listOf(0.0, 0.0, 0.0, 0.0)
    return listOf(0.2, 0.4, 0.6, 0.8).map { v[((v.size - 1) * it).toInt()] }
}

fun faixaDe(valor: Double?, cortes: List<Double>): Int {
    if (valor == null) return -1
    return cortes.count { valor > it }
}

fun rotulosFaixas(cortes: List<Double>, maximo: Double): List<String> {
    fun mi(v: Double) = (v / 1000).formatar(0)
    val pontos = listOf(0.0) + cortes + listOf(maximo)
    return (0 until 5).map { "R$ ${mi(pontos[it])} – ${mi(pontos[it + 1])} mi" }
}

fun String.diaSemana(): String = try {
    val d = java.text.SimpleDateFormat("yyyy-MM-dd", Locale("pt", "BR")).parse(this)
    java.text.SimpleDateFormat("EEE", Locale("pt", "BR")).format(d!!).replace(".", "")
} catch (e: Exception) { this }
