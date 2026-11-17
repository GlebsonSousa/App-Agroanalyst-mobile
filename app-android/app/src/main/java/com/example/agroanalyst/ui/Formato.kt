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
