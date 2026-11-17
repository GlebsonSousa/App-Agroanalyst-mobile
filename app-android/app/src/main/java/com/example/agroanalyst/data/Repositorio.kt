package com.example.agroanalyst.data

import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import okhttp3.MultipartBody
import retrofit2.Response
import java.io.IOException

open class ApiException(mensagem: String) : Exception(mensagem)
/** 401/403 em rota autenticada: o app volta ao login. */
class SessaoExpirada : ApiException("Sessão expirada. Entre novamente.")

/** Chama as APIs, confere isSuccessful, lê errorBody e devolve o corpo ou lança exceção com a mensagem da API. Sem estado, sem corrotina própria. */
object Repositorio {
    private fun mensagemDaApi(r: Response<*>): String {
        val corpo = runCatching { r.errorBody()?.string() }.getOrNull()
        val daApi = runCatching { JsonParser.parseString(corpo).asJsonObject.get("mensagem").asString }.getOrNull()
        return daApi ?: "Erro ${r.code()} ao falar com o servidor"
    }

    private suspend fun <T> chamar(autenticado: Boolean = true, bloco: suspend () -> Response<T>): T {
        val r = try { bloco() } catch (e: IOException) {
            throw ApiException("Sem conexão com o servidor. Verifique a internet e tente de novo.")
        }
        if (r.isSuccessful) return r.body() ?: throw ApiException("Resposta vazia do servidor")
        if (autenticado && (r.code() == 401 || r.code() == 403)) throw SessaoExpirada()
        throw ApiException(mensagemDaApi(r))
    }

    // ----- usuários -----
    suspend fun registrar(nome: String, email: String, senha: String) =
        chamar(false) { ApiClient.agro.registrar(RegistroEnvio(nome, email, senha)) }
    suspend fun login(email: String, senha: String) =
        chamar(false) { ApiClient.agro.login(LoginEnvio(email, senha)) }

    // ----- plantios -----
    suspend fun plantios() = chamar { ApiClient.agro.plantios() }
    suspend fun plantio(id: String) = chamar { ApiClient.agro.plantio(id) }
    suspend fun criarPlantio(e: PlantioEnvio) = chamar { ApiClient.agro.criarPlantio(e) }
    suspend fun atualizarPlantio(id: String, e: PlantioEnvio) = chamar { ApiClient.agro.atualizarPlantio(id, e) }
    suspend fun excluirPlantio(id: String) = chamar { ApiClient.agro.excluirPlantio(id) }
    suspend fun enviarFoto(parte: MultipartBody.Part) = chamar { ApiClient.agro.enviarFoto(parte) }

    // ----- logística -----
    suspend fun destinos() = chamar { ApiClient.agro.destinos() }
    suspend fun graos() = chamar { ApiClient.agro.graos() }
    suspend fun calcularFrete(e: FreteEnvio) = chamar { ApiClient.agro.calcularFrete(e) }
    suspend fun vendas() = chamar { ApiClient.agro.vendas() }
    suspend fun venda(id: String) = chamar { ApiClient.agro.venda(id) }
    suspend fun criarVenda(e: VendaEnvio) = chamar { ApiClient.agro.criarVenda(e) }
    suspend fun mudarStatus(id: String, status: String) = chamar { ApiClient.agro.mudarStatus(id, StatusEnvio(status)) }

    // ----- meteoserver (público) -----
    suspend fun solo(lat: Double, lon: Double) = chamar(false) { ApiClient.meteo.solo(lat, lon) }

    /** Acorda os dois serviços do Render em paralelo; resultado e erro são ignorados. */
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
