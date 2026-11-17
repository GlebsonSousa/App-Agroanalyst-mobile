package com.example.agroanalyst.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.agroanalyst.data.Repositorio
import com.example.agroanalyst.data.Sessao
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class AuthViewModel : OperacaoViewModel() {
    var aviso by mutableStateOf<String?>(null)
        private set

    /** Dispara o ping nos dois serviços e não espera: o app não depende do resultado. */
    fun aquecer() {
        viewModelScope.launch {
            try { Repositorio.aquecer() } catch (e: CancellationException) { throw e } catch (_: Exception) { }
        }
    }

    fun cadastrar(nome: String, email: String, senha: String, aoSucesso: () -> Unit) = executar {
        Repositorio.registrar(nome.trim(), email.trim(), senha)
        aviso = "Conta criada. Entre com seu e-mail e senha."
        aoSucesso()
    }

    fun entrar(email: String, senha: String, aoSucesso: () -> Unit) = executar {
        val r = Repositorio.login(email.trim(), senha)
        val token = r.token ?: throw com.example.agroanalyst.data.ApiException("Resposta de login sem token")
        aviso = null
        Sessao.iniciar(token, r.nome ?: "")
        aoSucesso()
    }

    fun sair() = Sessao.limpar()

    override fun limpar() { super.limpar(); aviso = null }
}
