package com.example.agroanalyst.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.agroanalyst.viewmodel.AuthViewModel

@Composable
fun CadastroUsuarioTela(vm: AuthViewModel, aoCadastrar: () -> Unit, aoVoltar: () -> Unit) {
    var nome by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var confirmar by rememberSaveable { mutableStateOf("") }
    var erroLocal by rememberSaveable { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize()) {
        BarraTopo("Criar conta", aoVoltar)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CampoTexto(nome, { nome = it }, "Nome")
            CampoTexto(email, { email = it }, "E-mail", tipo = KeyboardType.Email)
            CampoTexto(senha, { senha = it }, "Senha (mínimo 6 caracteres)", tipo = KeyboardType.Password, senha = true)
            CampoTexto(confirmar, { confirmar = it }, "Confirmar senha", tipo = KeyboardType.Password, senha = true)
            erroLocal?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error) }
            EstadoDaTela(vm.carregando, vm.demorando, vm.mensagem)
            BotaoPrincipal("Cadastrar", {
                erroLocal = when {
                    nome.isBlank() || email.isBlank() -> "Informe nome e e-mail."
                    senha.length < 6 -> "A senha precisa de pelo menos 6 caracteres."
                    senha != confirmar -> "As senhas não conferem."
                    else -> null
                }
                if (erroLocal == null) vm.cadastrar(nome, email, senha, aoCadastrar)
            }, carregando = vm.carregando)
        }
    }
}
