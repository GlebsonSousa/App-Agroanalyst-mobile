package com.example.agroanalyst.data

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Consome o Content Provider do Photo Picker (content://): nenhuma permissão de armazenamento é necessária,
 * o seletor roda fora do processo e entrega leitura só daquele item.
 */
object FotoUpload {
    suspend fun paraParte(resolver: ContentResolver, uri: Uri): MultipartBody.Part {
        // Leitura de vários MB fora da thread principal.
        val bytes = withContext(Dispatchers.IO) {
            resolver.openInputStream(uri)?.use { it.readBytes() } ?: throw IllegalStateException("Não foi possível ler a foto")
        }
        val tipo = resolver.getType(uri) ?: "image/jpeg"
        val nome = resolver.query(uri, null, null, null, null)?.use { c ->
            val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (i >= 0 && c.moveToFirst()) c.getString(i) else null
        } ?: "foto.jpg"
        return MultipartBody.Part.createFormData("foto", nome, bytes.toRequestBody(tipo.toMediaTypeOrNull()))
    }
}
