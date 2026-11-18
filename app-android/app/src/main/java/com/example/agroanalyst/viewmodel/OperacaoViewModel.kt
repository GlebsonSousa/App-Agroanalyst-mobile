package com.example.agroanalyst.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agroanalyst.data.ApiException
import com.example.agroanalyst.data.Sessao
import com.example.agroanalyst.data.SessaoExpirada
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class OperacaoViewModel : ViewModel() {
    var carregando by mutableStateOf(false)
        private set
    var mensagem by mutableStateOf<String?>(null)
    var demorando by mutableStateOf(false)
        private set

    private var tarefa: Job? = null
    private var geracao = 0

    protected fun executar(substituir: Boolean = false, bloco: suspend () -> Unit) {
        if (carregando && !substituir) return
        tarefa?.cancel()
        val minha = ++geracao
        tarefa = viewModelScope.launch {
            carregando = true
            mensagem = null
            demorando = false
            val aviso = launch { delay(5_000); if (minha == geracao) demorando = true }
            try {
                bloco()
            } catch (e: CancellationException) {
                throw e
            } catch (e: SessaoExpirada) {
                Sessao.limpar()
            } catch (e: ApiException) {
                if (minha == geracao) mensagem = e.message
            } catch (e: Exception) {
                if (minha == geracao) mensagem = "Algo deu errado (${e.javaClass.simpleName}). Tente novamente."
            } finally {
                aviso.cancel()
                if (minha == geracao) { demorando = false; carregando = false }
            }
        }
    }

    open fun limpar() { mensagem = null }
}
