package com.example.agroanalyst.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Token e nome só em memória: não persiste em disco (proposital) e nunca guarda a senha. */
object Sessao {
    var token by mutableStateOf<String?>(null)
        private set
    var nome by mutableStateOf<String?>(null)
        private set

    fun iniciar(token: String, nome: String) { this.token = token; this.nome = nome }
    fun limpar() { token = null; nome = null }

}
