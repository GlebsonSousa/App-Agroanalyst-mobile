package com.example.agroanalyst.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.agroanalyst.ui.theme.Alta
import com.example.agroanalyst.ui.theme.Baixa
import com.example.agroanalyst.viewmodel.CULTURAS
import com.example.agroanalyst.viewmodel.MercadoViewModel

@Composable
fun MercadoTela(vm: MercadoViewModel) {
    LaunchedEffect(Unit) { vm.iniciar() }
    Column(Modifier.fillMaxSize()) {
        BarraTopo("Mercado")
        LazyColumn(contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { EstadoDaTela(vm.carregando, vm.demorando, vm.mensagem, vm::atualizar) }
            items(vm.precos, key = { it.cultura ?: "" }) { p ->
                val nome = CULTURAS.firstOrNull { it.first == p.cultura }?.second ?: (p.cultura ?: "")
                CartaoInfo(Modifier.clickable { p.cultura?.let(vm::alternar) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(nome, style = MaterialTheme.typography.titleMedium)
                            Text("${p.unidade} · ${p.fonte} · ${(p.data ?: "").dataCurta()}${if (p.stale == true) " · desatualizado" else ""}",
                                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text((p.valor ?: 0.0).formatar(2), style = MaterialTheme.typography.titleLarge)
                            p.variacaoDia?.let { v ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val cor = if (v >= 0) Alta else Baixa
                                    Icon(if (v >= 0) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown, null, tint = cor, modifier = Modifier.size(24.dp))
                                    Text(v.variacao(), style = MaterialTheme.typography.labelMedium, color = cor)
                                }
                            }
                        }
                    }
                    if (vm.aberta == p.cultura) {
                        val serie = vm.series[p.cultura].orEmpty()
                        Spacer(Modifier.height(12.dp))
                        Divisoria()
                        Spacer(Modifier.height(12.dp))
                        if (serie.size < 2) Text("Histórico ainda curto.", style = MaterialTheme.typography.bodyMedium)
                        else {
                            Text("Últimos ${serie.size} dias", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            GraficoLinha(serie.map { it.valor ?: 0.0 }, (serie.first().data ?: "").dataCurta(), (serie.last().data ?: "").dataCurta())
                        }
                    }
                }
            }
            item {
                Text("Fonte: CEPEA/ESALQ. Preços com até 6 horas de atraso. Toque numa cultura para ver o histórico.",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
