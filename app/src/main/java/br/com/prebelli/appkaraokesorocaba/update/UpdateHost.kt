package br.com.prebelli.appkaraokesorocaba.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.prebelli.appkaraokesorocaba.BuildConfig
import br.com.prebelli.appkaraokesorocaba.ui.theme.Brand600
import br.com.prebelli.appkaraokesorocaba.ui.theme.Muted
import br.com.prebelli.appkaraokesorocaba.util.abrirLink
import java.util.Locale

@Composable
fun UpdateHost(viewModel: UpdateViewModel) {
    val estado by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    when (val atual = estado) {
        is UpdateState.Checking -> {
            if (atual.manual) {
                AlertDialog(
                    onDismissRequest = {},
                    confirmButton = {},
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Brand600,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(Modifier.width(14.dp))
                            Text("Verificando atualizações...")
                        }
                    }
                )
            }
        }

        is UpdateState.Available -> {
            AlertDialog(
                onDismissRequest = { viewModel.dispensar() },
                title = { Text("Atualização disponível") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Versão ${atual.release.versao}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (atual.release.notas.isNotBlank()) {
                            Text(
                                text = atual.release.notas.take(700),
                                style = MaterialTheme.typography.bodySmall,
                                color = Muted
                            )
                        }
                        if (atual.release.apkUrl.isBlank()) {
                            Text(
                                text = "O APK desta versão ainda não foi publicado.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                text = "O download será feito agora e o instalador do Android será aberto.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Muted
                            )
                        }
                    }
                },
                confirmButton = {
                    if (atual.release.apkUrl.isBlank()) {
                        TextButton(
                            onClick = {
                                if (atual.release.htmlUrl.isNotBlank()) {
                                    context.abrirLink(atual.release.htmlUrl)
                                }
                                viewModel.dispensar()
                            }
                        ) {
                            Text("Abrir no GitHub")
                        }
                    } else {
                        TextButton(onClick = { viewModel.baixarEInstalar(atual.release) }) {
                            Text("Atualizar agora")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dispensar() }) {
                        Text("Depois")
                    }
                }
            )
        }

        is UpdateState.Downloading -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Baixando atualização") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (atual.fracao >= 0f) {
                            LinearProgressIndicator(
                                progress = { atual.fracao.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth(),
                                color = Brand600
                            )
                            Text(
                                text = "${(atual.fracao * 100).toInt()}% • " +
                                    "${formatarTamanho(atual.baixado)} de ${formatarTamanho(atual.total)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Muted
                            )
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = Brand600
                            )
                            Text(
                                text = "${formatarTamanho(atual.baixado)} baixados",
                                style = MaterialTheme.typography.bodySmall,
                                color = Muted
                            )
                        }
                    }
                },
                confirmButton = {}
            )
        }

        is UpdateState.ReadyToInstall -> {
            LaunchedEffect(atual.apk.absolutePath) {
                viewModel.instalar(atual.apk, atual.release)
            }
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Brand600,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(Modifier.width(14.dp))
                        Text("Preparando instalação...")
                    }
                }
            )
        }

        is UpdateState.NeedsPermission -> {
            AlertDialog(
                onDismissRequest = { viewModel.dispensar() },
                title = { Text("Permissão para instalar") },
                text = {
                    Text(
                        "Para atualizar o Karaoke Sorocaba, permita a instalação de apps " +
                            "desconhecidos para este aplicativo.\n\n" +
                            "Toque em \"Abrir configurações\", ative a permissão e volte " +
                            "para tocar em \"Já autorizei\"."
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.abrirConfiguracoesPermissao() }) {
                        Text("Abrir configurações")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.instalar(atual.apk, atual.release) }) {
                        Text("Já autorizei")
                    }
                }
            )
        }

        is UpdateState.UpToDate -> {
            AlertDialog(
                onDismissRequest = { viewModel.dispensar() },
                title = { Text("Tudo atualizado") },
                text = {
                    Text(
                        "O Karaoke Sorocaba já está na versão mais recente " +
                            "(${BuildConfig.VERSION_NAME})."
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.dispensar() }) {
                        Text("OK")
                    }
                }
            )
        }

        is UpdateState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.dispensar() },
                title = { Text("Atualização") },
                text = { Text(atual.mensagem) },
                confirmButton = {
                    TextButton(onClick = { viewModel.dispensar() }) {
                        Text("Fechar")
                    }
                }
            )
        }

        UpdateState.Idle -> Unit
    }
}

private fun formatarTamanho(bytes: Long): String {
    if (bytes <= 0L) return "0 KB"
    val kb = bytes / 1024.0
    if (kb < 1024.0) {
        return String.format(Locale("pt", "BR"), "%.0f KB", kb)
    }
    val mb = kb / 1024.0
    return String.format(Locale("pt", "BR"), "%.1f MB", mb)
}
