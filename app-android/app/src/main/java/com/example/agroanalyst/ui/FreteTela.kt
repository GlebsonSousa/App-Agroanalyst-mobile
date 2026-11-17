package com.example.agroanalyst.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.agroanalyst.viewmodel.FreteViewModel
import com.example.agroanalyst.viewmodel.PlantiosViewModel

private val MODAIS = listOf("rodoviario" to "Rodoviário", "ferroviario" to "Ferroviário", "hidroviario" to "Hidroviário")
private val GRAOS = listOf("soja" to "Soja", "milho" to "Milho")

@Composable
fun FreteTela(vm: FreteViewModel, plantios: PlantiosViewModel, logado: Boolean, aoPedirLogin: () -> Unit, aoVoltar: () -> Unit) {
    LaunchedEffect(Unit) {
        vm.carregarPracas()
        if (logado) plantios.carregarLista()
    }
    val r = vm.rascunho

    Column(Modifier.fillMaxSize()) {
        BarraTopo("Calcular frete", aoVoltar)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (logado && plantios.lista.isNotEmpty()) {
                SelecaoRadio("Plantio de origem", plantios.lista.map { (it.id ?: "") to (it.nome ?: "—") }, r.plantioId) { id ->
                    val p = plantios.lista.firstOrNull { it.id == id }
                    vm.atualizarRascunho(r.copy(plantioId = id, origemLat = p?.lat?.toString().orEmpty(), origemLon = p?.lon?.toString().orEmpty()))
                }
            }
            CampoTexto(r.origemLat, { vm.atualizarRascunho(r.copy(origemLat = it)) }, "Latitude de origem", tipo = KeyboardType.Decimal)
            CampoTexto(r.origemLon, { vm.atualizarRascunho(r.copy(origemLon = it)) }, "Longitude de origem", tipo = KeyboardType.Decimal)
            SelecaoRadio("Destino", vm.pracas.map { (it.nome ?: "") to "${it.nome} · ${it.uf}" }, r.destino) { vm.atualizarRascunho(r.copy(destino = it)) }
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
                        Dado("Distância estimada", "${(f.distanciaKm ?: 0.0).formatar()} km", Modifier.weight(1f))
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
                    Text("Distância em linha reta × 1,3 (estimativa, não é rota real).", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
                }
                if (vm.salvo) Text("Frete salvo no plantio.", style = MaterialTheme.typography.bodyLarge)
                else if (!logado) BotaoSecundario("Entrar para salvar", aoPedirLogin)
                else BotaoPrincipal("Salvar no plantio", vm::salvar, carregando = vm.carregando)
            }
        }
    }
}
