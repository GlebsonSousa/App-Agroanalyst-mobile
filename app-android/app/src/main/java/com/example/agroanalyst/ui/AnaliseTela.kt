package com.example.agroanalyst.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.agroanalyst.viewmodel.AnaliseViewModel
import com.example.agroanalyst.viewmodel.BuscaViewModel

@Composable
fun AnaliseTela(vm: AnaliseViewModel, busca: BuscaViewModel, aoAnalisar: (Double, Double) -> Unit) {
    var lat by rememberSaveable { mutableStateOf("") }
    var lon by rememberSaveable { mutableStateOf("") }
    var nome by rememberSaveable { mutableStateOf("") }
    val posicionador = rememberPosicionador { a, o -> lat = a.toString(); lon = o.toString(); nome = "Minha posição" }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        BarraTopo("Análise de solo", sobreMapa = true)
        MapaContorno()
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Escolha o ponto", style = MaterialTheme.typography.headlineSmall)
            Text("Busque a cidade, use a sua posição ou digite a coordenada do talhão.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SeletorLocal("analise", "Local do talhão", busca, lat, lon, nome) { a, o, n -> lat = a; lon = o; nome = n }
            BotaoSecundario("Usar minha posição", posicionador.solicitar)
            when (posicionador.estado) {
                EstadoPermissao.NegadaUmaVez -> Text(
                    "A posição só preenche a coordenada. Toque em \"Usar minha posição\" para tentar de novo ou busque a cidade.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                EstadoPermissao.NegadaPermanente -> Text(
                    "A permissão de localização foi negada. Busque a cidade ou digite a coordenada (ou libere nas configurações do aparelho).",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                EstadoPermissao.NaoPedida -> {}
            }
            posicionador.aviso?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error) }
            vm.mensagem?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error) }
            BotaoPrincipal("Analisar ponto", { vm.coordenadas(lat, lon)?.let { (a, o) -> aoAnalisar(a, o) } })
        }
    }
}
