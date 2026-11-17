package com.example.agroanalyst.data

import android.content.res.Resources
import com.example.agroanalyst.R
import com.google.gson.JsonArray
import com.google.gson.JsonParser

class UfPoligono(val codigo: String, val aneis: List<List<Pair<Double, Double>>>)

class LinhaLogistica(val tipo: String, val pontos: List<Pair<Double, Double>>)

object MalhaLocal {
    private var ufs: List<UfPoligono>? = null

    fun ufs(res: Resources): List<UfPoligono> = ufs ?: lerUfs(res).also { ufs = it }

    private fun lerUfs(res: Resources): List<UfPoligono> {
        val raiz = res.openRawResource(R.raw.ufs).bufferedReader().use { JsonParser.parseReader(it).asJsonObject }
        return raiz.getAsJsonArray("features").map { f ->
            val obj = f.asJsonObject
            val geo = obj.getAsJsonObject("geometry")
            val coords = geo.getAsJsonArray("coordinates")
            val poligonos = if (geo.get("type").asString == "MultiPolygon") coords.map { it.asJsonArray } else listOf(coords)
            UfPoligono(
                obj.getAsJsonObject("properties").get("codarea").asString,
                poligonos.flatMap { p -> p.map { anel -> anel.asJsonArray.map { it.asJsonArray.let { c -> c[0].asDouble to c[1].asDouble } } } }
            )
        }
    }

    fun linhas(res: Resources, pacote: String): List<LinhaLogistica>? {
        val id = res.getIdentifier("malha", "raw", pacote)
        if (id == 0) return null
        val raiz = res.openRawResource(id).bufferedReader().use { JsonParser.parseReader(it).asJsonArray }
        return raiz.map { e ->
            val o = e.asJsonObject
            LinhaLogistica(o.get("tipo").asString, o.getAsJsonArray("pontos").pontos())
        }
    }

    private fun JsonArray.pontos() = map { it.asJsonArray.let { c -> c[0].asDouble to c[1].asDouble } }
}
