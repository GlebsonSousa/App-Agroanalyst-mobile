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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class OperacaoViewModel : ViewModel() {
    var carregando by mutableStateOf(false)
        private set
    var mensagem by mutableStateOf<String?>(null)
    var demorando by mutableStateOf(false)
        private set

    protected fun executar(bloco: suspend () -> Unit) {
        if (carregando) return
        viewModelScope.launch {
            carregando = true
            mensagem = null
            val aviso = launch { delay(5_000); demorando = true }
            try {
                bloco()
            } catch (e: CancellationException) {
                throw e
            } catch (e: SessaoExpirada) {
                Sessao.limpar()
            } catch (e: ApiException) {
                mensagem = e.message
            } catch (e: Exception) {
                mensagem = "Algo deu errado (${e.javaClass.simpleName}). Tente novamente."
            } finally {
                aviso.cancel()
                demorando = false
                carregando = false
            }
        }
    }

    open fun limpar() { mensagem = null }
}
