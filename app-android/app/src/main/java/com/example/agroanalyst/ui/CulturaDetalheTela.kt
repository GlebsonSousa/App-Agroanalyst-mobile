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
import com.google.gson.JsonElement

/** Cada cultura de culturas_db tem esquema próprio: exibe todos os campos presentes, sem depender de nomes fixos. */
private fun JsonElement.texto(): String = when {
    isJsonNull -> "—"
    isJsonPrimitive -> asString.trim()
    isJsonArray -> asJsonArray.joinToString(" – ") { it.texto() }
    else -> asJsonObject.entrySet().joinToString("; ") { "${it.key.humanizar()}: ${it.value.texto()}" }
}

@Composable
fun CulturaDetalheTela(nome: String, vm: AnaliseViewModel, aoVoltar: () -> Unit) {
    val dados = vm.resultado?.culturasDb?.get(nome)
    Column(Modifier.fillMaxSize()) {
        BarraTopo(nome, aoVoltar)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (dados == null) {
                Text("Detalhes indisponíveis. Volte e refaça a análise do ponto.", style = MaterialTheme.typography.bodyLarge)
            } else {
                CartaoInfo(titulo = "Faixas ideais e requisitos") {
                    dados.entrySet().forEachIndexed { i, (chave, valor) ->
                        if (i > 0) Divisoria()
                        Dado(chave.humanizar(), valor.texto(), Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}
