package com.example.agroanalyst.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.agroanalyst.data.FreteEnvio
import com.example.agroanalyst.viewmodel.LogisticaViewModel
import com.example.agroanalyst.viewmodel.PlantiosViewModel

private val MODAIS = listOf("rodoviario" to "Rodoviário", "ferroviario" to "Ferroviário", "hidroviario" to "Hidroviário")
private const val PRIORIDADE_PADRAO = 50 // campo exigido pelo contrato da API; sem controle na tela

@Composable
fun FreteTela(vm: LogisticaViewModel, plantios: PlantiosViewModel, aoVoltar: () -> Unit, aoVenda: () -> Unit) {
    LaunchedEffect(Unit) { vm.limparResultado(); vm.carregarOpcoes(); plantios.carregarLista() }

    var plantioId by rememberSaveable { mutableStateOf(vm.plantioPreSelecionado.orEmpty()) }
    var destinoId by rememberSaveable { mutableStateOf("") }
    var modal by rememberSaveable { mutableStateOf("rodoviario") }
    var sacas by rememberSaveable { mutableStateOf("") }
    var preco by rememberSaveable { mutableStateOf("") }
    var capacidade by rememberSaveable { mutableStateOf("30") }
    var kmL by rememberSaveable { mutableStateOf("2.5") }
    var eixos by rememberSaveable { mutableStateOf("6") }
    var erroLocal by rememberSaveable { mutableStateOf<String?>(null) }

    fun montar(): FreteEnvio? {
        val s = sacas.trim().toIntOrNull()
        val p = preco.trim().replace(',', '.').toDoubleOrNull()
        val cap = capacidade.trim().replace(',', '.').toDoubleOrNull()
        val km = kmL.trim().replace(',', '.').toDoubleOrNull()
        val ex = eixos.trim().toIntOrNull()
        val rodo = modal == "rodoviario"
        erroLocal = when {
            plantioId.isEmpty() -> "Escolha o plantio."
            destinoId.isEmpty() -> "Escolha o destino."
            s == null || s <= 0 || p == null || p <= 0 -> "Informe sacas e preço maiores que zero."
            rodo && (cap == null || cap <= 0 || km == null || km <= 0 || ex == null || ex <= 0) -> "Informe capacidade, km/L e eixos do caminhão."
            else -> null
        }
        if (erroLocal != null) return null
        return FreteEnvio(plantioId, destinoId, s!!, p!!, modal, if (rodo) cap!! else 0.0, if (rodo) km else null, if (rodo) ex else null, PRIORIDADE_PADRAO)
    }

    Column(Modifier.fillMaxSize()) {
        BarraTopo("Calcular frete", aoVoltar)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SelecaoRadio("Plantio", plantios.lista.map { (it.id ?: "") to (it.nome ?: "—") }, plantioId) { plantioId = it }
            if (plantios.lista.isEmpty() && !plantios.carregando) Text("Cadastre um plantio antes de calcular o frete.", style = MaterialTheme.typography.bodyMedium)

            SelecaoRadio("Destino", vm.destinos.map { (it.id ?: "") to "${it.nome} — ${it.cidade}/${it.uf}" }, destinoId) { id ->
                destinoId = id
                vm.destinos.firstOrNull { it.id == id }?.precoSaca?.let { preco = it.toString() } // sugestão editável
            }
            SelecaoRadio("Modal", MODAIS, modal) { modal = it }

            CampoTexto(sacas, { sacas = it }, "Sacas", tipo = KeyboardType.Number)
            CampoTexto(preco, { preco = it }, "Preço por saca (R$)", tipo = KeyboardType.Decimal)
            if (modal == "rodoviario") {
                CampoTexto(capacidade, { capacidade = it }, "Capacidade do caminhão (t)", tipo = KeyboardType.Decimal)
                CampoTexto(kmL, { kmL = it }, "Consumo (km/L)", tipo = KeyboardType.Decimal)
                CampoTexto(eixos, { eixos = it }, "Eixos", tipo = KeyboardType.Number)
            }
            erroLocal?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error) }
            EstadoDaTela(vm.carregando, vm.demorando, vm.mensagem)
            BotaoPrincipal("Calcular", { montar()?.let(vm::calcular) }, carregando = vm.carregando)

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
                    Dado("Líquido por saca", (f.liquidoSaca ?: 0.0).reais())
                    f.viagens?.let { Dado("Viagens", it.toString(), Modifier.padding(top = 12.dp)) }
                    Text("Distância em linha reta × 1,3 (estimativa, não é rota real).", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
                }
                BotaoPrincipal("Registrar venda", { montar()?.let { vm.registrarVenda(it, aoVenda) } }, carregando = vm.carregando)
            }
        }
    }
}
