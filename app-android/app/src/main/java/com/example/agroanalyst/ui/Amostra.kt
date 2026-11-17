package com.example.agroanalyst.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.agroanalyst.ui.theme.Laranja
import com.example.agroanalyst.ui.theme.MapaFundo

@Composable
fun Amostra() {
    var texto by remember { mutableStateOf("") }
    var chip by remember { mutableStateOf(true) }
    Column(Modifier.verticalScroll(rememberScrollState())) {
        BarraTopo("Componentes", sobreMapa = true)
        MapaContorno()
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CampoTexto(texto, { texto = it }, "Campo")
            BotaoPrincipal("Principal", {})
            BotaoSecundario("Secundário", {})
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipCultura("Soja", chip) { chip = true }
                ChipCultura("Milho", !chip) { chip = false }
                Selo("Adequada")
            }
            CartaoPreco("Soja", "R$ 167,43", "R$/sc", 0.82, "17/11")
            CartaoInfo(titulo = "Gráficos") {
                GraficoChuva(listOf("J" to 40.0, "A" to 53.0, "S" to 23.0, "O" to 88.0, "N" to 208.0, "D" to 216.0, "J" to 316.0, "F" to 122.0, "M" to 85.0, "A" to 31.0, "M" to 12.0, "J" to 0.0))
                Spacer(Modifier.height(16.dp))
                GraficoLinha(listOf(1.0, 3.0, 2.0, 4.0, 3.5), "início", "fim")
            }
            Legenda(listOf(MapaFundo to "faixa 1", Laranja to "faixa 5"))
        }
    }
}
