package com.example.agroanalyst.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.agroanalyst.data.Repositorio
import com.example.agroanalyst.data.SoloResposta

class AnaliseViewModel : OperacaoViewModel() {
    var resultado by mutableStateOf<SoloResposta?>(null)
        private set
    private var chave: String? = null

    fun coordenadas(latTxt: String, lonTxt: String): Pair<Double, Double>? {
        val lat = latTxt.trim().replace(',', '.').toDoubleOrNull()
        val lon = lonTxt.trim().replace(',', '.').toDoubleOrNull()
        if (lat == null || lon == null || lat !in -90.0..90.0 || lon !in -180.0..180.0) {
            mensagem = "Coordenada inválida: latitude entre -90 e 90, longitude entre -180 e 180."
            return null
        }
        mensagem = null
        return lat to lon
    }

    fun buscar(lat: Double, lon: Double) {
        val nova = "$lat,$lon"
        if (nova == chave && resultado != null) return
        resultado = null
        executar(substituir = true) {
            resultado = Repositorio.solo(lat, lon)
            chave = nova
        }
    }

    fun tentarDeNovo(lat: Double, lon: Double) { chave = null; buscar(lat, lon) }

    override fun limpar() { super.limpar(); resultado = null; chave = null }
}
