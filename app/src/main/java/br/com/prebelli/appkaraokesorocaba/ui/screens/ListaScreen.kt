package br.com.prebelli.appkaraokesorocaba.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.prebelli.appkaraokesorocaba.R
import br.com.prebelli.appkaraokesorocaba.data.CATEGORIAS
import br.com.prebelli.appkaraokesorocaba.data.Categoria
import br.com.prebelli.appkaraokesorocaba.ui.components.AppFooter
import br.com.prebelli.appkaraokesorocaba.ui.components.SearchForm
import br.com.prebelli.appkaraokesorocaba.ui.theme.Border
import br.com.prebelli.appkaraokesorocaba.ui.theme.Brand500
import br.com.prebelli.appkaraokesorocaba.ui.theme.Gold
import br.com.prebelli.appkaraokesorocaba.ui.theme.HeroEnd
import br.com.prebelli.appkaraokesorocaba.ui.theme.HeroMiddle
import br.com.prebelli.appkaraokesorocaba.ui.theme.HeroStart
import br.com.prebelli.appkaraokesorocaba.ui.theme.Muted
import br.com.prebelli.appkaraokesorocaba.ui.theme.Surface
import br.com.prebelli.appkaraokesorocaba.ui.theme.TextPrimary

@Composable
fun ListaScreen(
    onCategoria: (String) -> Unit,
    onBuscar: (String) -> Unit,
    onVerificarAtualizacao: () -> Unit
) {
    var termo by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .verticalScroll(rememberScrollState())
    ) {
        HeroLista(
            termo = termo,
            onTermoChange = { termo = it },
            onSearch = {
                val texto = termo.trim()
                if (texto.length >= 4) onBuscar(texto)
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(18.dp))
            SectionTitle(titulo = "Categorias", hint = "Escolha um estilo")
            Spacer(Modifier.height(14.dp))
            CategoriasGrid(onCategoria = onCategoria)
            AppFooter(onVerificarAtualizacao = onVerificarAtualizacao)
        }
    }
}

@Composable
private fun HeroLista(
    termo: String,
    onTermoChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            .heroBackground()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 28.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = 100.dp.toPx(),
                center = Offset(size.width - 20.dp.toPx(), -20.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.logo_don),
                    contentDescription = "Don Petisco",
                    modifier = Modifier
                        .size(width = 82.dp, height = 70.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(7.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    text = "Karaoke Sorocaba",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 30.sp,
                        lineHeight = 33.sp
                    ),
                    color = Color.White
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Encontre a música certa para a sua vez no microfone.",
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 14.5.sp,
                lineHeight = 21.sp,
                modifier = Modifier.fillMaxWidth(0.92f)
            )

            Spacer(Modifier.height(22.dp))

            SearchForm(
                termo = termo,
                onTermoChange = onTermoChange,
                onSearch = onSearch
            )
        }
    }
}

@Composable
private fun SectionTitle(titulo: String, hint: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 17.5.sp),
            color = TextPrimary
        )
        Text(
            text = hint,
            color = Muted,
            fontSize = 11.5.sp
        )
    }
}

@Composable
private fun CategoriasGrid(onCategoria: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CATEGORIAS.chunked(2).forEach { linha ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                linha.forEach { categoria ->
                    CartaoCategoria(
                        categoria = categoria,
                        modifier = Modifier.weight(1f),
                        onClick = { onCategoria(categoria.key) }
                    )
                }
                if (linha.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CartaoCategoria(
    categoria: Categoria,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(Color.White)
            .border(1.dp, Border, shape)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 16.dp, end = 14.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(categoria.bandeira),
            contentDescription = null,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(15.dp)),
            contentScale = ContentScale.Crop
        )
        Text(
            text = categoria.titulo,
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = categoria.hint,
            color = Muted,
            fontSize = 11.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun Modifier.heroBackground(): Modifier = drawBehind {
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(HeroStart, HeroMiddle, HeroEnd),
            start = Offset.Zero,
            end = Offset(size.width, size.height)
        )
    )
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Brand500.copy(alpha = 0.50f), Color.Transparent),
            center = Offset(size.width, 0f),
            radius = size.width * 0.9f
        )
    )
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Gold.copy(alpha = 0.16f), Color.Transparent),
            center = Offset(0f, size.height),
            radius = size.width * 0.8f
        )
    )
}
