package com.example.agroanalyst.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.agroanalyst.viewmodel.AnaliseViewModel
import com.example.agroanalyst.viewmodel.InicioViewModel
import com.google.gson.JsonElement

private fun JsonElement.texto(): String = when {
    isJsonNull -> "—"
    isJsonPrimitive -> asString.trim()
    isJsonArray -> asJsonArray.joinToString(" – ") { it.texto() }
    else -> asJsonObject.entrySet().joinToString("; ") { "${it.key.humanizar()}: ${it.value.texto()}" }
}

@Composable
fun CulturaDetalheTela(nome: String, analise: AnaliseViewModel, inicio: InicioViewModel, aoVoltar: () -> Unit) {
    val dados = analise.resultado?.culturasDb?.get(nome)
    val chave = culturaDoMercado(nome)
    val preco = inicio.precos.firstOrNull { it.cultura == chave }
    val topo = inicio.top[chave].orEmpty()

    Column(Modifier.fillMaxSize()) {
        BarraTopo(nome, aoVoltar)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CartaoInfo(titulo = "Faixas ideais e requisitos") {
                if (dados == null) Text("Disponível depois de uma análise de solo que recomende esta cultura.", style = MaterialTheme.typography.bodyMedium)
                else dados.entrySet().forEachIndexed { i, (campo, valor) ->
                    if (i > 0) Divisoria()
                    Dado(campo.humanizar(), valor.texto(), Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(vertical = 8.dp))
                }
            }
            CartaoInfo(titulo = "Preço atual") {
                if (preco == null) Text("Sem cotação do CEPEA para esta cultura.", style = MaterialTheme.typography.bodyMedium)
                else {
                    Text("${(preco.valor ?: 0.0).formatar(2)} ${preco.unidade}", style = MaterialTheme.typography.titleLarge)
                    Text("Fonte: ${preco.fonte} · ${(preco.data ?: "").dataCurta()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (topo.isNotEmpty()) CartaoInfo(titulo = "Maiores municípios produtores") {
                topo.forEachIndexed { i, m ->
                    if (i > 0) Divisoria()
                    Text("${i + 1}. ${m.municipio} · ${m.uf}", Modifier.heightIn(min = 48.dp).padding(vertical = 12.dp), style = MaterialTheme.typography.bodyLarge)
                }
                Text("Fonte: IBGE/SIDRA · ${topo.first().ano}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
