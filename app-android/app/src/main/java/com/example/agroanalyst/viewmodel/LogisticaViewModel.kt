package com.example.agroanalyst.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.agroanalyst.data.DestinoDto
import com.example.agroanalyst.data.FreteDto
import com.example.agroanalyst.data.GraoDto
import com.example.agroanalyst.data.FreteEnvio
import com.example.agroanalyst.data.Repositorio
import com.example.agroanalyst.data.VendaDto
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class LogisticaViewModel : OperacaoViewModel() {
    var destinos by mutableStateOf<List<DestinoDto>>(emptyList())
        private set
    var graos by mutableStateOf<List<GraoDto>>(emptyList())
        private set
    var resultado by mutableStateOf<FreteDto?>(null)
        private set
    var vendas by mutableStateOf<List<VendaDto>>(emptyList())
        private set
    var venda by mutableStateOf<VendaDto?>(null)
        private set
    /** Plantio escolhido na tela de detalhe antes de abrir o frete. */
    var plantioPreSelecionado by mutableStateOf<String?>(null)

    fun limparResultado() { resultado = null }

    /** Destinos e grãos são independentes: buscados em paralelo com async/await. */
    fun carregarOpcoes() = executar {
        coroutineScope {
            val d = async { Repositorio.destinos() }
            val g = async { Repositorio.graos() }
            destinos = d.await()
            graos = g.await()
        }
    }

    fun calcular(e: FreteEnvio) = executar { resultado = Repositorio.calcularFrete(e) }

    fun registrarVenda(e: FreteEnvio, aoSucesso: () -> Unit) = executar {
        Repositorio.criarVenda(e)
        vendas = Repositorio.vendas()
        resultado = null
        aoSucesso()
    }

    fun carregarVendas() = executar { vendas = Repositorio.vendas() }

    fun carregarVenda(id: String) {
        if (venda?.id != id) venda = null
        executar { venda = Repositorio.venda(id) }
    }

    fun mudarStatus(id: String, status: String) = executar {
        venda = Repositorio.mudarStatus(id, status)
        vendas = Repositorio.vendas()
    }

    override fun limpar() {
        super.limpar(); destinos = emptyList(); graos = emptyList(); resultado = null; vendas = emptyList(); venda = null; plantioPreSelecionado = null
    }
}
