package com.example.agroanalyst.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.agroanalyst.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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
    var locais by mutableStateOf<List<PracaDto>>(emptyList())
        private set
    var linhas by mutableStateOf<List<LinhaLogistica>?>(null)
        private set
    var ufSelecionada by mutableStateOf<ProducaoUfDto?>(null)
        private set
    var origem by mutableStateOf<PracaDto?>(null)
        private set
    var destino by mutableStateOf<PracaDto?>(null)
        private set
    var modal by mutableStateOf("rodoviario")
        private set
    var rota by mutableStateOf<RotaDto?>(null)
        private set
    var avisoRota by mutableStateOf<String?>(null)
        private set
    var calculandoRota by mutableStateOf(false)
        private set

    private var iniciou = false
    private var tarefaRota: Job? = null

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

    fun carregarLocais(logado: Boolean) {
        if (!logado) { locais = emptyList(); return }
        viewModelScope.launch {
            try {
                locais = Repositorio.locais().map { PracaDto(it.nome, "local", it.uf, it.lat, it.lon) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: ApiException) { }
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
            origem == null || destino != null -> { origem = p; destino = null; limparRotaCalculada() }
            origem?.nome == p.nome -> {}
            else -> { destino = p; carregarRota() }
        }
    }

    fun definirRota(o: PracaDto, d: PracaDto, novoModal: String) {
        logisticaLigada = true
        origem = o; destino = d; modal = novoModal
        carregarRota()
    }

    fun mudarModal(novo: String) {
        modal = novo
        if (origem != null && destino != null) carregarRota()
    }

    fun limparRota() { origem = null; destino = null; limparRotaCalculada() }

    private fun limparRotaCalculada() { tarefaRota?.cancel(); rota = null; avisoRota = null; calculandoRota = false }

    private fun carregarRota() {
        val o = origem ?: return
        val d = destino ?: return
        val m = modal
        tarefaRota?.cancel()
        rota = null
        avisoRota = null
        calculandoRota = true
        tarefaRota = viewModelScope.launch {
            try {
                val r = Repositorio.rota(o.lat ?: 0.0, o.lon ?: 0.0, d.lat ?: 0.0, d.lon ?: 0.0, m)
                if (origem === o && destino === d && modal == m) rota = r
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                if (origem === o && destino === d && modal == m) avisoRota = e.message
            } finally {
                if (origem === o && destino === d && modal == m) calculandoRota = false
            }
        }
    }

    private suspend fun carregarProducao(c: String) {
        Repositorio.semente()?.producao?.get(c)?.let { if (cultura == c) producao = it }
        try {
            val novos = Repositorio.producao(c)
            if (cultura == c) producao = novos
        } catch (e: CancellationException) {
            throw e
        } catch (_: ApiException) { }
    }

    override fun limpar() { super.limpar(); locais = emptyList() }
}
