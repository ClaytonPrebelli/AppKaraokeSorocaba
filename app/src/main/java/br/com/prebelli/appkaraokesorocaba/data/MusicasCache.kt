package br.com.prebelli.appkaraokesorocaba.data

import android.content.Context
import java.io.File

/**
 * Reproduz o cache de 15 dias que o site mantém no localStorage,
 * agora em arquivos locais do aplicativo.
 */
class MusicasCache(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("karaoke_cache", Context.MODE_PRIVATE)
    private val dir = File(context.applicationContext.filesDir, "musicas").apply { mkdirs() }

    fun limparSeExpirado() {
        val agora = System.currentTimeMillis()
        val ultimaLimpeza = prefs.getLong(KEY_ULTIMA_LIMPEZA, 0L)
        if (ultimaLimpeza == 0L) {
            prefs.edit().putLong(KEY_ULTIMA_LIMPEZA, agora).apply()
            return
        }
        if (agora - ultimaLimpeza >= VALIDADE_MS) {
            dir.listFiles()?.forEach { runCatching { it.delete() } }
            prefs.edit().putLong(KEY_ULTIMA_LIMPEZA, agora).apply()
        }
    }

    fun ler(key: String): String? {
        val arquivo = File(dir, "$key.json")
        if (!arquivo.exists()) return null
        if (System.currentTimeMillis() - arquivo.lastModified() >= VALIDADE_MS) {
            runCatching { arquivo.delete() }
            return null
        }
        return runCatching { arquivo.readText() }.getOrNull()
    }

    fun gravar(key: String, conteudo: String) {
        runCatching {
            File(dir, "$key.json").writeText(conteudo)
        }
    }

    companion object {
        private const val KEY_ULTIMA_LIMPEZA = "ultima_limpeza"
        private const val VALIDADE_MS = 15L * 24 * 60 * 60 * 1000
    }
}
