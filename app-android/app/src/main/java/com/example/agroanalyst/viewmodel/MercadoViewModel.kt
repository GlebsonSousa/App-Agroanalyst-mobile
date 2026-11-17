package com.example.agroanalyst.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.agroanalyst.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MercadoViewModel : OperacaoViewModel() {
    var precos by mutableStateOf<List<PrecoDto>>(emptyList())
        private set
    var series by mutableStateOf<Map<String, List<SeriePontoDto>>>(emptyMap())
        private set
    var aberta by mutableStateOf<String?>(null)
        private set
    private var iniciou = false

    fun iniciar() {
        if (iniciou) return
        iniciou = true
        viewModelScope.launch {
            Repositorio.semente()?.let { precos = it.precos.orEmpty(); series = it.series.orEmpty() }
            atualizar()
        }
    }

    fun atualizar() = executar {
        val novos = Repositorio.precos()
        if (novos.isNotEmpty()) precos = mesclarPrecos(precos, novos)
    }

    fun alternar(cultura: String) {
        aberta = if (aberta == cultura) null else cultura
        if (aberta == null) return
        viewModelScope.launch {
            try {
                val s = Repositorio.serie(cultura)
                if (s.isNotEmpty()) series = series + (cultura to s)
            } catch (e: CancellationException) {
                throw e
            } catch (_: ApiException) { }
        }
    }
}
