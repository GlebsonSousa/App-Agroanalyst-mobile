package com.example.agroanalyst.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.agroanalyst.data.FreteDto
import com.example.agroanalyst.data.FreteEnvio
import com.example.agroanalyst.data.PracaDto
import com.example.agroanalyst.data.Repositorio

data class FreteRascunho(
    val plantioId: String = "",
    val origemLat: String = "",
    val origemLon: String = "",
    val destino: String = "",
    val grao: String = "soja",
    val sacas: String = "",
    val preco: String = "",
    val modal: String = "rodoviario",
    val capacidade: String = "30",
    val kmPorLitro: String = "2.5",
    val eixos: String = "6"
)

class FreteViewModel : OperacaoViewModel() {
    var pracas by mutableStateOf<List<PracaDto>>(emptyList())
        private set
    var resultado by mutableStateOf<FreteDto?>(null)
        private set
    var rascunho by mutableStateOf(FreteRascunho())
        private set
    var salvo by mutableStateOf(false)
        private set

    fun atualizarRascunho(novo: FreteRascunho) { rascunho = novo }

    fun preparar(origemLat: Double?, origemLon: Double?, destino: String?, plantioId: String?) {
        rascunho = FreteRascunho(
            plantioId = plantioId.orEmpty(), origemLat = origemLat?.toString().orEmpty(),
            origemLon = origemLon?.toString().orEmpty(), destino = destino.orEmpty()
        )
        resultado = null
        salvo = false
    }

    fun carregarPracas() = executar { pracas = Repositorio.pracas() }

    private fun montar(): FreteEnvio? {
        val r = rascunho
        fun d(t: String) = t.trim().replace(',', '.').toDoubleOrNull()
        val lat = d(r.origemLat); val lon = d(r.origemLon)
        val sacas = r.sacas.trim().toIntOrNull(); val preco = d(r.preco)
        val rodoviario = r.modal == "rodoviario"
        val cap = d(r.capacidade); val km = d(r.kmPorLitro); val eixos = r.eixos.trim().toIntOrNull()
        val erro = when {
            lat == null || lon == null || lat !in -90.0..90.0 || lon !in -180.0..180.0 -> "Informe a coordenada de origem."
            r.destino.isEmpty() -> "Escolha o destino."
            sacas == null || sacas <= 0 || preco == null || preco <= 0 -> "Informe sacas e preço maiores que zero."
            rodoviario && (cap == null || cap <= 0 || km == null || km <= 0 || eixos == null || eixos <= 0) -> "Informe capacidade, consumo e eixos do caminhão."
            else -> null
        }
        if (erro != null) { mensagem = erro; return null }
        return FreteEnvio(
            lat!!, lon!!, r.destino, r.grao, sacas!!, preco!!, r.modal,
            if (rodoviario) cap else null, if (rodoviario) km else null, if (rodoviario) eixos else null
        )
    }

    fun calcular() {
        val envio = montar() ?: return
        executar { resultado = Repositorio.calcularFrete(envio); salvo = false }
    }

    fun salvar() {
        val envio = montar() ?: return
        if (rascunho.plantioId.isEmpty()) { mensagem = "Escolha o plantio para salvar."; return }
        executar { Repositorio.salvarFrete(rascunho.plantioId, envio); salvo = true }
    }

    override fun limpar() { super.limpar(); resultado = null; rascunho = FreteRascunho(); salvo = false }
}
