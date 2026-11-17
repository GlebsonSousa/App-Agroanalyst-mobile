package com.example.agroanalyst.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.agroanalyst.data.SoloResposta
import com.example.agroanalyst.ui.theme.Puxador
import com.example.agroanalyst.viewmodel.AnaliseViewModel

@Composable
fun AnaliseResultadoTela(
    vm: AnaliseViewModel,
    lat: Double,
    lon: Double,
    aoVoltar: () -> Unit,
    aoAbrirCultura: (String) -> Unit,
    aoSalvarNoPlantio: (String?) -> Unit
) {
    LaunchedEffect(lat, lon) { vm.buscar(lat, lon) }
    val r = vm.resultado

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        BarraTopo("Resultado", aoVoltar, sobreMapa = true)
        if (r == null) {
            Column(Modifier.padding(16.dp)) {
                EstadoDaTela(vm.carregando, vm.demorando, vm.mensagem, { vm.tentarDeNovo(lat, lon) })
            }
        } else {
            Conteudo(r, lat, lon, aoAbrirCultura, aoSalvarNoPlantio)
        }
    }
}

@Composable
private fun Conteudo(r: SoloResposta, lat: Double, lon: Double, aoAbrirCultura: (String) -> Unit, aoSalvar: (String?) -> Unit) {
    val solo = r.soloLocal
    val chuva = r.chuva
    val local = listOfNotNull(chuva?.cidadeProxima, chuva?.estado).joinToString(", ")
    val meses = chuva?.somaChuvaMensal.orEmpty().map { (it.nomeMes ?: "?") to (it.somaMm ?: 0.0) }
    val recs = r.recomendacoes.orEmpty()

    Box(Modifier.fillMaxWidth()) {
        MapaContorno()
        Column(Modifier.padding(top = 148.dp)) {
            Column(
                Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Box(Modifier.align(Alignment.CenterHorizontally).size(width = 36.dp, height = 4.dp).background(Puxador, RoundedCornerShape(2.dp)))
                Spacer(Modifier.height(16.dp))
                val contexto = listOfNotNull(local.ifBlank { null }, "${lat.coordenada()} / ${lon.coordenada()}").joinToString(" · ")
                Text(contexto, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Text(solo?.tipoSolo ?: "Solo não identificado", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth()) {
                    Dado("pH", r.phExibido()?.formatar() ?: "—", Modifier.weight(1f))
                    Dado("Textura", solo?.textura.limpo(), Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth()) {
                    Dado("Drenagem", solo?.drenagem.limpo(), Modifier.weight(1f))
                    Dado("Fertilidade", solo?.fertilidade.limpo(), Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
            }
            Column(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CartaoInfo(titulo = "Chuva · ${(chuva?.chuvaTotalAnualMm ?: meses.sumOf { it.second }).formatar()} mm em 12 meses") {
                    if (meses.isEmpty()) Text("Sem dados de chuva para este ponto.", style = MaterialTheme.typography.bodyMedium)
                    else GraficoChuva(meses)
                }
                CartaoInfo(titulo = "Culturas recomendadas") {
                    if (recs.isEmpty()) Text("Nenhuma cultura recomendada para este ponto.", style = MaterialTheme.typography.bodyMedium)
                    recs.forEachIndexed { i, c ->
                        if (i > 0) Divisoria()
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable { c.nome?.let(aoAbrirCultura) }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(c.nome ?: "—", style = MaterialTheme.typography.bodyLarge)
                                Text(c.categoria ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Selo("Adequada")
                        }
                    }
                }
                Text("Fonte: meteoserver", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                BotaoPrincipal("Salvar no plantio", { aoSalvar(recs.firstOrNull()?.nome) })
            }
        }
    }
}
