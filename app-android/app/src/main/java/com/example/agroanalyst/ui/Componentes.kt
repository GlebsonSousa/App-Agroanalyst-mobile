package com.example.agroanalyst.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.agroanalyst.ui.theme.*
import kotlin.math.sin

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
        border = BorderStroke(1.dp, if (perigo) cor else MaterialTheme.colorScheme.outline),
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

@Composable
fun Dado(rotulo: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(rotulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}

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
        Text(titulo, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        acoes()
    }
}

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
            if (aoTentarDeNovo != null) TextButton(colors = CoresTexto, onClick = aoTentarDeNovo, modifier = Modifier.heightIn(min = 48.dp)) { Text("Tentar de novo") }
        }
    }
}

@Composable
fun ChipCultura(texto: String, selecionado: Boolean, aoClicar: () -> Unit, papel: Role = Role.RadioButton) {
    val cor = if (selecionado) MaterialTheme.colorScheme.primary else Color.Transparent
    Box(
        Modifier.heightIn(min = 48.dp)
            .selectable(selected = selecionado, onClick = aoClicar, role = papel)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto,
            style = MaterialTheme.typography.labelLarge,
            color = if (selecionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .background(cor, RoundedCornerShape(50))
                .border(1.dp, if (selecionado) cor else MaterialTheme.colorScheme.outline, RoundedCornerShape(50))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun CartaoPreco(nome: String, valor: String, unidade: String, variacao: Double?, data: String, modifier: Modifier = Modifier) {
    Column(
        modifier.size(width = 160.dp, height = 112.dp)
            .shadow(4.dp, MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(nome, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        Text(valor, style = MaterialTheme.typography.titleLarge, maxLines = 1)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (variacao != null) {
                val cor = if (variacao >= 0) Alta else Baixa
                Icon(if (variacao >= 0) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown, null, tint = cor, modifier = Modifier.size(24.dp))
                Text(variacao.variacao(), style = MaterialTheme.typography.labelMedium, color = cor)
            }
        }
        Text("$unidade · $data", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
fun MapaContorno(modifier: Modifier = Modifier, altura: Dp = 168.dp) {
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
        val tracejado = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 5.dp.toPx()))
        drawLine(MapaCerca, Offset(w * 0.28f, 0f), Offset(w * 0.31f, h), strokeWidth = 1.25.dp.toPx(), pathEffect = tracejado)
        drawLine(MapaCerca, Offset(w * 0.70f, 0f), Offset(w * 0.67f, h), strokeWidth = 1.25.dp.toPx(), pathEffect = tracejado)
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

@Composable
fun GraficoChuva(meses: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    val maximo = meses.maxOfOrNull { it.second } ?: 0.0
    val resumo = meses.maxByOrNull { it.second }?.let { "Maior chuva em ${it.first}, ${it.second.formatar()} mm" } ?: "Sem dados"
    Column(modifier.fillMaxWidth().semantics { contentDescription = "Gráfico de chuva mensal. $resumo" }) {
        Row(Modifier.fillMaxWidth().height(64.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
            meses.forEach { (_, mm) ->
                val frac = if (maximo > 0) (mm / maximo).toFloat() else 0f
                Box(
                    Modifier.weight(1f).fillMaxHeight(frac).background(
                        if (mm == maximo && mm > 0) MaterialTheme.colorScheme.primary else BarraChuva,
                        RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                    )
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            meses.forEach { (nome, _) ->
                Text(nome.take(1).uppercase(), Modifier.weight(1f), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun GraficoLinha(valores: List<Double>, rotuloInicio: String, rotuloFim: String, modifier: Modifier = Modifier) {
    val cor = MaterialTheme.colorScheme.primary
    val resumo = if (valores.isEmpty()) "Sem dados" else "De ${valores.first().formatar(2)} a ${valores.last().formatar(2)}"
    Column(modifier.fillMaxWidth().semantics { contentDescription = "Gráfico de preço. $resumo" }) {
        Canvas(Modifier.fillMaxWidth().height(120.dp)) {
            if (valores.size < 2) return@Canvas
            val min = valores.min()
            val max = valores.max()
            val faixa = if (max - min < 1e-9) 1.0 else max - min
            val passo = size.width / (valores.size - 1)
            fun y(v: Double) = (size.height - ((v - min) / faixa * size.height * 0.9 + size.height * 0.05)).toFloat()
            val linha = Path().apply { valores.forEachIndexed { i, v -> if (i == 0) moveTo(0f, y(v)) else lineTo(i * passo, y(v)) } }
            val area = Path().apply {
                addPath(linha)
                lineTo(size.width, size.height); lineTo(0f, size.height); close()
            }
            drawPath(area, cor.copy(alpha = 0.12f))
            drawPath(linha, cor, style = Stroke(2.dp.toPx()))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(rotuloInicio, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(rotuloFim, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun Legenda(faixas: List<Pair<Color, String>>, modifier: Modifier = Modifier) {
    Column(modifier) {
        faixas.forEach { (cor, texto) ->
            Row(Modifier.heightIn(min = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(16.dp).background(cor, RoundedCornerShape(4.dp)).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp)))
                Spacer(Modifier.width(8.dp))
                Text(texto, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun SelecaoRadio(titulo: String, opcoes: List<Pair<String, String>>, selecionado: String, aoSelecionar: (String) -> Unit) {
    Column {
        Text(titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        opcoes.forEach { (id, texto) ->
            Row(
                Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    .selectable(selected = id == selecionado, onClick = { aoSelecionar(id) }, role = Role.RadioButton),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = id == selecionado, onClick = null)
                Spacer(Modifier.width(12.dp))
                Text(texto, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun MiniMapaBrasil(
    ufs: List<com.example.agroanalyst.data.UfPoligono>,
    valores: Map<String, Double?>,
    cortes: List<Double>,
    aoToque: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vazio = MaterialTheme.colorScheme.outlineVariant
    val contorno = MaterialTheme.colorScheme.surface
    val cores = faixasDeCor()
    Canvas(
        modifier.fillMaxWidth().height(240.dp).clickable(onClickLabel = "Abrir o mapa", onClick = aoToque)
            .semantics { contentDescription = "Mapa do Brasil colorido pela produção por estado. Toque para abrir o mapa." }
    ) {
        val minLon = -74.0; val maxLon = -34.0; val minLat = -34.0; val maxLat = 5.5
        val escalaX = 0.97
        val largura = (maxLon - minLon) * escalaX
        val altura = maxLat - minLat
        val k = minOf(size.width / largura, size.height / altura).toFloat()
        val dx = (size.width - (largura * k).toFloat()) / 2f
        fun x(lon: Double) = dx + ((lon - minLon) * escalaX * k).toFloat()
        fun y(lat: Double) = ((maxLat - lat) * k).toFloat()
        ufs.forEach { uf ->
            val faixa = faixaDe(valores[uf.codigo], cortes)
            val cor = if (faixa < 0) vazio else cores[faixa]
            uf.aneis.forEach { anel ->
                val caminho = Path()
                anel.forEachIndexed { i, (lon, lat) -> if (i == 0) caminho.moveTo(x(lon), y(lat)) else caminho.lineTo(x(lon), y(lat)) }
                caminho.close()
                drawPath(caminho, cor)
                drawPath(caminho, contorno, style = Stroke(0.75.dp.toPx()))
            }
        }
    }
}

@Composable
fun faixasDeCor(): List<Color> {
    val de = MaterialTheme.colorScheme.surfaceVariant
    val ate = MaterialTheme.colorScheme.primary
    return (0 until 5).map { androidx.compose.ui.graphics.lerp(de, ate, it / 4f) }
}

val CoresTexto @Composable get() = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
