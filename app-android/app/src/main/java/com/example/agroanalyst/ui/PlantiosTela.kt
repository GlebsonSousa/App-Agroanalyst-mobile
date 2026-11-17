package com.example.agroanalyst.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.agroanalyst.data.ApiClient
import com.example.agroanalyst.data.PlantioDto
import com.example.agroanalyst.viewmodel.PlantiosViewModel

@Composable
fun PlantioItem(plantio: PlantioDto, aoAbrir: () -> Unit, aoExcluir: () -> Unit) {
    CartaoInfo(Modifier.clickable(onClick = aoAbrir)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (plantio.fotoId != null) {
                AsyncImage(ApiClient.urlFoto(plantio.fotoId), contentDescription = "Foto do plantio ${plantio.nome}",
                    contentScale = ContentScale.Crop, modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.medium))
            } else {
                Icon(Icons.Filled.Grass, contentDescription = null, modifier = Modifier.size(56.dp).padding(12.dp), tint = MaterialTheme.colorScheme.secondary)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(plantio.nome ?: "—", style = MaterialTheme.typography.titleMedium)
                Text("${plantio.cultura ?: "—"} · ${(plantio.areaHa ?: 0.0).formatar()} ha", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = aoExcluir, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Filled.Delete, contentDescription = "Excluir plantio ${plantio.nome}")
            }
        }
    }
}

@Composable
fun PlantiosTela(vm: PlantiosViewModel, nome: String?, aoNovo: () -> Unit, aoAbrir: (String) -> Unit, aoSair: () -> Unit) {
    LaunchedEffect(Unit) { vm.carregarLista() }
    var excluindo by remember { mutableStateOf<PlantioDto?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            BarraTopo(if (nome != null) "Plantios de $nome" else "Meus plantios", acoes = {
                IconButton(onClick = aoSair, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Filled.Logout, "Sair da conta") }
            })
            if (vm.lista.isEmpty()) {
                Column(Modifier.padding(16.dp)) {
                    EstadoDaTela(vm.carregando, vm.demorando, vm.mensagem, vm::carregarLista)
                    if (!vm.carregando && vm.mensagem == null) Text("Nenhum plantio ainda. Toque em \"Novo plantio\".", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item { EstadoDaTela(vm.carregando, vm.demorando, vm.mensagem, vm::carregarLista) }
                    items(vm.lista, key = { it.id ?: it.nome.orEmpty() }) { p ->
                        PlantioItem(p, aoAbrir = { p.id?.let(aoAbrir) }, aoExcluir = { excluindo = p })
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = aoNovo,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Novo plantio") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }

    excluindo?.let { p ->
        AlertDialog(
            onDismissRequest = { excluindo = null },
            title = { Text("Excluir plantio?") },
            text = { Text("\"${p.nome}\" será removido da sua conta.") },
            confirmButton = { TextButton(onClick = { p.id?.let { vm.excluir(it) }; excluindo = null }) { Text("Excluir") } },
            dismissButton = { TextButton(onClick = { excluindo = null }) { Text("Cancelar") } }
        )
    }
}
