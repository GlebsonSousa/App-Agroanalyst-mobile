package com.example.agroanalyst.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.agroanalyst.viewmodel.AnaliseViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

private enum class Permissao { NaoPedida, NegadaUmaVez, NegadaPermanente }

private fun Context.activity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}

private val PERMISSOES = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

@Composable
fun AnaliseMapaTela(vm: AnaliseViewModel, aoAnalisar: (Double, Double) -> Unit, aoSair: () -> Unit) {
    val context = LocalContext.current
    var lat by rememberSaveable { mutableStateOf("") }
    var lon by rememberSaveable { mutableStateOf("") }
    var permissao by rememberSaveable { mutableStateOf(Permissao.NaoPedida) }
    var avisoGps by rememberSaveable { mutableStateOf<String?>(null) }

    fun buscarPosicao() {
        try {
            LocationServices.getFusedLocationProviderClient(context)
                .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    if (loc != null) { lat = loc.latitude.toString(); lon = loc.longitude.toString(); avisoGps = null }
                    else avisoGps = "Não foi possível obter a posição. Digite a coordenada."
                }
                .addOnFailureListener { avisoGps = "Não foi possível obter a posição. Digite a coordenada." }
        } catch (e: SecurityException) {
            avisoGps = "Sem permissão de localização. Digite a coordenada."
        }
    }

    val pedir = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        if (r.values.any { it }) { permissao = Permissao.NaoPedida; buscarPosicao() }
        else {
            val ativ = context.activity()
            permissao = if (ativ != null && PERMISSOES.any { ActivityCompat.shouldShowRequestPermissionRationale(ativ, it) })
                Permissao.NegadaUmaVez else Permissao.NegadaPermanente
        }
    }

    fun usarMinhaPosicao() {
        val ja = PERMISSOES.any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
        if (ja) buscarPosicao() else pedir.launch(PERMISSOES)
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        BarraTopo("Análise de solo", sobreMapa = true, acoes = {
            IconButton(onClick = aoSair, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Filled.Logout, "Sair da conta") }
        })
        MapaContorno()
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Escolha o ponto", style = MaterialTheme.typography.headlineSmall)
            Text("Use a sua posição ou digite a coordenada do talhão.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

            BotaoSecundario("Usar minha posição", ::usarMinhaPosicao)
            when (permissao) {
                Permissao.NegadaUmaVez -> Text(
                    "A posição só é usada para preencher a coordenada. Toque em \"Usar minha posição\" para tentar de novo ou digite a coordenada.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Permissao.NegadaPermanente -> Text(
                    "A permissão de localização foi negada. Digite a latitude e a longitude abaixo (ou libere nas configurações do aparelho).",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Permissao.NaoPedida -> {}
            }
            avisoGps?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error) }

            CampoTexto(lat, { lat = it }, "Latitude (ex.: -23.67)", tipo = KeyboardType.Decimal)
            CampoTexto(lon, { lon = it }, "Longitude (ex.: -46.46)", tipo = KeyboardType.Decimal)
            vm.mensagem?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error) }
            BotaoPrincipal("Analisar ponto", { vm.coordenadas(lat, lon)?.let { (a, o) -> aoAnalisar(a, o) } })
        }
    }
}
