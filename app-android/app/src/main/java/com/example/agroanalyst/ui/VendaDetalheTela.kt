package com.example.agroanalyst.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.agroanalyst.viewmodel.LogisticaViewModel

private val STATUS = listOf("pendente", "confirmada", "entregue", "cancelada").map { it to it.replaceFirstChar { c -> c.uppercase() } }

@Composable
fun VendaDetalheTela(id: String, vm: LogisticaViewModel, aoVoltar: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(id) { vm.carregarVenda(id) }
    val v = vm.venda?.takeIf { it.id == id }

    Column(Modifier.fillMaxSize()) {
        BarraTopo(v?.destinoNome ?: "Venda", aoVoltar)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (v == null) {
                EstadoDaTela(vm.carregando, vm.demorando, vm.mensagem, { vm.carregarVenda(id) })
            } else {
                CartaoInfo(titulo = "Números da venda") {
                    Row(Modifier.fillMaxWidth()) {
                        Dado("Sacas", "${v.sacas ?: 0}", Modifier.weight(1f))
                        Dado("Preço por saca", (v.precoSaca ?: 0.0).reais(), Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Dado("Receita", (v.receitaTotal ?: 0.0).reais(), Modifier.weight(1f))
                        Dado("Frete", (v.freteTotal ?: 0.0).reais(), Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Dado("Margem", (v.margem ?: 0.0).reais(), Modifier.weight(1f))
                        Dado("Distância estimada", "${(v.distanciaKm ?: 0.0).formatar()} km", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    Dado("Modal", (v.modal ?: "—").replaceFirstChar { it.uppercase() })
                }
                CartaoInfo {
                    SelecaoRadio("Status", STATUS, v.status ?: "") { vm.mudarStatus(id, it) }
                }
                EstadoDaTela(vm.carregando, vm.demorando, vm.mensagem)
                BotaoPrincipal("Ver rota no mapa", { abrirRotaNoMapa(context, v.destinoLat ?: 0.0, v.destinoLon ?: 0.0, v.destinoNome ?: "Destino") })
                BotaoSecundario("Compartilhar resumo", {
                    compartilharTexto(context,
                        "Venda AgroAnalyst\nDestino: ${v.destinoNome}\n${v.sacas} sacas a ${(v.precoSaca ?: 0.0).reais()}\n" +
                        "Frete: ${(v.freteTotal ?: 0.0).reais()} (${(v.distanciaKm ?: 0.0).formatar()} km estimados)\n" +
                        "Margem: ${(v.margem ?: 0.0).reais()}\nStatus: ${v.status}")
                })
            }
        }
    }
}
