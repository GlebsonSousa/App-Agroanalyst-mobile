package com.example.agroanalyst.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.agroanalyst.data.FreteDto
import com.example.agroanalyst.data.FreteEnvio
import com.example.agroanalyst.data.LocalDto
import com.example.agroanalyst.data.LocalEnvio
import com.example.agroanalyst.data.PracaDto
import com.example.agroanalyst.data.Repositorio

data class FreteRascunho(
    val plantioId: String = "",
    val origemLat: String = "",
    val origemLon: String = "",
    val origemNome: String = "",
    val destinoLat: String = "",
    val destinoLon: String = "",
    val destinoNome: String = "",
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
    var locais by mutableStateOf<List<LocalDto>>(emptyList())
        private set
    var resultado by mutableStateOf<FreteDto?>(null)
        private set
    var rascunho by mutableStateOf(FreteRascunho())
        private set
    var salvo by mutableStateOf(false)
        private set
    var avisoLocal by mutableStateOf<String?>(null)
        private set

    fun atualizarRascunho(novo: FreteRascunho) { rascunho = novo }

    fun preparar(
        origemLat: Double?, origemLon: Double?, origemNome: String?,
        destinoLat: Double?, destinoLon: Double?, destinoNome: String?,
        plantioId: String?, modal: String = "rodoviario"
    ) {
        rascunho = FreteRascunho(
            plantioId = plantioId.orEmpty(),
            origemLat = origemLat?.toString().orEmpty(), origemLon = origemLon?.toString().orEmpty(), origemNome = origemNome.orEmpty(),
            destinoLat = destinoLat?.toString().orEmpty(), destinoLon = destinoLon?.toString().orEmpty(), destinoNome = destinoNome.orEmpty(),
            modal = modal
        )
        resultado = null
        salvo = false
        avisoLocal = null
    }

    fun carregarPracas() = executar { pracas = Repositorio.pracas() }

    fun carregarLocais() = executar { locais = Repositorio.locais() }

    fun salvarLocal(nome: String, lat: Double, lon: Double, tipo: String) = executar {
        val novo = Repositorio.criarLocal(LocalEnvio(nome.trim(), tipo, lat, lon, null, null))
        locais = (locais + novo).sortedBy { it.nome }
        avisoLocal = "Local \"${novo.nome}\" salvo."
    }

    fun excluirLocal(id: String) = executar {
        Repositorio.excluirLocal(id)
        locais = locais.filter { it.id != id }
    }

    private fun montar(): FreteEnvio? {
        val r = rascunho
        fun d(t: String) = t.trim().replace(',', '.').toDoubleOrNull()
        val lat = d(r.origemLat); val lon = d(r.origemLon)
        val dLat = d(r.destinoLat); val dLon = d(r.destinoLon)
        val sacas = r.sacas.trim().toIntOrNull(); val preco = d(r.preco)
        val rodoviario = r.modal == "rodoviario"
        val cap = d(r.capacidade); val km = d(r.kmPorLitro); val eixos = r.eixos.trim().toIntOrNull()
        fun valida(a: Double?, o: Double?) = a != null && o != null && a in -90.0..90.0 && o in -180.0..180.0
        val erro = when {
            !valida(lat, lon) -> "Escolha o local de origem (busque a cidade ou digite as coordenadas)."
            !valida(dLat, dLon) -> "Escolha o local de destino (busque a cidade ou escolha uma praça)."
            sacas == null || sacas <= 0 || preco == null || preco <= 0 -> "Informe sacas e preço maiores que zero."
            rodoviario && (cap == null || cap <= 0 || km == null || km <= 0 || eixos == null || eixos <= 0) -> "Informe capacidade, consumo e eixos do caminhão."
            else -> null
        }
        if (erro != null) { mensagem = erro; return null }
        return FreteEnvio(
            lat!!, lon!!, r.destinoNome.ifBlank { "Destino" }, dLat!!, dLon!!, r.grao, sacas!!, preco!!, r.modal,
            if (rodoviario) cap else null, if (rodoviario) km else null, if (rodoviario) eixos else null
        )
    }

    fun calcular() {
        val envio = montar() ?: return
        executar(substituir = true) { resultado = Repositorio.calcularFrete(envio); salvo = false }
    }

    fun salvar() {
        val envio = montar() ?: return
        if (rascunho.plantioId.isEmpty()) { mensagem = "Escolha o plantio de origem para salvar."; return }
        executar { Repositorio.salvarFrete(rascunho.plantioId, envio); salvo = true }
    }

    override fun limpar() { super.limpar(); resultado = null; rascunho = FreteRascunho(); salvo = false; locais = emptyList(); avisoLocal = null }
}
