package com.example.agroanalyst.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = Laranja, onPrimary = SobreLaranja,
    primaryContainer = MapaFundo, onPrimaryContainer = Color(0xFF7C2D12),
    secondary = Agua, onSecondary = SobreAgua,
    secondaryContainer = SeloFundo, onSecondaryContainer = SeloTexto,
    background = Papel, onBackground = Tinta,
    surface = Cartao, onSurface = Tinta,
    surfaceVariant = MapaFundo, onSurfaceVariant = TintaSuave,
    outline = Contorno, outlineVariant = Regra,
    error = Baixa, onError = SobreErro
)

@Composable
fun AgroTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = EsquemaClaro, typography = Tipografia, shapes = Formas, content = content)
}
