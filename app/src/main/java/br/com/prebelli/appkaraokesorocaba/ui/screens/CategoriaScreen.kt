package br.com.prebelli.appkaraokesorocaba.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.prebelli.appkaraokesorocaba.data.ALFABETO
import br.com.prebelli.appkaraokesorocaba.data.CantorFaixas
import br.com.prebelli.appkaraokesorocaba.data.MusicasRepository
import br.com.prebelli.appkaraokesorocaba.data.agruparPorCantor
import br.com.prebelli.appkaraokesorocaba.data.categoriaPorKey
import br.com.prebelli.appkaraokesorocaba.ui.components.AppFooter
import br.com.prebelli.appkaraokesorocaba.ui.components.BotaoTopo
import br.com.prebelli.appkaraokesorocaba.ui.components.CantorList
import br.com.prebelli.appkaraokesorocaba.ui.components.LoadingOverlay
import br.com.prebelli.appkaraokesorocaba.ui.components.PageHeader
import br.com.prebelli.appkaraokesorocaba.ui.theme.Border
import br.com.prebelli.appkaraokesorocaba.ui.theme.Brand600
import br.com.prebelli.appkaraokesorocaba.ui.theme.Brand700
import br.com.prebelli.appkaraokesorocaba.ui.theme.Muted
import br.com.prebelli.appkaraokesorocaba.ui.theme.Surface
import kotlinx.coroutines.launch

@Composable
fun CategoriaScreen(
    catKey: String,
    repository: MusicasRepository,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onVerificarAtualizacao: () -> Unit
) {
    val categoria = remember(catKey) { categoriaPorKey(catKey) }
    var letraSelecionada by rememberSaveable(catKey) {
        mutableStateOf(if (categoria.filtraPorLetra) "A" else "")
    }
    var grupos by remember { mutableStateOf<List<CantorFaixas>>(emptyList()) }
    var expandidos by remember { mutableStateOf<Set<String>>(emptySet()) }
    var carregando by remember { mutableStateOf(true) }
    var erro by remember { mutableStateOf<String?>(null) }
    var tentativa by remember { mutableIntStateOf(0) }

    val listState = rememberLazyListState()
    val escopo = rememberCoroutineScope()

    LaunchedEffect(categoria.key, letraSelecionada, tentativa) {
        carregando = true
        erro = null
        expandidos = emptySet()
        val letra = if (categoria.filtraPorLetra) {
            if (letraSelecionada == "#") "0" else letraSelecionada
        } else {
            null
        }

        runCatching { repository.categoria(categoria.key, letra) }
            .onSuccess { resultado ->
                grupos = agruparPorCantor(resultado)
                runCatching { listState.scrollToItem(0) }
            }
            .onFailure { falha ->
                grupos = emptyList()
                erro = falha.message ?: "Não foi possível carregar as músicas."
            }
        carregando = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
    ) {
        PageHeader(
            titulo = categoria.titulo,
            eyebrow = "Karaoke Sorocaba",
            onBack = onBack,
            onHome = onHome
        )

        if (categoria.filtraPorLetra) {
            LetterNav(
                selecionada = letraSelecionada,
                onSelect = { letra ->
                    if (letra != letraSelecionada) {
                        carregando = true
                        erro = null
                        letraSelecionada = letra
                    }
                }
            )
        }

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
                emptyText = "Nenhuma música encontrada nesta letra.",
                emptyIcon = Icons.Filled.MusicNote,
                erro = erro,
                onRetry = { tentativa++ },
                footer = { AppFooter(onVerificarAtualizacao = onVerificarAtualizacao) }
            )

            if (carregando) {
                LoadingOverlay(texto = "Carregando músicas...")
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

@Composable
private fun LetterNav(
    selecionada: String,
    onSelect: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        items(items = ALFABETO, key = { it }) { letra ->
            val ativa = letra == selecionada
            val shape = RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .size(width = 38.dp, height = 38.dp)
                    .clip(shape)
                    .background(
                        brush = if (ativa) {
                            Brush.linearGradient(listOf(Brand700, Brand600))
                        } else {
                            SolidColor(Color.White)
                        },
                        shape = shape
                    )
                    .border(
                        width = 1.dp,
                        color = if (ativa) Color.Transparent else Border,
                        shape = shape
                    )
                    .clickable(role = Role.Button) { onSelect(letra) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letra,
                    color = if (ativa) Color.White else Muted,
                    fontSize = 13.5.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }
    }
}
