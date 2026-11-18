package com.example.agroanalyst.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agroanalyst.data.ApiException
import com.example.agroanalyst.data.CidadeDto
import com.example.agroanalyst.data.Repositorio
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BuscaViewModel : ViewModel() {
    var campo by mutableStateOf("")
        private set
    var sugestoes by mutableStateOf<List<CidadeDto>>(emptyList())
        private set
    var buscando by mutableStateOf(false)
        private set
    var mensagem by mutableStateOf<String?>(null)
        private set

    private var tarefa: Job? = null
    private var geracao = 0

    fun buscar(campoAtivo: String, texto: String) {
        tarefa?.cancel()
        val minha = ++geracao
        campo = campoAtivo
        val consulta = texto.trim()
        if (consulta.length < 2) { sugestoes = emptyList(); mensagem = null; buscando = false; return }
        tarefa = viewModelScope.launch {
            delay(400)
            buscando = true
            mensagem = null
            try {
                val achadas = Repositorio.cidades(consulta)
                if (minha == geracao) {
                    sugestoes = achadas
                    if (achadas.isEmpty()) mensagem = "Nenhuma cidade encontrada para \"$consulta\"."
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                if (minha == geracao) { sugestoes = emptyList(); mensagem = e.message }
            } finally {
                if (minha == geracao) buscando = false
            }
        }
    }

    fun limpar() {
        tarefa?.cancel()
        geracao++
        sugestoes = emptyList(); mensagem = null; buscando = false; campo = ""
    }
}
