package com.example.agroanalyst.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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

@Composable
fun MapaTela(vm: MapaViewModel, aoCalcularFrete: (PracaDto, PracaDto) -> Unit) {
    LaunchedEffect(Unit) { vm.iniciar() }
    val valores = vm.producao.associateBy { it.codigoIbge ?: "" }
    val numeros = vm.producao.mapNotNull { it.valorProducao }
    val cortes = cortesProducao(numeros)
    val cores = faixasDeCor()
    val nomeCultura = CULTURAS.first { it.first == vm.cultura }.second

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
                pracas = vm.pracas, linhas = vm.linhas,
                corFerrovia = MaterialTheme.colorScheme.outline, corHidrovia = MaterialTheme.colorScheme.secondary,
                corPraca = MaterialTheme.colorScheme.secondary, corRota = MaterialTheme.colorScheme.primary,
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
            if (vm.producaoLigada && numeros.isNotEmpty()) Column(
                Modifier.align(Alignment.TopStart).padding(8.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f), MaterialTheme.shapes.medium)
                    .padding(8.dp)
            ) {
                Text("${nomeCultura} · ${vm.producao.firstOrNull()?.ano ?: ""}", style = MaterialTheme.typography.labelMedium)
                Legenda(cores.zip(rotulosFaixas(cortes, numeros.max())))
                Text("Fonte: IBGE/SIDRA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(Modifier.align(Alignment.BottomCenter).padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (vm.logisticaLigada) CartaoInfo {
                    val o = vm.origem; val d = vm.destino
                    if (vm.linhas == null) Text("Traçado de ferrovias e hidrovias indisponível; mostrando só as praças.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (o == null) Text("Toque numa praça para escolher a origem.", style = MaterialTheme.typography.bodyMedium)
                    else if (d == null) Text("Origem: ${o.nome}. Toque em outra praça para o destino.", style = MaterialTheme.typography.bodyMedium)
                    else {
                        Text("${o.nome} → ${d.nome}", style = MaterialTheme.typography.titleMedium)
                        Text("Distância estimada: ${(vm.distanciaEstimadaKm() ?: 0.0).formatar(0)} km", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        BotaoPrincipal("Calcular frete", { aoCalcularFrete(o, d) })
                    }
                    if (o != null) TextButton(onClick = vm::limparRota, modifier = Modifier.heightIn(min = 48.dp)) { Text("Limpar rota") }
                }
                vm.ufSelecionada?.let { u ->
                    CartaoInfo(titulo = "${u.uf} · ${nomeCultura} · ${u.ano}") {
                        Text("Valor: ${((u.valorProducao ?: 0.0) / 1000).formatar(0)} mi R$ · Quantidade: ${(u.quantidade ?: 0.0).formatar(0)} t", style = MaterialTheme.typography.bodyMedium)
                        Text("Área: ${(u.area ?: 0.0).formatar(0)} ha · Rendimento: ${(u.rendimento ?: 0.0).formatar(0)} kg/ha", style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = vm::fecharUf, modifier = Modifier.heightIn(min = 48.dp)) { Text("Fechar") }
                    }
                }
            }
        }
    }
}
