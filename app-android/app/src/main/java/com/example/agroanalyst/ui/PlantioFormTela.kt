package com.example.agroanalyst.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.agroanalyst.data.ApiClient
import com.example.agroanalyst.viewmodel.BuscaViewModel
import com.example.agroanalyst.viewmodel.PlantiosViewModel

@Composable
fun PlantioFormTela(vm: PlantiosViewModel, busca: BuscaViewModel, id: String?, aoVoltar: () -> Unit, aoSalvar: () -> Unit) {
    val context = LocalContext.current
    val r = vm.rascunho
    LaunchedEffect(id) { if (id != null) vm.abrirEdicao(id) }

    val sair = { vm.descartar(); aoVoltar() }
    BackHandler(onBack = sair)

    val escolher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.atualizarRascunho(vm.rascunho.copy(fotoUri = uri))
    }

    Column(Modifier.fillMaxSize()) {
        BarraTopo(if (id == null) "Novo plantio" else "Editar plantio", sair)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val foto: Any? = r.fotoUri ?: r.fotoId?.let(ApiClient::urlFoto)
            if (foto != null) AsyncImage(foto, contentDescription = "Prévia da foto do plantio", contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(160.dp).clip(MaterialTheme.shapes.large))
            BotaoSecundario(if (foto == null) "Adicionar foto" else "Trocar foto",
                { escolher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) })
            if (foto != null) BotaoSecundario("Remover foto", { vm.atualizarRascunho(r.copy(fotoUri = null, fotoId = null)) })

            CampoTexto(r.nome, { vm.atualizarRascunho(r.copy(nome = it)) }, "Nome do plantio")
            CampoTexto(r.cultura, { vm.atualizarRascunho(r.copy(cultura = it)) }, "Cultura")
            CampoTexto(r.areaHa, { vm.atualizarRascunho(r.copy(areaHa = it)) }, "Área (ha)", tipo = KeyboardType.Decimal)
            CampoTexto(r.produtividade, { vm.atualizarRascunho(r.copy(produtividade = it)) }, "Produtividade (sacas/ha)", tipo = KeyboardType.Decimal)
            SeletorLocal("plantio", "Local do plantio", busca, r.lat, r.lon, r.local) { a, o, n ->
                vm.atualizarRascunho(vm.rascunho.copy(lat = a, lon = o, local = n))
            }

            EstadoDaTela(vm.carregando, vm.demorando, vm.mensagem)
            BotaoPrincipal("Salvar", { vm.salvar(context.contentResolver, aoSalvar) }, carregando = vm.carregando)
        }
    }
}
