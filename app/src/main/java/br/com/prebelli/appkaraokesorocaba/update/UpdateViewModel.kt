package br.com.prebelli.appkaraokesorocaba.update

import android.app.Application
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.prebelli.appkaraokesorocaba.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class UpdateViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application
    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    private var verificouNestaSessao = false

    fun verificarAoAbrir() {
        if (verificouNestaSessao) return
        verificouNestaSessao = true
        verificar(manual = false)
    }

    fun verificar(manual: Boolean) {
        val atual = _state.value
        if (atual is UpdateState.Downloading ||
            atual is UpdateState.ReadyToInstall ||
            atual is UpdateState.NeedsPermission ||
            atual is UpdateState.Checking
        ) {
            return
        }

        viewModelScope.launch {
            _state.value = UpdateState.Checking(manual)
            runCatching { GitHubReleaseClient.buscarUltima(BuildConfig.VERSION_NAME) }
                .onSuccess { release ->
                    _state.value = when {
                        release == null && manual -> UpdateState.UpToDate
                        release == null -> UpdateState.Idle
                        else -> UpdateState.Available(release)
                    }
                }
                .onFailure { erro ->
                    _state.value = if (manual) {
                        UpdateState.Error(erro.message ?: "Não foi possível verificar atualizações.")
                    } else {
                        UpdateState.Idle
                    }
                }
        }
    }

    fun baixarEInstalar(release: ReleaseInfo) {
        if (release.apkUrl.isBlank()) {
            _state.value = UpdateState.Error(
                "A versão ${release.versao} ainda não possui um APK publicado no release."
            )
            return
        }

        viewModelScope.launch {
            _state.value = UpdateState.Downloading(-1f, 0L, release.tamanhoApk)
            runCatching {
                baixarApk(release.apkUrl) { baixado, total ->
                    _state.value = UpdateState.Downloading(
                        fracao = if (total > 0) baixado.toFloat() / total else -1f,
                        baixado = baixado,
                        total = total
                    )
                }
            }.onSuccess { apk ->
                _state.value = UpdateState.ReadyToInstall(apk, release)
            }.onFailure { erro ->
                _state.value = UpdateState.Error(
                    "Falha ao baixar a atualização: ${erro.message ?: "erro desconhecido"}"
                )
            }
        }
    }

    fun instalar(apk: File, release: ReleaseInfo) {
        if (!apk.exists()) {
            _state.value = UpdateState.Error("O arquivo da atualização não foi encontrado. Baixe novamente.")
            return
        }

        if (!app.packageManager.canRequestPackageInstalls()) {
            _state.value = UpdateState.NeedsPermission(apk, release)
            return
        }

        abrirInstalador(apk)
    }

    fun abrirConfiguracoesPermissao() {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${app.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        runCatching { app.startActivity(intent) }.onFailure {
            runCatching {
                app.startActivity(
                    Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }

    fun dispensar() {
        val atual = _state.value
        if (atual is UpdateState.Downloading) return
        _state.value = UpdateState.Idle
    }

    private fun abrirInstalador(apk: File) {
        runCatching {
            val uri = FileProvider.getUriForFile(
                app,
                "${app.packageName}.fileprovider",
                apk
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                clipData = ClipData.newRawUri("apk", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            app.startActivity(intent)
            _state.value = UpdateState.Idle
        }.onFailure { erro ->
            _state.value = UpdateState.Error(
                "Não foi possível abrir o instalador: ${erro.message ?: "erro desconhecido"}"
            )
        }
    }

    private suspend fun baixarApk(
        url: String,
        aoProgredir: (Long, Long) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val pasta = File(app.cacheDir, "updates").apply { mkdirs() }
        val destino = File(pasta, "app-update.apk")
        if (destino.exists()) runCatching { destino.delete() }

        val conexao = URL(url).openConnection() as HttpURLConnection
        conexao.requestMethod = "GET"
        conexao.instanceFollowRedirects = true
        conexao.connectTimeout = 20_000
        conexao.readTimeout = 60_000
        conexao.setRequestProperty("User-Agent", "AppKaraokeSorocaba")
        conexao.setRequestProperty("Accept", "application/octet-stream")

        try {
            val codigo = conexao.responseCode
            if (codigo !in 200..299) {
                throw IOException("o servidor respondeu HTTP $codigo")
            }

            val total = conexao.contentLengthLong
            conexao.inputStream.use { entrada ->
                FileOutputStream(destino).use { saida ->
                    val buffer = ByteArray(64 * 1024)
                    var baixado = 0L
                    var ultimaAtualizacao = 0L
                    while (true) {
                        val lido = entrada.read(buffer)
                        if (lido < 0) break
                        saida.write(buffer, 0, lido)
                        baixado += lido
                        val agora = System.currentTimeMillis()
                        if (agora - ultimaAtualizacao >= 120 || (total > 0 && baixado >= total)) {
                            aoProgredir(baixado, total)
                            ultimaAtualizacao = agora
                        }
                    }
                    saida.flush()
                }
            }

            if (destino.length() < 1024) {
                throw IOException("o arquivo baixado está incompleto")
            }
            val assinatura = destino.inputStream().use { stream ->
                val bytes = ByteArray(2)
                val lidos = stream.read(bytes)
                if (lidos < 2) ByteArray(0) else bytes
            }
            if (assinatura.size < 2 ||
                assinatura[0] != 0x50.toByte() ||
                assinatura[1] != 0x4B.toByte()
            ) {
                throw IOException("o arquivo baixado não é um APK válido")
            }

            aoProgredir(destino.length(), destino.length())
            destino
        } finally {
            conexao.disconnect()
        }
    }
}
