package com.example.agroanalyst.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.agroanalyst.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class MapaViewModel : OperacaoViewModel() {
    var producaoLigada by mutableStateOf(true)
        private set
    var logisticaLigada by mutableStateOf(false)
        private set
    var cultura by mutableStateOf("soja")
        private set
    var producao by mutableStateOf<List<ProducaoUfDto>>(emptyList())
        private set
    var ufs by mutableStateOf<List<UfPoligono>>(emptyList())
        private set
    var pracas by mutableStateOf<List<PracaDto>>(emptyList())
        private set
    var linhas by mutableStateOf<List<LinhaLogistica>?>(null)
        private set
    var ufSelecionada by mutableStateOf<ProducaoUfDto?>(null)
        private set
    var origem by mutableStateOf<PracaDto?>(null)
        private set
    var destino by mutableStateOf<PracaDto?>(null)
        private set
    var rotuloDados by mutableStateOf("")
        private set

    private var iniciou = false

    fun iniciar() {
        if (iniciou) return
        iniciou = true
        viewModelScope.launch {
            ufs = Repositorio.ufs()
            linhas = Repositorio.linhasLogistica()
            carregarProducao(cultura)
            executar { pracas = Repositorio.pracas() }
        }
    }

    fun alternarProducao() { producaoLigada = !producaoLigada }
    fun alternarLogistica() { logisticaLigada = !logisticaLigada }

    fun selecionarCultura(nova: String) {
        cultura = nova
        ufSelecionada = null
        viewModelScope.launch { carregarProducao(nova) }
    }

    fun tocarUf(codigo: String?) { ufSelecionada = producao.firstOrNull { it.codigoIbge == codigo } }
    fun fecharUf() { ufSelecionada = null }

    fun tocarPraca(p: PracaDto) {
        when {
            origem == null || destino != null -> { origem = p; destino = null }
            origem?.nome == p.nome -> {}
            else -> destino = p
        }
    }

    fun limparRota() { origem = null; destino = null }

    fun distanciaEstimadaKm(): Double? {
        val o = origem ?: return null
        val d = destino ?: return null
        val rad = Math.PI / 180
        val h = sin(((d.lat ?: 0.0) - (o.lat ?: 0.0)) * rad / 2).pow(2) +
            cos((o.lat ?: 0.0) * rad) * cos((d.lat ?: 0.0) * rad) * sin(((d.lon ?: 0.0) - (o.lon ?: 0.0)) * rad / 2).pow(2)
        return 6371 * 2 * asin(sqrt(h)) * 1.3
    }

    private suspend fun carregarProducao(c: String) {
        Repositorio.semente()?.producao?.get(c)?.let { if (cultura == c) { producao = it; rotuloDados = "dados salvos" } }
        try {
            val novos = Repositorio.producao(c)
            if (cultura == c) { producao = novos; rotuloDados = "IBGE/SIDRA" }
        } catch (e: CancellationException) {
            throw e
        } catch (_: ApiException) { }
    }

    override fun limpar() { super.limpar(); origem = null; destino = null; ufSelecionada = null }
}
