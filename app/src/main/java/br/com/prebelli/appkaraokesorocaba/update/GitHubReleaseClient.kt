package br.com.prebelli.appkaraokesorocaba.update

import br.com.prebelli.appkaraokesorocaba.util.versaoRemotaMaisNova
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class ReleaseInfo(
    val tag: String,
    val versao: String,
    val notas: String,
    val apkUrl: String,
    val htmlUrl: String,
    val tamanhoApk: Long
)

object GitHubReleaseClient {

    private const val URL_ULTIMO_RELEASE =
        "https://api.github.com/repos/ClaytonPrebelli/AppKaraokeSorocaba/releases/latest"

    suspend fun buscarUltima(versaoAtual: String): ReleaseInfo? = withContext(Dispatchers.IO) {
        val conexao = URL(URL_ULTIMO_RELEASE).openConnection() as HttpURLConnection
        conexao.requestMethod = "GET"
        conexao.connectTimeout = 15_000
        conexao.readTimeout = 20_000
        conexao.instanceFollowRedirects = true
        conexao.setRequestProperty("Accept", "application/vnd.github+json")
        conexao.setRequestProperty("User-Agent", "AppKaraokeSorocaba")

        try {
            val codigo = conexao.responseCode
            if (codigo == HttpURLConnection.HTTP_NOT_FOUND) return@withContext null
            if (codigo !in 200..299) {
                val mensagem = when (codigo) {
                    403 -> "Limite de verificações do GitHub atingido. Tente novamente mais tarde."
                    429 -> "Muitas verificações em pouco tempo. Tente novamente mais tarde."
                    else -> "Falha ao verificar atualizações (HTTP $codigo)."
                }
                throw IOException(mensagem)
            }

            val texto = conexao.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val objeto = JSONObject(texto)
            if (objeto.optBoolean("draft") || objeto.optBoolean("prerelease")) {
                return@withContext null
            }

            val tag = objeto.optString("tag_name")
            val versao = tag.trim().removePrefix("v").removePrefix("V")
            if (versao.isBlank() || !versaoRemotaMaisNova(versao, versaoAtual)) {
                return@withContext null
            }

            val ativos = objeto.optJSONArray("assets") ?: JSONArray()
            var apkUrl = ""
            var tamanho = 0L
            for (i in 0 until ativos.length()) {
                val ativo = ativos.optJSONObject(i) ?: continue
                val nome = ativo.optString("name")
                if (nome.endsWith(".apk", ignoreCase = true)) {
                    apkUrl = ativo.optString("browser_download_url")
                    tamanho = ativo.optLong("size")
                    break
                }
            }

            ReleaseInfo(
                tag = tag,
                versao = versao,
                notas = objeto.optString("body").trim(),
                apkUrl = apkUrl,
                htmlUrl = objeto.optString("html_url"),
                tamanhoApk = tamanho
            )
        } finally {
            conexao.disconnect()
        }
    }
}
