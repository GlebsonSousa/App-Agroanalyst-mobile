package com.example.agroanalyst.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.agroanalyst.ui.theme.*
import kotlin.math.sin

/** Campo sem estado próprio (state hoisting): quem usa guarda o valor. Rótulo sempre visível. */
@Composable
fun CampoTexto(
    valor: String,
    aoMudar: (String) -> Unit,
    rotulo: String,
    modifier: Modifier = Modifier,
    tipo: KeyboardType = KeyboardType.Text,
    senha: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = aoMudar,
        label = { Text(rotulo) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = tipo),
        visualTransformation = if (senha) PasswordVisualTransformation() else VisualTransformation.None,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.onSurface,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp)
    )
}

@Composable
fun BotaoPrincipal(texto: String, aoClicar: () -> Unit, modifier: Modifier = Modifier, carregando: Boolean = false, habilitado: Boolean = true) {
    Button(
        onClick = aoClicar,
        enabled = habilitado && !carregando,
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp)
    ) {
        if (carregando) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
        else Text(texto, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun BotaoSecundario(texto: String, aoClicar: () -> Unit, modifier: Modifier = Modifier, perigo: Boolean = false) {
    val cor = if (perigo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    OutlinedButton(
        onClick = aoClicar,
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (perigo) cor else MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = cor),
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp)
    ) { Text(texto, style = MaterialTheme.typography.titleMedium) }
}

@Composable
fun CartaoInfo(modifier: Modifier = Modifier, titulo: String? = null, conteudo: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .shadow(4.dp, MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
            .padding(16.dp)
    ) {
        if (titulo != null) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
        }
        conteudo()
    }
}

@Composable
fun Selo(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
fun Divisoria() = HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

/** Rótulo pequeno em cima, valor embaixo (grade de dados do solo, números de frete). */
@Composable
fun Dado(rotulo: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(rotulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}

/** Barra de 56 dp: título, voltar opcional e ações; 48 dp por alvo de toque. */
@Composable
fun BarraTopo(
    titulo: String,
    aoVoltar: (() -> Unit)? = null,
    sobreMapa: Boolean = false,
    acoes: @Composable RowScope.() -> Unit = {}
) {
    Row(
        Modifier.fillMaxWidth()
            .background(if (sobreMapa) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (aoVoltar != null) IconButton(onClick = aoVoltar, modifier = Modifier.size(48.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
        } else Spacer(Modifier.width(12.dp))
        Text(titulo, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        acoes()
    }
}

/** Carregando (com aviso de cold start) ou erro com "tentar de novo". Não desenha nada quando está tudo bem. */
@Composable
fun EstadoDaTela(carregando: Boolean, demorando: Boolean, mensagem: String?, aoTentarDeNovo: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (carregando) {
            CircularProgressIndicator(Modifier.padding(8.dp))
            if (demorando) Text(
                "Acordando o servidor, isso pode levar até um minuto…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (mensagem != null) {
            Text(mensagem, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            if (aoTentarDeNovo != null) TextButton(onClick = aoTentarDeNovo, modifier = Modifier.heightIn(min = 48.dp)) { Text("Tentar de novo") }
        }
    }
}

/** Mapa generativo (não é tile nem satélite): curvas de nível, curso d'água, divisas, talhão e pino. */
@Composable
fun MapaContorno(modifier: Modifier = Modifier, altura: androidx.compose.ui.unit.Dp = 168.dp) {
    Canvas(modifier.fillMaxWidth().height(altura).background(MapaFundo)) {
        val w = size.width
        val h = size.height
        fun onda(y0: Float, amp: Float, fase: Float) = Path().apply {
            moveTo(0f, y0)
            var x = 0f
            while (x <= w) { lineTo(x, y0 + amp * sin(x / w * 6.2832f + fase)); x += 8f }
        }
        for (i in 0 until 6) drawPath(onda(h * (0.1f + i * 0.15f), 8.dp.toPx(), i * 0.35f), MapaContornoCor, style = Stroke(1.dp.toPx()))
        drawPath(onda(h * 0.78f, 10.dp.toPx(), 0.8f), Agua.copy(alpha = 0.75f), style = Stroke(2.5.dp.toPx()))
        val tracejado = Stroke(1.25.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 5.dp.toPx())))
        drawLine(MapaCerca, Offset(w * 0.28f, 0f), Offset(w * 0.31f, h), strokeWidth = tracejado.width, pathEffect = tracejado.pathEffect)
        drawLine(MapaCerca, Offset(w * 0.70f, 0f), Offset(w * 0.67f, h), strokeWidth = tracejado.width, pathEffect = tracejado.pathEffect)
        val talhao = Path().apply {
            moveTo(w * 0.30f, h * 0.22f); lineTo(w * 0.70f, h * 0.18f)
            lineTo(w * 0.72f, h * 0.62f); lineTo(w * 0.32f, h * 0.66f); close()
        }
        drawPath(talhao, Laranja, style = Stroke(2.dp.toPx()))
        val c = Offset(w / 2, h * 0.42f)
        drawCircle(Color.Black.copy(alpha = 0.2f), 12.dp.toPx(), c + Offset(0f, 2.dp.toPx()))
        drawCircle(Color.White, 12.dp.toPx(), c)
        drawCircle(Laranja, 9.dp.toPx(), c)
    }
}

/** 12 barras; a maior em laranja; mês sem chuva fica sem barra, mas com rótulo. */
@Composable
fun GraficoChuva(meses: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    val maximo = meses.maxOfOrNull { it.second } ?: 0.0
    val resumo = meses.maxByOrNull { it.second }?.let { "Maior chuva em ${it.first}, ${it.second.formatar()} mm" } ?: "Sem dados"
    Column(modifier.fillMaxWidth().semantics { contentDescription = "Gráfico de chuva mensal. $resumo" }) {
        Row(Modifier.fillMaxWidth().height(64.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
            meses.forEach { (_, mm) ->
                val frac = if (maximo > 0) (mm / maximo).toFloat() else 0f
                Box(Modifier.weight(1f).fillMaxHeight(frac).background(
                    if (mm == maximo && mm > 0) MaterialTheme.colorScheme.primary else BarraChuva,
                    RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                ))
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            meses.forEach { (nome, _) ->
                Text(nome.take(1).uppercase(), Modifier.weight(1f), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

/** Lista de opções com rádio; cada linha tem pelo menos 48 dp. */
@Composable
fun SelecaoRadio(titulo: String, opcoes: List<Pair<String, String>>, selecionado: String, aoSelecionar: (String) -> Unit) {
    Column {
        Text(titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        opcoes.forEach { (id, texto) ->
            Row(
                Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    .selectable(selected = id == selecionado, onClick = { aoSelecionar(id) }, role = androidx.compose.ui.semantics.Role.RadioButton),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = id == selecionado, onClick = null)
                Spacer(Modifier.width(12.dp))
                Text(texto, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
