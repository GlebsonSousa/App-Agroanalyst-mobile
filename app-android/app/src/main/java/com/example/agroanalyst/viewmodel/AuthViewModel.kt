package com.example.agroanalyst.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.agroanalyst.data.ApiException
import com.example.agroanalyst.data.Repositorio
import com.example.agroanalyst.data.Sessao

class AuthViewModel : OperacaoViewModel() {
    var aviso by mutableStateOf<String?>(null)
        private set
    var origem: Any? = null
        private set

    fun pedirLogin(destinoAposEntrar: Any?) {
        origem = destinoAposEntrar
        aviso = null
        mensagem = null
    }

    fun cadastrar(nome: String, email: String, senha: String, aoSucesso: () -> Unit) = executar {
        Repositorio.registrar(nome.trim(), email.trim(), senha)
        aviso = "Conta criada. Entre com seu e-mail e senha."
        aoSucesso()
    }

    fun entrar(email: String, senha: String, aoSucesso: (Any?) -> Unit) = executar {
        val r = Repositorio.login(email.trim(), senha)
        val token = r.token ?: throw ApiException("Resposta de login sem token")
        aviso = null
        Sessao.iniciar(token, r.nome ?: "")
        aoSucesso(origem)
    }

    fun sair() = Sessao.limpar()

    override fun limpar() { super.limpar(); aviso = null; origem = null }
}
