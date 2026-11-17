package com.example.agroanalyst.ui

import com.example.agroanalyst.data.SoloResposta
import java.util.Locale

private val BR = Locale("pt", "BR")

fun Double.formatar(casas: Int = 1): String = String.format(BR, "%,.${casas}f", this)
fun Double.reais(): String = "R$ " + formatar(2)
fun Double.coordenada(): String = String.format(BR, "%.4f", this).replace('-', '−')

/** pH do ponto: o preciso vem como texto e "0.0" não é medida válida; cai para o do mapa de solos. */
fun SoloResposta.phExibido(): Double? =
    soloPreciso?.phPreciso?.toDoubleOrNull()?.takeIf { it > 0.0 } ?: soloLocal?.ph

/** "argilosa  ou muito argilosa" vem com espaço duplo. */
fun String?.limpo(): String =
    this?.trim()?.replace(Regex("\\s+"), " ")?.replaceFirstChar { it.uppercase() } ?: "—"

fun String.humanizar(): String = replace('_', ' ').replaceFirstChar { it.uppercase() }
