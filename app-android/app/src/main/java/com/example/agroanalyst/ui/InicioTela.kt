package com.example.agroanalyst.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.agroanalyst.viewmodel.CULTURAS
import com.example.agroanalyst.viewmodel.InicioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioTela(
    vm: InicioViewModel,
    nome: String?,
    aoEntrar: () -> Unit,
    aoSair: () -> Unit,
    aoAbrirMapa: (String) -> Unit,
    aoAnalisar: () -> Unit,
    aoFrete: () -> Unit
) {
    LaunchedEffect(Unit) { vm.iniciar() }
    val posicionador = rememberPosicionador { lat, lon -> vm.usarPosicao(lat, lon) }
    val cultura = vm.cultura
    val nomeCultura = CULTURAS.first { it.first == cultura }.second
    val producao = vm.producao[cultura].orEmpty()
    val valores = producao.associate { (it.codigoIbge ?: "") to it.valorProducao }
    val cortes = cortesProducao(valores.values.filterNotNull())
    val serie = vm.series[cultura].orEmpty()

    PullToRefreshBox(isRefreshing = vm.carregando, onRefresh = vm::atualizar, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth().statusBarsPadding().padding(start = 16.dp, end = 4.dp, top = 8.dp).heightIn(min = 56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(if (nome != null) "Olá, $nome" else "AgroAnalyst", style = MaterialTheme.typography.headlineSmall, maxLines = 1)
                        Text("Mercado, clima e solo em um só lugar", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (nome != null) IconButton(onClick = aoSair, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Sair da conta")
                    } else TextButton(onClick = aoEntrar, modifier = Modifier.heightIn(min = 48.dp)) { Text("Entrar") }
                }
            }
            if (vm.carregando && vm.demorando) item {
                Text("Acordando o servidor, isso pode levar até um minuto…", Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            vm.mensagem?.let { m ->
                item { Text(m, Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error) }
            }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(vm.precos, key = { it.cultura ?: "" }) { p ->
                        CartaoPreco(
                            CULTURAS.firstOrNull { it.first == p.cultura }?.second ?: (p.cultura ?: ""),
                            (p.valor ?: 0.0).formatar(2), p.unidade ?: "", p.variacaoDia, (p.data ?: "").dataCurta()
                        )
                    }
                }
            }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CULTURAS, key = { it.first }) { (id, rotulo) ->
                        ChipCultura(rotulo, id == cultura) { vm.selecionarCultura(id) }
                    }
                }
            }
            item {
                CartaoInfo(Modifier.padding(horizontal = 16.dp), titulo = "Clima · ${vm.localClima}") {
                    val hoje = vm.clima?.hoje
                    if (hoje == null) Text("Sem previsão no momento.", style = MaterialTheme.typography.bodyMedium)
                    else {
                        Row(Modifier.fillMaxWidth()) {
                            Dado("Hoje", "${(hoje.max ?: 0.0).formatar(0)}° / ${(hoje.min ?: 0.0).formatar(0)}°", Modifier.weight(1f))
                            Dado("Chuva", "${(hoje.chuva ?: 0.0).formatar()} mm", Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth()) {
                            vm.clima?.proximos.orEmpty().forEach { d ->
                                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text((d.data ?: "").diaSemana(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${(d.max ?: 0.0).formatar(0)}°", style = MaterialTheme.typography.labelMedium)
                                    Text("${(d.chuva ?: 0.0).formatar(0)} mm", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = posicionador.solicitar, modifier = Modifier.heightIn(min = 48.dp)) { Text("Usar minha posição") }
                    posicionador.aviso?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error) }
                    if (posicionador.estado != EstadoPermissao.NaoPedida)
                        Text("Sem permissão de localização, a previsão fica em Mauá, SP.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                val ano = producao.firstOrNull()?.ano
                CartaoInfo(Modifier.padding(horizontal = 16.dp), titulo = "Produção de ${nomeCultura.lowercase()} por UF${ano?.let { " · $it" } ?: ""}") {
                    MiniMapaBrasil(vm.ufs, valores, cortes, { aoAbrirMapa(cultura) })
                    Spacer(Modifier.height(8.dp))
                    val maximo = valores.values.filterNotNull().maxOrNull() ?: 0.0
                    Legenda(faixasDeCor().zip(rotulosFaixas(cortes, maximo)))
                }
            }
            item {
                CartaoInfo(Modifier.padding(horizontal = 16.dp), titulo = "Preço de ${nomeCultura.lowercase()} · últimos ${serie.size} dias") {
                    if (serie.size < 2) Text("Histórico ainda curto.", style = MaterialTheme.typography.bodyMedium)
                    else GraficoLinha(serie.map { it.valor ?: 0.0 }, (serie.first().data ?: "").dataCurta(), (serie.last().data ?: "").dataCurta())
                }
            }
            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BotaoPrincipal("Analisar meu solo", aoAnalisar)
                    BotaoSecundario("Calcular frete", aoFrete)
                }
            }
            item {
                CartaoInfo(Modifier.padding(horizontal = 16.dp), titulo = "Maiores municípios produtores de ${nomeCultura.lowercase()}") {
                    val topo = vm.top[cultura].orEmpty()
                    if (topo.isEmpty()) Text("Sem dados no momento.", style = MaterialTheme.typography.bodyMedium)
                    topo.forEachIndexed { i, m ->
                        if (i > 0) Divisoria()
                        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${i + 1}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(32.dp))
                            Column(Modifier.weight(1f)) {
                                Text("${m.municipio} · ${m.uf}", style = MaterialTheme.typography.bodyLarge)
                                Text("${((m.quantidade ?: 0.0)).formatar(0)} t · ${m.ano}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("R$ ${((m.valorProducao ?: 0.0) / 1000).formatar(0)} mi", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
            item {
                Text(
                    "Fontes: CEPEA/ESALQ (preços), IBGE/SIDRA (produção), Open-Meteo (clima) · ${vm.rotuloDados}",
                    Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
