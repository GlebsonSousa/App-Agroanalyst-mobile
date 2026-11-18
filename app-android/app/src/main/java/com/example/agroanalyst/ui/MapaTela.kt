package com.example.agroanalyst.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.agroanalyst.data.PracaDto
import com.example.agroanalyst.viewmodel.CULTURAS
import com.example.agroanalyst.viewmodel.MapaViewModel

private val MODAIS = listOf("rodoviario" to "Rodoviário", "ferroviario" to "Ferroviário", "hidroviario" to "Hidroviário")

private fun nomeDaPerna(tipo: String?) = when (tipo) { "ferrovia" -> "Ferrovia"; "hidrovia" -> "Hidrovia"; else -> "Rodovia" }

@Composable
fun MapaTela(vm: MapaViewModel, logado: Boolean, aoCalcularFrete: (PracaDto, PracaDto, String) -> Unit) {
    LaunchedEffect(Unit) { vm.iniciar() }
    LaunchedEffect(logado) { vm.carregarLocais(logado) }
    val valores = vm.producao.associateBy { it.codigoIbge ?: "" }
    val numeros = vm.producao.mapNotNull { it.valorProducao }
    val cortes = cortesProducao(numeros)
    val cores = faixasDeCor()
    val nomeCultura = CULTURAS.first { it.first == vm.cultura }.second
    val corRota = MaterialTheme.colorScheme.primary
    val corFerroRota = MaterialTheme.colorScheme.onSurface
    val corHidroRota = MaterialTheme.colorScheme.onSecondaryContainer

    Column(Modifier.fillMaxSize()) {
        BarraTopo("Mapa", sobreMapa = true)
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChipCultura("Produção", vm.producaoLigada, vm::alternarProducao, Role.Checkbox)
            ChipCultura("Logística", vm.logisticaLigada, vm::alternarLogistica, Role.Checkbox)
        }
        if (vm.producaoLigada) LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(CULTURAS, key = { it.first }) { (id, rotulo) -> ChipCultura(rotulo, id == vm.cultura, { vm.selecionarCultura(id) }) }
        }
        Box(Modifier.weight(1f).fillMaxWidth().clipToBounds()) {
            MapaOsm(
                ufs = vm.ufs, producao = valores, cortes = cortes, cores = cores,
                corSemDado = MaterialTheme.colorScheme.outlineVariant,
                corFundo = MaterialTheme.colorScheme.surfaceVariant,
                mostrarProducao = vm.producaoLigada, mostrarLogistica = vm.logisticaLigada,
                pracas = vm.pracas, locais = vm.locais, linhas = vm.linhas, rota = vm.rota,
                corFerrovia = MaterialTheme.colorScheme.outline, corHidrovia = MaterialTheme.colorScheme.secondary,
                corPraca = MaterialTheme.colorScheme.secondary, corLocal = MaterialTheme.colorScheme.onSecondaryContainer,
                corRota = corRota, corRotaFerrovia = corFerroRota, corRotaHidrovia = corHidroRota,
                origem = vm.origem, destino = vm.destino,
                aoTocarUf = vm::tocarUf, aoTocarPraca = vm::tocarPraca,
                modifier = Modifier.fillMaxSize()
            )
            Text(
                "© OpenStreetMap",
                Modifier.align(Alignment.TopEnd).padding(8.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), MaterialTheme.shapes.small)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall
            )
            Column(Modifier.align(Alignment.TopStart).padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (vm.producaoLigada && numeros.isNotEmpty()) Column(
                    Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f), MaterialTheme.shapes.medium).padding(8.dp)
                ) {
                    Text("$nomeCultura · ${vm.producao.firstOrNull()?.ano ?: ""}", style = MaterialTheme.typography.labelMedium)
                    Legenda(cores.zip(rotulosFaixas(cortes, numeros.max())))
                    Text("Fonte: IBGE/SIDRA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (vm.logisticaLigada) Column(
                    Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f), MaterialTheme.shapes.medium).padding(8.dp)
                ) {
                    Text("Trajetos", style = MaterialTheme.typography.labelMedium)
                    Legenda(listOf(corRota to "Rodovia", corFerroRota to "Ferrovia", corHidroRota to "Hidrovia"))
                    Text("Fonte: IBGE, OSRM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(Modifier.align(Alignment.BottomCenter).padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (vm.logisticaLigada) CartaoInfo {
                    val o = vm.origem; val d = vm.destino
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MODAIS.forEach { (id, rotulo) -> ChipCultura(rotulo, id == vm.modal, { vm.mudarModal(id) }) }
                    }
                    if (o == null) Text("Toque numa praça ou num local seu para escolher a origem.", style = MaterialTheme.typography.bodyMedium)
                    else if (d == null) Text("Origem: ${o.nome}. Toque em outro ponto para o destino.", style = MaterialTheme.typography.bodyMedium)
                    else {
                        Text("${o.nome} → ${d.nome}", style = MaterialTheme.typography.titleMedium)
                        if (vm.calculandoRota) Text("Calculando o trajeto real…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        vm.avisoRota?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error) }
                        vm.rota?.let { r ->
                            Text("Distância por rota: ${(r.distanciaKm ?: 0.0).formatar(0)} km", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                r.pernas.orEmpty().joinToString(" · ") { "${nomeDaPerna(it.tipo)} ${(it.km ?: 0.0).formatar(0)} km" },
                                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        BotaoPrincipal("Calcular frete", { aoCalcularFrete(o, d, vm.modal) })
                    }
                    if (o != null) TextButton(colors = CoresTexto, onClick = vm::limparRota, modifier = Modifier.heightIn(min = 48.dp)) { Text("Limpar rota") }
                }
                vm.ufSelecionada?.let { u ->
                    CartaoInfo(titulo = "${u.uf} · $nomeCultura · ${u.ano}") {
                        Text("Valor: ${((u.valorProducao ?: 0.0) / 1000).formatar(0)} mi R$ · Quantidade: ${(u.quantidade ?: 0.0).formatar(0)} t", style = MaterialTheme.typography.bodyMedium)
                        Text("Área: ${(u.area ?: 0.0).formatar(0)} ha · Rendimento: ${(u.rendimento ?: 0.0).formatar(0)} kg/ha", style = MaterialTheme.typography.bodyMedium)
                        TextButton(colors = CoresTexto, onClick = vm::fecharUf, modifier = Modifier.heightIn(min = 48.dp)) { Text("Fechar") }
                    }
                }
            }
        }
    }
}
