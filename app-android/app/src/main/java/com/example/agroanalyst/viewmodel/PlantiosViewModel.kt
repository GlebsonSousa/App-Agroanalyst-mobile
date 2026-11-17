package com.example.agroanalyst.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.agroanalyst.data.FotoUpload
import com.example.agroanalyst.data.PlantioDto
import com.example.agroanalyst.data.PlantioEnvio
import com.example.agroanalyst.data.Repositorio

/** Rascunho do formulário: vive no ViewModel e por isso sobrevive à rotação. Números como texto. */
data class Rascunho(
    val id: String? = null,
    val nome: String = "",
    val cultura: String = "",
    val areaHa: String = "",
    val produtividade: String = "",
    val lat: String = "",
    val lon: String = "",
    val fotoId: String? = null,
    val fotoUri: Uri? = null
)

class PlantiosViewModel : OperacaoViewModel() {
    var lista by mutableStateOf<List<PlantioDto>>(emptyList())
        private set
    var atual by mutableStateOf<PlantioDto?>(null)
        private set
    var rascunho by mutableStateOf(Rascunho())
        private set

    fun carregarLista() = executar { lista = Repositorio.plantios() }

    fun carregarUm(id: String) {
        if (atual?.id != id) atual = null
        executar { atual = Repositorio.plantio(id) }
    }

    // ----- rascunho -----
    fun atualizarRascunho(novo: Rascunho) { rascunho = novo }
    fun descartar() { rascunho = Rascunho() }

    fun prepararNovo(cultura: String?, lat: Double?, lon: Double?) {
        rascunho = Rascunho(
            cultura = cultura.orEmpty(),
            lat = lat?.toString().orEmpty(),
            lon = lon?.toString().orEmpty()
        )
    }

    /** Edição sempre parte do que o servidor tem; só ignora se a rotação já trouxe o rascunho. */
    fun abrirEdicao(id: String) {
        if (rascunho.id == id) return
        executar {
            val p = Repositorio.plantio(id)
            rascunho = Rascunho(
                id = p.id, nome = p.nome.orEmpty(), cultura = p.cultura.orEmpty(),
                areaHa = p.areaHa?.toString().orEmpty(), produtividade = p.produtividadeSacasHa?.toString().orEmpty(),
                lat = p.lat?.toString().orEmpty(), lon = p.lon?.toString().orEmpty(), fotoId = p.fotoId
            )
        }
    }

    fun salvar(resolver: ContentResolver, aoSucesso: () -> Unit) {
        val r = rascunho
        fun n(t: String) = t.trim().replace(',', '.').toDoubleOrNull()
        val area = n(r.areaHa); val prod = n(r.produtividade); val lat = n(r.lat); val lon = n(r.lon)
        when {
            r.nome.isBlank() || r.cultura.isBlank() -> mensagem = "Informe nome e cultura."
            area == null || area <= 0 || prod == null || prod <= 0 -> mensagem = "Área e produtividade devem ser números maiores que zero."
            lat == null || lon == null || lat !in -90.0..90.0 || lon !in -180.0..180.0 -> mensagem = "Coordenada inválida."
            else -> executar {
                // Upload e vínculo são passos separados: envia a foto, recebe o fileId, depois grava no plantio.
                val fotoId = if (r.fotoUri != null)
                    Repositorio.enviarFoto(FotoUpload.paraParte(resolver, r.fotoUri)).fileId else r.fotoId
                val envio = PlantioEnvio(r.nome.trim(), r.cultura.trim(), area, prod, lat, lon, fotoId)
                val salvo = if (r.id == null) Repositorio.criarPlantio(envio) else Repositorio.atualizarPlantio(r.id, envio)
                lista = Repositorio.plantios() // a lista reflete o que o servidor confirmou
                atual = salvo
                descartar()
                aoSucesso()
            }
        }
    }

    fun excluir(id: String, aoSucesso: () -> Unit = {}) = executar {
        Repositorio.excluirPlantio(id)
        lista = Repositorio.plantios()
        aoSucesso()
    }

    override fun limpar() { super.limpar(); lista = emptyList(); atual = null; rascunho = Rascunho() }
}
