package com.example.agroanalyst.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

enum class EstadoPermissao { NaoPedida, NegadaUmaVez, NegadaPermanente }

class Posicionador(val solicitar: () -> Unit, val estado: EstadoPermissao, val aviso: String?)

private val PERMISSOES = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

private fun Context.atividade(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.atividade()
    else -> null
}

@Composable
fun rememberPosicionador(aoPosicao: (Double, Double) -> Unit): Posicionador {
    val context = LocalContext.current
    var estado by rememberSaveable { mutableStateOf(EstadoPermissao.NaoPedida) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    val aoPosicaoAtual by rememberUpdatedState(aoPosicao)

    fun buscar() {
        try {
            LocationServices.getFusedLocationProviderClient(context)
                .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    if (loc != null) { aviso = null; aoPosicaoAtual(loc.latitude, loc.longitude) }
                    else aviso = "Não foi possível obter a posição. Digite a coordenada."
                }
                .addOnFailureListener { aviso = "Não foi possível obter a posição. Digite a coordenada." }
        } catch (e: SecurityException) {
            aviso = "Sem permissão de localização. Digite a coordenada."
        }
    }

    val pedir = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        if (r.values.any { it }) { estado = EstadoPermissao.NaoPedida; buscar() }
        else {
            val ativ = context.atividade()
            estado = if (ativ != null && PERMISSOES.any { ActivityCompat.shouldShowRequestPermissionRationale(ativ, it) })
                EstadoPermissao.NegadaUmaVez else EstadoPermissao.NegadaPermanente
        }
    }

    val solicitar = {
        val concedida = PERMISSOES.any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
        if (concedida) buscar() else pedir.launch(PERMISSOES)
    }
    return Posicionador(solicitar, estado, aviso)
}
