package br.com.prebelli.appkaraokesorocaba.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder

class MusicasApi(
    private val baseUrl: String = "https://apikaraoke.prebellisolucoes.com/Musicas/"
) {

    suspend fun getCategoria(cat: String, letra: String?): String = withContext(Dispatchers.IO) {
        val url = buildString {
            append(baseUrl)
            append("verCategoria?cat=")
            append(URLEncoder.encode(cat, "UTF-8"))
            if (letra != null) {
                append("&letra=")
                append(URLEncoder.encode(letra, "UTF-8"))
            }
        }
        request(url)
    }

    suspend fun busca(termo: String): String = withContext(Dispatchers.IO) {
        request("${baseUrl}Busca?termo=${URLEncoder.encode(termo, "UTF-8")}")
    }

    private fun request(url: String): String {
        val conexao = URL(url).openConnection() as HttpURLConnection
        conexao.requestMethod = "GET"
        conexao.connectTimeout = 15_000
        conexao.readTimeout = 25_000
        conexao.instanceFollowRedirects = true
        conexao.setRequestProperty("Accept", "application/json")
        conexao.setRequestProperty("User-Agent", "AppKaraokeSorocaba")
        try {
            val codigo = conexao.responseCode
            if (codigo !in 200..299) {
                throw IOException("Falha ao consultar o servidor (HTTP $codigo).")
            }
            return conexao.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (e: SocketTimeoutException) {
            throw IOException("Tempo esgotado ao consultar o servidor.", e)
        } finally {
            conexao.disconnect()
        }
    }
}
