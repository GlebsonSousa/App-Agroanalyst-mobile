package com.example.agroanalyst

import kotlinx.serialization.Serializable

@Serializable object Inicio
@Serializable object Mercado
@Serializable data class CulturaDetalhe(val nome: String)
@Serializable object Mapa
@Serializable object Analise
@Serializable data class AnaliseResultado(val lat: Double, val lon: Double)
@Serializable object Frete
@Serializable object Login
@Serializable object Cadastro
@Serializable object Plantios
@Serializable object PlantioNovo
@Serializable data class PlantioForm(val id: String)
@Serializable data class PlantioDetalhe(val id: String)
