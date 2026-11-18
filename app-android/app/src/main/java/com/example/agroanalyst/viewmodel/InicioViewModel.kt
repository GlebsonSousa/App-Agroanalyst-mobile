package com.example.agroanalyst.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.agroanalyst.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val CULTURAS = listOf("soja" to "Soja", "milho" to "Milho", "cafe" to "Café", "algodao" to "Algodão", "trigo" to "Trigo")

fun mesclarPrecos(atuais: List<PrecoDto>, novos: List<PrecoDto>): List<PrecoDto> {
    val porCultura = atuais.associateBy { it.cultura } + novos.associateBy { it.cultura }
    return CULTURAS.mapNotNull { porCultura[it.first] }
}

class InicioViewModel : OperacaoViewModel() {
    var precos by mutableStateOf<List<PrecoDto>>(emptyList())
        private set
    var cultura by mutableStateOf("soja")
        private set
    var series by mutableStateOf<Map<String, List<SeriePontoDto>>>(emptyMap())
        private set
    var producao by mutableStateOf<Map<String, List<ProducaoUfDto>>>(emptyMap())
        private set
    var top by mutableStateOf<Map<String, List<TopMunicipioDto>>>(emptyMap())
        private set
    var clima by mutableStateOf<ClimaDto?>(null)
        private set
    var localClima by mutableStateOf("Mauá, SP")
        private set
    var ufs by mutableStateOf<List<UfPoligono>>(emptyList())
        private set
    var rotuloDados by mutableStateOf("")
        private set

    private var lat = -23.67
    private var lon = -46.46
    private var iniciou = false

    fun iniciar() {
        if (iniciou) return
        iniciou = true
        viewModelScope.launch {
            ufs = Repositorio.ufs()
            Repositorio.semente()?.let { s ->
                precos = s.precos.orEmpty()
                series = s.series.orEmpty()
                producao = s.producao.orEmpty()
                top = s.top.orEmpty()
                rotuloDados = "dados de ${(s.geradoEm ?: "").take(10).dataCurta()}"
            }
            launch { try { Repositorio.aquecer() } catch (e: CancellationException) { throw e } catch (_: Exception) { } }
            atualizar()
        }
    }

    fun selecionarCultura(nova: String) {
        cultura = nova
        viewModelScope.launch {
            try { carregarCultura(nova) } catch (e: CancellationException) { throw e } catch (_: Exception) { }
        }
    }

    fun usarPosicao(novaLat: Double, novaLon: Double) {
        lat = novaLat; lon = novaLon; localClima = "sua posição"
        atualizar()
    }

    fun atualizar() = executar {
        coroutineScope {
            val p = async { opcional { Repositorio.precos() } }
            val cl = async { opcional { Repositorio.clima(lat, lon) } }
            val cu = async { carregarCultura(cultura) }
            val precosNovos = p.await()
            val climaNovo = cl.await()
            val culturaOk = cu.await()
            precosNovos?.takeIf { it.isNotEmpty() }?.let { precos = mesclarPrecos(precos, it) }
            climaNovo?.let { clima = it }
            if (precosNovos == null && climaNovo == null && !culturaOk) mensagem = "Sem conexão com o servidor. Mostrando os últimos dados salvos."
            else rotuloDados = "atualizado às ${SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(Date())}"
        }
    }

    private suspend fun carregarCultura(c: String): Boolean = coroutineScope {
        val pr = async { opcional { Repositorio.producao(c) } }
        val tp = async { opcional { Repositorio.top(c) } }
        val sr = async { opcional { Repositorio.serie(c) } }
        val prod = pr.await()
        val topo = tp.await()
        val serie = sr.await()
        prod?.let { producao = producao + (c to it) }
        topo?.let { top = top + (c to it) }
        serie?.takeIf { it.isNotEmpty() }?.let { series = series + (c to it) }
        prod != null || topo != null || serie != null
    }

    private suspend fun <T> opcional(bloco: suspend () -> T): T? = try {
        bloco()
    } catch (e: CancellationException) {
        throw e
    } catch (e: ApiException) {
        null
    }

    private fun String.dataCurta(): String {
        val p = split("-")
        return if (p.size == 3) "${p[2]}/${p[1]}" else this
    }
}
