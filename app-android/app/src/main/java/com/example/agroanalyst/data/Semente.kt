package com.example.agroanalyst.data

import android.content.res.Resources
import com.example.agroanalyst.R
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Semente {
    private var cache: SementeDto? = null

    suspend fun carregar(res: Resources): SementeDto? {
        cache?.let { return it }
        return withContext(Dispatchers.IO) {
            res.openRawResource(R.raw.semente).bufferedReader().use { Gson().fromJson(it, SementeDto::class.java) }
        }.also { cache = it }
    }
}
