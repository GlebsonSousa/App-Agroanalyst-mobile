package com.example.agroanalyst.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.agroanalyst.viewmodel.BuscaViewModel
import com.example.agroanalyst.viewmodel.FreteViewModel
import com.example.agroanalyst.viewmodel.PlantiosViewModel

private val MODAIS = listOf("rodoviario" to "Rodoviário", "ferroviario" to "Ferroviário", "hidroviario" to "Hidroviário")
private val GRAOS = listOf("soja" to "Soja", "milho" to "Milho")

private class Atalho(val nome: String, val lat: Double?, val lon: Double?, val plantioId: String? = null)

@Composable
private fun Atalhos(titulo: String, itens: List<Atalho>, aoEscolher: (Atalho) -> Unit) {
    if (itens.isEmpty()) return
    Column {
        Text(titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        itens.forEach { a ->
            Text(a.nome, Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable { aoEscolher(a) }.padding(vertical = 12.dp), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun FreteTela(
    vm: FreteViewModel,
    plantios: PlantiosViewModel,
    busca: BuscaViewModel,
    logado: Boolean,
    aoPedirLogin: () -> Unit,
    aoVerRota: () -> Unit,
    aoVoltar: () -> Unit
) {
    LaunchedEffect(logado) {
        vm.carregarPracas()
        if (logado) { plantios.carregarLista(); vm.carregarLocais() }
    }
    val r = vm.rascunho
    var salvando by rememberSaveable { mutableStateOf<String?>(null) }
    var nomeLocal by rememberSaveable { mutableStateOf("") }

    fun localValido(lat: String, lon: String) = lat.trim().replace(',', '.').toDoubleOrNull() != null && lon.trim().replace(',', '.').toDoubleOrNull() != null

    Column(Modifier.fillMaxSize()) {
        BarraTopo("Calcular frete", aoVoltar)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CartaoInfo(titulo = "Origem") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (logado) Atalhos("Meus plantios", plantios.lista.map { Atalho(it.nome ?: "—", it.lat, it.lon, it.id) }) { a ->
                        vm.atualizarRascunho(r.copy(plantioId = a.plantioId.orEmpty(), origemLat = a.lat.toString(), origemLon = a.lon.toString(), origemNome = a.nome))
                    }
                    if (logado) Atalhos("Meus locais", vm.locais.filter { it.tipo != "destino" }.map { Atalho(it.nome ?: "—", it.lat, it.lon) }) { a ->
                        vm.atualizarRascunho(r.copy(origemLat = a.lat.toString(), origemLon = a.lon.toString(), origemNome = a.nome))
                    }
                    SeletorLocal("origem", "Local de partida", busca, r.origemLat, r.origemLon, r.origemNome) { a, o, n ->
                        vm.atualizarRascunho(vm.rascunho.copy(origemLat = a, origemLon = o, origemNome = n, plantioId = if (n.isBlank()) vm.rascunho.plantioId else ""))
                    }
                    if (localValido(r.origemLat, r.origemLon)) TextButton(colors = CoresTexto, onClick = {
                        if (logado) { nomeLocal = r.origemNome; salvando = "origem" } else aoPedirLogin()
                    }, modifier = Modifier.heightIn(min = 48.dp)) { Text(if (logado) "Salvar em meus locais" else "Entrar para salvar o local") }
                }
            }
            CartaoInfo(titulo = "Destino") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Atalhos("Praças e terminais", vm.pracas.map { Atalho("${it.nome} · ${it.uf}", it.lat, it.lon) }) { a ->
                        vm.atualizarRascunho(r.copy(destinoLat = a.lat.toString(), destinoLon = a.lon.toString(), destinoNome = a.nome.substringBefore(" · ")))
                    }
                    if (logado) Atalhos("Meus locais", vm.locais.filter { it.tipo != "origem" }.map { Atalho(it.nome ?: "—", it.lat, it.lon) }) { a ->
                        vm.atualizarRascunho(r.copy(destinoLat = a.lat.toString(), destinoLon = a.lon.toString(), destinoNome = a.nome))
                    }
                    SeletorLocal("destino", "Local de entrega", busca, r.destinoLat, r.destinoLon, r.destinoNome) { a, o, n ->
                        vm.atualizarRascunho(vm.rascunho.copy(destinoLat = a, destinoLon = o, destinoNome = n))
                    }
                    if (localValido(r.destinoLat, r.destinoLon)) TextButton(colors = CoresTexto, onClick = {
                        if (logado) { nomeLocal = r.destinoNome; salvando = "destino" } else aoPedirLogin()
                    }, modifier = Modifier.heightIn(min = 48.dp)) { Text(if (logado) "Salvar em meus locais" else "Entrar para salvar o local") }
                }
            }
            vm.avisoLocal?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer) }

            SelecaoRadio("Grão", GRAOS, r.grao) { vm.atualizarRascunho(r.copy(grao = it)) }
            SelecaoRadio("Modal", MODAIS, r.modal) { vm.atualizarRascunho(r.copy(modal = it)) }
            CampoTexto(r.sacas, { vm.atualizarRascunho(r.copy(sacas = it)) }, "Sacas de 60 kg", tipo = KeyboardType.Number)
            CampoTexto(r.preco, { vm.atualizarRascunho(r.copy(preco = it)) }, "Preço por saca (R$)", tipo = KeyboardType.Decimal)
            if (r.modal == "rodoviario") {
                CampoTexto(r.capacidade, { vm.atualizarRascunho(r.copy(capacidade = it)) }, "Capacidade do caminhão (t)", tipo = KeyboardType.Decimal)
                CampoTexto(r.kmPorLitro, { vm.atualizarRascunho(r.copy(kmPorLitro = it)) }, "Consumo (km/L)", tipo = KeyboardType.Decimal)
                CampoTexto(r.eixos, { vm.atualizarRascunho(r.copy(eixos = it)) }, "Eixos", tipo = KeyboardType.Number)
            }
            EstadoDaTela(vm.carregando, vm.demorando, vm.mensagem)
            BotaoPrincipal("Calcular", vm::calcular, carregando = vm.carregando)

            vm.resultado?.let { f ->
                CartaoInfo(titulo = "Resultado") {
                    Row(Modifier.fillMaxWidth()) {
                        Dado(if (f.fonteDistancia == "rota") "Distância por rota" else "Distância estimada", "${(f.distanciaKm ?: 0.0).formatar()} km", Modifier.weight(1f))
                        Dado("Tempo", "${(f.duracaoH ?: 0.0).formatar()} h", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Dado("Frete total", (f.freteTotal ?: 0.0).reais(), Modifier.weight(1f))
                        Dado("Frete por saca", (f.fretePorSaca ?: 0.0).reais(), Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Dado("Receita", (f.receitaTotal ?: 0.0).reais(), Modifier.weight(1f))
                        Dado("Margem", (f.margem ?: 0.0).reais(), Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Dado("Líquido por saca", (f.liquidoSaca ?: 0.0).reais(), Modifier.weight(1f))
                        f.viagens?.let { Dado("Viagens", it.toString(), Modifier.weight(1f)) } ?: Spacer(Modifier.weight(1f))
                    }
                    Text(
                        if (f.fonteDistancia == "rota") "Distância calculada pelo trajeto real (rodovia, ferrovia ou hidrovia)."
                        else "Sem trajeto real para este par de locais: distância em linha reta × 1,3.",
                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp)
                    )
                }
                BotaoSecundario("Ver rota no mapa", aoVerRota)
                if (vm.salvo) Text("Frete salvo no plantio.", style = MaterialTheme.typography.bodyLarge)
                else if (!logado) BotaoSecundario("Entrar para salvar", aoPedirLogin)
                else if (r.plantioId.isNotEmpty()) BotaoPrincipal("Salvar no plantio", vm::salvar, carregando = vm.carregando)
                else Text("Para salvar o frete, escolha um dos seus plantios como origem.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (logado && vm.locais.isNotEmpty()) CartaoInfo(titulo = "Meus locais") {
                vm.locais.forEachIndexed { i, l ->
                    if (i > 0) Divisoria()
                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(l.nome ?: "—", style = MaterialTheme.typography.bodyLarge)
                            Text("${l.tipo} · ${(l.lat ?: 0.0).coordenada()} / ${(l.lon ?: 0.0).coordenada()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { l.id?.let(vm::excluirLocal) }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remover ${l.nome}")
                        }
                    }
                }
            }
        }
    }

    salvando?.let { tipo ->
        AlertDialog(
            onDismissRequest = { salvando = null },
            title = { Text("Salvar em meus locais") },
            text = { CampoTexto(nomeLocal, { nomeLocal = it }, "Nome do local") },
            confirmButton = {
                TextButton(colors = CoresTexto, onClick = {
                    val lat = (if (tipo == "origem") r.origemLat else r.destinoLat).trim().replace(',', '.').toDoubleOrNull()
                    val lon = (if (tipo == "origem") r.origemLon else r.destinoLon).trim().replace(',', '.').toDoubleOrNull()
                    if (lat != null && lon != null && nomeLocal.isNotBlank()) vm.salvarLocal(nomeLocal, lat, lon, tipo)
                    salvando = null
                }) { Text("Salvar") }
            },
            dismissButton = { TextButton(colors = CoresTexto, onClick = { salvando = null }) { Text("Cancelar") } }
        )
    }
}
