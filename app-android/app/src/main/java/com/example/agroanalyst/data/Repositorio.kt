package com.example.agroanalyst.data

import android.content.res.Resources
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import retrofit2.Response
import java.io.IOException

open class ApiException(mensagem: String) : Exception(mensagem)
class SessaoExpirada : ApiException("Sessão expirada. Entre novamente.")

object Repositorio {
    private var recursos: Resources? = null
    private var pacote: String = ""

    fun iniciar(resources: Resources, packageName: String) { recursos = resources; pacote = packageName }

    suspend fun semente(): SementeDto? = recursos?.let { Semente.carregar(it) }

    suspend fun ufs(): List<UfPoligono> {
        val res = recursos ?: return emptyList()
        return withContext(Dispatchers.IO) { MalhaLocal.ufs(res) }
    }

    suspend fun linhasLogistica(): List<LinhaLogistica>? {
        val res = recursos ?: return null
        return withContext(Dispatchers.IO) { MalhaLocal.linhas(res, pacote) }
    }

    private fun mensagemDaApi(r: Response<*>): String {
        val corpo = runCatching { r.errorBody()?.string() }.getOrNull()
        val daApi = runCatching { JsonParser.parseString(corpo).asJsonObject.get("mensagem").asString }.getOrNull()
        return daApi ?: "Erro ${r.code()} ao falar com o servidor"
    }

    private suspend fun <T> chamar(privado: Boolean, bloco: suspend () -> Response<T>): T {
        val r = try { bloco() } catch (e: IOException) {
            throw ApiException("Sem conexão com o servidor. Verifique a internet e tente de novo.")
        }
        if (r.isSuccessful) return r.body() ?: throw ApiException("Resposta vazia do servidor")
        if (privado && (r.code() == 401 || r.code() == 403)) throw SessaoExpirada()
        throw ApiException(mensagemDaApi(r))
    }

    private suspend fun <T> publico(bloco: suspend () -> Response<T>) = chamar(false, bloco)
    private suspend fun <T> privado(bloco: suspend () -> Response<T>) = chamar(true, bloco)

    // ----- públicas -----
    suspend fun precos() = publico { ApiClient.agro.precos() }
    suspend fun serie(cultura: String) = publico { ApiClient.agro.serie(cultura, 30) }
    suspend fun producao(cultura: String) = publico { ApiClient.agro.producao(cultura, "uf") }
    suspend fun top(cultura: String) = publico { ApiClient.agro.top(cultura, 5) }
    suspend fun clima(lat: Double, lon: Double) = publico { ApiClient.agro.clima(lat, lon) }
    suspend fun pracas() = publico { ApiClient.agro.pracas() }
    suspend fun calcularFrete(e: FreteEnvio) = publico { ApiClient.agro.calcularFrete(e) }
    suspend fun solo(lat: Double, lon: Double) = publico { ApiClient.meteo.solo(lat, lon) }
    suspend fun registrar(nome: String, email: String, senha: String) = publico { ApiClient.agro.registrar(RegistroEnvio(nome, email, senha)) }
    suspend fun login(email: String, senha: String) = publico { ApiClient.agro.login(LoginEnvio(email, senha)) }

    // ----- privadas -----
    suspend fun plantios() = privado { ApiClient.agro.plantios() }
    suspend fun plantio(id: String) = privado { ApiClient.agro.plantio(id) }
    suspend fun criarPlantio(e: PlantioEnvio) = privado { ApiClient.agro.criarPlantio(e) }
    suspend fun atualizarPlantio(id: String, e: PlantioEnvio) = privado { ApiClient.agro.atualizarPlantio(id, e) }
    suspend fun excluirPlantio(id: String) = privado { ApiClient.agro.excluirPlantio(id) }
    suspend fun enviarFoto(parte: MultipartBody.Part) = privado { ApiClient.agro.enviarFoto(parte) }
    suspend fun fretes(plantioId: String) = privado { ApiClient.agro.fretes(plantioId) }
    suspend fun salvarFrete(plantioId: String, e: FreteEnvio) = privado { ApiClient.agro.salvarFrete(plantioId, e) }

    suspend fun aquecer() = coroutineScope {
        listOf(
            async { ignorarErro { ApiClient.agro.ping() } },
            async { ignorarErro { ApiClient.meteo.ping() } }
        ).awaitAll()
    }

    private suspend fun ignorarErro(bloco: suspend () -> Unit) {
        try { bloco() } catch (e: CancellationException) { throw e } catch (_: Exception) { }
    }
}
