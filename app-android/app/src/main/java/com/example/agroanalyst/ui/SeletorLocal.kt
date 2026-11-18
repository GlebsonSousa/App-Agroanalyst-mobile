package com.example.agroanalyst.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.agroanalyst.viewmodel.BuscaViewModel

private fun numero(t: String) = t.trim().replace(',', '.').toDoubleOrNull()

/**
 * Local por nome de cidade (com sugestões enquanto digita) ou por coordenadas.
 * O local escolhido aparece sempre num quadro próprio, com nome e latitude/longitude.
 */
@Composable
fun SeletorLocal(
    id: String,
    rotulo: String,
    busca: BuscaViewModel,
    lat: String,
    lon: String,
    nome: String,
    modifier: Modifier = Modifier,
    permitirCoordenadas: Boolean = true,
    aoEscolher: (lat: String, lon: String, nome: String) -> Unit
) {
    var consulta by rememberSaveable(id) { mutableStateOf("") }
    var manual by rememberSaveable(id) { mutableStateOf(false) }
    val la = numero(lat)
    val lo = numero(lon)
    val escolhido = la != null && lo != null

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(rotulo, style = MaterialTheme.typography.titleMedium)
        CampoTexto(consulta, { consulta = it; busca.buscar(id, it) }, "Buscar cidade")
        if (busca.campo == id) {
            if (busca.buscando) Text("Buscando…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            busca.mensagem?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error) }
            busca.sugestoes.forEachIndexed { i, c ->
                if (i > 0) Divisoria()
                Column(
                    Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .clickable {
                            val rotuloCidade = listOfNotNull(c.nome, c.uf).joinToString(", ")
                            aoEscolher((c.lat ?: 0.0).toString(), (c.lon ?: 0.0).toString(), rotuloCidade)
                            consulta = ""
                            busca.limpar()
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Text(listOfNotNull(c.nome, c.uf).joinToString(", "), style = MaterialTheme.typography.bodyLarge)
                    Text("Lat ${(c.lat ?: 0.0).coordenada()} · Lon ${(c.lon ?: 0.0).coordenada()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Column(
            Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium).padding(12.dp)
        ) {
            Text("Local escolhido", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                when {
                    nome.isNotBlank() -> nome
                    escolhido -> "Coordenadas digitadas"
                    else -> "Nenhum local escolhido"
                },
                style = MaterialTheme.typography.titleMedium
            )
            if (escolhido) Text("Latitude ${la!!.coordenada()} · Longitude ${lo!!.coordenada()}", style = MaterialTheme.typography.bodyMedium)
        }
        if (permitirCoordenadas) {
            TextButton(colors = CoresTexto, onClick = { manual = !manual }, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(if (manual) "Ocultar coordenadas" else "Digitar coordenadas")
            }
            if (manual) {
                CampoTexto(lat, { aoEscolher(it, lon, "") }, "Latitude (ex.: -23.67)", tipo = KeyboardType.Decimal)
                CampoTexto(lon, { aoEscolher(lat, it, "") }, "Longitude (ex.: -46.46)", tipo = KeyboardType.Decimal)
            }
        }
    }
}
