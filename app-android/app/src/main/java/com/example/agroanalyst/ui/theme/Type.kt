package com.example.agroanalyst.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.agroanalyst.R

val Outfit = FontFamily(
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_medium, FontWeight.Medium),
    Font(R.font.outfit_semibold, FontWeight.SemiBold),
    Font(R.font.outfit_bold, FontWeight.Bold)
)

private fun estilo(tam: Int, alt: Int, peso: FontWeight, tracking: Boolean = false) = TextStyle(
    fontFamily = Outfit, fontSize = tam.sp, lineHeight = alt.sp, fontWeight = peso,
    letterSpacing = if (tracking) (-0.02).em else 0.em
)

val Tipografia = Typography(
    displaySmall = estilo(36, 44, FontWeight.Bold, true),
    headlineSmall = estilo(24, 30, FontWeight.Bold, true),
    titleLarge = estilo(20, 26, FontWeight.Bold, true),
    titleMedium = estilo(16, 22, FontWeight.SemiBold),
    bodyLarge = estilo(16, 24, FontWeight.Normal),
    bodyMedium = estilo(14, 20, FontWeight.Normal),
    labelLarge = estilo(14, 20, FontWeight.SemiBold),
    labelMedium = estilo(12, 16, FontWeight.Medium),
    labelSmall = estilo(12, 16, FontWeight.Normal)
)
