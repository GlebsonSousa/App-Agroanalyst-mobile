package com.example.agroanalyst.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun abrirNoMapa(context: Context, lat: Double, lon: Double, nome: String) {
    val uri = Uri.parse("geo:0,0?q=$lat,$lon(${Uri.encode(nome)})")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Nenhum app de mapas encontrado neste aparelho.", Toast.LENGTH_LONG).show()
    }
}

fun compartilharTexto(context: Context, texto: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Compartilhar"))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Nenhum app para compartilhar encontrado.", Toast.LENGTH_LONG).show()
    }
}
