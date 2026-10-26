package br.com.prebelli.appkaraokesorocaba.update

import java.io.File

sealed interface UpdateState {
    data object Idle : UpdateState
    data class Checking(val manual: Boolean) : UpdateState
    data class Available(val release: ReleaseInfo) : UpdateState
    data object UpToDate : UpdateState
    data class Downloading(
        val fracao: Float,
        val baixado: Long,
        val total: Long
    ) : UpdateState
    data class ReadyToInstall(
        val apk: File,
        val release: ReleaseInfo
    ) : UpdateState
    data class NeedsPermission(
        val apk: File,
        val release: ReleaseInfo
    ) : UpdateState
    data class Error(val mensagem: String) : UpdateState
}
