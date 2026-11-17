package com.example.agroanalyst.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.agroanalyst.data.urlFoto
import com.example.agroanalyst.viewmodel.AnaliseViewModel
import com.example.agroanalyst.viewmodel.PlantiosViewModel

@Composable
fun PlantioDetalheTela(
    id: String,
    vm: PlantiosViewModel,
    analise: AnaliseViewModel,
    aoVoltar: () -> Unit,
    aoEditar: () -> Unit,
    aoFrete: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(id) { vm.carregarUm(id) }
    val p = vm.atual?.takeIf { it.id == id }
    LaunchedEffect(p?.lat, p?.lon) { if (p?.lat != null && p.lon != null) analise.buscar(p.lat, p.lon) }

    Column(Modifier.fillMaxSize()) {
        BarraTopo(p?.nome ?: "Plantio", aoVoltar)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (p == null) {
                EstadoDaTela(vm.carregando, vm.demorando, vm.mensagem, { vm.carregarUm(id) })
            } else {
                if (p.fotoId != null) AsyncImage(urlFoto(p.fotoId), contentDescription = "Foto do plantio ${p.nome}",
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(200.dp).clip(MaterialTheme.shapes.large))
                CartaoInfo(titulo = "Dados") {
                    Row(Modifier.fillMaxWidth()) {
                        Dado("Cultura", p.cultura ?: "—", Modifier.weight(1f))
                        Dado("Área", "${(p.areaHa ?: 0.0).formatar()} ha", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Dado("Produtividade", "${(p.produtividadeSacasHa ?: 0.0).formatar()} sc/ha", Modifier.weight(1f))
                        Dado("Colheita estimada", "${(p.sacasEstimadas ?: 0.0).formatar(0)} sacas", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    Dado("Coordenada", "${(p.lat ?: 0.0).coordenada()} / ${(p.lon ?: 0.0).coordenada()}")
                }
                CartaoInfo(titulo = "Solo do ponto") {
                    val r = analise.resultado
                    if (r == null) EstadoDaTela(analise.carregando, analise.demorando, analise.mensagem,
                        { if (p.lat != null && p.lon != null) analise.tentarDeNovo(p.lat, p.lon) })
                    else {
                        Text(r.soloLocal?.tipoSolo ?: "Solo não identificado", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth()) {
                            Dado("pH", r.phExibido()?.formatar() ?: "—", Modifier.weight(1f))
                            Dado("Chuva anual", "${(r.chuva?.chuvaTotalAnualMm ?: 0.0).formatar()} mm", Modifier.weight(1f))
                        }
                    }
                }
                BotaoPrincipal("Calcular frete", aoFrete)
                BotaoSecundario("Ver no mapa", { abrirRotaNoMapa(context, p.lat ?: 0.0, p.lon ?: 0.0, p.nome ?: "Plantio") })
                BotaoSecundario("Editar", aoEditar)
            }
        }
    }
}
