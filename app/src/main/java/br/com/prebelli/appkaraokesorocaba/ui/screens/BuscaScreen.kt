package br.com.prebelli.appkaraokesorocaba.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.prebelli.appkaraokesorocaba.data.CantorFaixas
import br.com.prebelli.appkaraokesorocaba.data.MusicasRepository
import br.com.prebelli.appkaraokesorocaba.data.agruparPorCantor
import br.com.prebelli.appkaraokesorocaba.ui.components.AppFooter
import br.com.prebelli.appkaraokesorocaba.ui.components.BotaoTopo
import br.com.prebelli.appkaraokesorocaba.ui.components.CantorList
import br.com.prebelli.appkaraokesorocaba.ui.components.LoadingOverlay
import br.com.prebelli.appkaraokesorocaba.ui.components.PageHeader
import br.com.prebelli.appkaraokesorocaba.ui.components.SearchForm
import br.com.prebelli.appkaraokesorocaba.ui.theme.Surface
import kotlinx.coroutines.launch

@Composable
fun BuscaScreen(
    initialTermo: String,
    repository: MusicasRepository,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onVerificarAtualizacao: () -> Unit
) {
    var termo by rememberSaveable { mutableStateOf(initialTermo) }
    var consulta by rememberSaveable { mutableStateOf(initialTermo) }
    var tentativa by remember { mutableIntStateOf(0) }
    var grupos by remember { mutableStateOf<List<CantorFaixas>>(emptyList()) }
    var expandidos by remember { mutableStateOf<Set<String>>(emptySet()) }
    var carregando by remember { mutableStateOf(true) }
    var erro by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    val escopo = rememberCoroutineScope()

    LaunchedEffect(consulta, tentativa) {
        val texto = consulta.trim()
        if (texto.length < 4) {
            grupos = emptyList()
            carregando = false
            erro = null
            return@LaunchedEffect
        }

        carregando = true
        erro = null
        expandidos = emptySet()

        runCatching { repository.buscar(texto) }
            .onSuccess { resultado ->
                grupos = agruparPorCantor(resultado)
                runCatching { listState.scrollToItem(0) }
            }
            .onFailure { falha ->
                grupos = emptyList()
                erro = falha.message ?: "Não foi possível buscar as músicas."
            }
        carregando = false
    }

    fun buscar() {
        val texto = termo.trim()
        if (texto.length < 4) return
        if (texto == consulta) {
            tentativa++
        } else {
            consulta = texto
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
    ) {
        PageHeader(
            titulo = consulta,
            eyebrow = "Resultados para",
            onBack = onBack,
            onHome = onHome
        )

        SearchForm(
            termo = termo,
            onTermoChange = { termo = it },
            onSearch = { buscar() },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        )

        Box(modifier = Modifier.weight(1f)) {
            CantorList(
                grupos = grupos,
                expandidos = expandidos,
                onToggle = { cantor ->
                    expandidos = if (cantor in expandidos) {
                        expandidos - cantor
                    } else {
                        expandidos + cantor
                    }
                },
                listState = listState,
                emptyText = if (consulta.isBlank()) {
                    "Digite pelo menos 4 letras para buscar."
                } else {
                    "Nenhuma música encontrada para “$consulta”."
                },
                emptyIcon = Icons.Filled.Search,
                erro = erro,
                onRetry = { tentativa++ },
                footer = { AppFooter(onVerificarAtualizacao = onVerificarAtualizacao) }
            )

            if (carregando) {
                LoadingOverlay(texto = "Buscando músicas...")
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
                    .navigationBarsPadding()
            ) {
                BotaoTopo(
                    onClick = {
                        expandidos = emptySet()
                        escopo.launch {
                            runCatching { listState.animateScrollToItem(0) }
                        }
                    }
                )
            }
        }
    }
}
