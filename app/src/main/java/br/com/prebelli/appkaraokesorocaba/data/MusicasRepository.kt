package br.com.prebelli.appkaraokesorocaba.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicasRepository(context: Context) {

    private val api = MusicasApi()
    private val cache = MusicasCache(context)

    init {
        cache.limparSeExpirado()
    }

    suspend fun categoria(cat: String, letra: String?): List<Musica> = withContext(Dispatchers.IO) {
        val chave = if (letra != null) "cat_${cat}_${letra}" else "cat_$cat"

        cache.ler(chave)?.let { conteudo ->
            runCatching { parseMusicas(conteudo) }.getOrNull()?.let { return@withContext it }
        }

        val conteudo = api.getCategoria(cat, letra)
        val musicas = parseMusicas(conteudo)
        cache.gravar(chave, conteudo)
        musicas
    }

    suspend fun buscar(termo: String): List<Musica> = withContext(Dispatchers.IO) {
        parseMusicas(api.busca(termo))
    }
}
