package com.example.agroanalyst

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.agroanalyst.data.Sessao
import com.example.agroanalyst.ui.InicioTela
import com.example.agroanalyst.viewmodel.InicioViewModel

@Composable
fun AppAgro() {
    val nav = rememberNavController()
    val inicio: InicioViewModel = viewModel()
    Scaffold(contentWindowInsets = WindowInsets(0)) { padding ->
        NavHost(nav, startDestination = Inicio, modifier = Modifier.padding(padding).consumeWindowInsets(padding)) {
            composable<Inicio> {
                InicioTela(inicio, Sessao.nome, {}, Sessao::limpar, {}, {}, {})
            }
        }
    }
}
