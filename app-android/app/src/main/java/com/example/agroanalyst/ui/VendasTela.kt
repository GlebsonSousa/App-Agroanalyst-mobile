package com.example.agroanalyst.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.agroanalyst.data.VendaDto
import com.example.agroanalyst.viewmodel.LogisticaViewModel

@Composable
fun VendaItem(venda: VendaDto, aoAbrir: () -> Unit) {
    CartaoInfo(Modifier.clickable(onClick = aoAbrir)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(venda.destinoNome ?: "—", style = MaterialTheme.typography.titleMedium)
                Text("${venda.sacas ?: 0} sacas · margem ${(venda.margem ?: 0.0).reais()}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Selo((venda.status ?: "—").replaceFirstChar { it.uppercase() })
        }
    }
}

@Composable
fun VendasTela(vm: LogisticaViewModel, aoNova: () -> Unit, aoAbrir: (String) -> Unit, aoSair: () -> Unit) {
    LaunchedEffect(Unit) { vm.carregarVendas() }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            BarraTopo("Vendas", acoes = {
                IconButton(onClick = aoSair, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Filled.Logout, "Sair da conta") }
            })
            LazyColumn(contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    EstadoDaTela(vm.carregando, vm.demorando, vm.mensagem, vm::carregarVendas)
                    if (vm.vendas.isEmpty() && !vm.carregando && vm.mensagem == null)
                        Text("Nenhuma venda registrada. Toque em \"Nova venda\".", style = MaterialTheme.typography.bodyLarge)
                }
                items(vm.vendas, key = { it.id ?: it.criadoEm.orEmpty() }) { v -> VendaItem(v, aoAbrir = { v.id?.let(aoAbrir) }) }
            }
        }
        ExtendedFloatingActionButton(
            onClick = aoNova,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Nova venda") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }
}
