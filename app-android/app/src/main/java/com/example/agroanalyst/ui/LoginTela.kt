package com.example.agroanalyst.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.agroanalyst.viewmodel.AuthViewModel

@Composable
fun LoginTela(vm: AuthViewModel, aoEntrar: (Any?) -> Unit, aoIrParaCadastro: () -> Unit, aoVoltar: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        BarraTopo("Entrar", aoVoltar, sobreMapa = true)
        MapaContorno(altura = 120.dp)
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Entre para salvar plantios, fotos e fretes.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            vm.aviso?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer) }
            CampoTexto(email, { email = it }, "E-mail", tipo = KeyboardType.Email)
            CampoTexto(senha, { senha = it }, "Senha", tipo = KeyboardType.Password, senha = true)
            EstadoDaTela(vm.carregando, vm.demorando, vm.mensagem)
            BotaoPrincipal("Entrar", { vm.entrar(email, senha, aoEntrar) }, carregando = vm.carregando, habilitado = email.isNotBlank() && senha.isNotBlank())
            TextButton(onClick = aoIrParaCadastro, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Criar conta") }
        }
    }
}
