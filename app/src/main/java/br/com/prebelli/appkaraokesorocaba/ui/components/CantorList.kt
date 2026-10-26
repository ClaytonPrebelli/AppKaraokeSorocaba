package br.com.prebelli.appkaraokesorocaba.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.prebelli.appkaraokesorocaba.data.CantorFaixas
import br.com.prebelli.appkaraokesorocaba.data.Musica
import br.com.prebelli.appkaraokesorocaba.ui.theme.Border
import br.com.prebelli.appkaraokesorocaba.ui.theme.BorderStrong
import br.com.prebelli.appkaraokesorocaba.ui.theme.Brand100
import br.com.prebelli.appkaraokesorocaba.ui.theme.Brand500
import br.com.prebelli.appkaraokesorocaba.ui.theme.Brand600
import br.com.prebelli.appkaraokesorocaba.ui.theme.Brand700
import br.com.prebelli.appkaraokesorocaba.ui.theme.Brand800
import br.com.prebelli.appkaraokesorocaba.ui.theme.Muted
import br.com.prebelli.appkaraokesorocaba.ui.theme.SongBackground
import br.com.prebelli.appkaraokesorocaba.ui.theme.TextPrimary

@Composable
fun CantorList(
    grupos: List<CantorFaixas>,
    expandidos: Set<String>,
    onToggle: (String) -> Unit,
    listState: LazyListState,
    emptyText: String,
    emptyIcon: ImageVector,
    modifier: Modifier = Modifier,
    erro: String? = null,
    onRetry: (() -> Unit)? = null,
    footer: @Composable () -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        when {
            erro != null -> item(key = "erro") {
                ErrorCard(mensagem = erro, onRetry = onRetry)
            }

            grupos.isEmpty() -> item(key = "vazio") {
                EmptyState(icon = emptyIcon, texto = emptyText)
            }

            else -> items(items = grupos, key = { it.cantor }) { grupo ->
                CantorAccordion(
                    grupo = grupo,
                    expandido = grupo.cantor in expandidos,
                    onToggle = { onToggle(grupo.cantor) }
                )
            }
        }

        item(key = "footer") {
            footer()
        }
    }
}

@Composable
private fun CantorAccordion(
    grupo: CantorFaixas,
    expandido: Boolean,
    onToggle: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White)
            .border(
                width = 1.dp,
                color = if (expandido) BorderStrong else Border,
                shape = shape
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = grupo.cantor,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "${grupo.musicas.size} faixa(s)",
                color = Muted,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = if (expandido) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = Brand700,
                modifier = Modifier.size(20.dp)
            )
        }

        AnimatedVisibility(
            visible = expandido,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                grupo.musicas.forEach { musica ->
                    SongRow(musica)
                }
            }
        }
    }
}

@Composable
private fun SongRow(musica: Musica) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(SongBackground)
            .padding(start = 12.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .width(52.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Brand100)
                .padding(horizontal = 6.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = musica.codigo.toString(),
                color = Brand700,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = musica.nome,
                color = TextPrimary,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 19.sp
            )
            if (musica.trecho.isNotBlank()) {
                Text(
                    text = musica.trecho,
                    color = Muted,
                    fontSize = 12.5.sp,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 17.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyState(icon: ImageVector, texto: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .dashedBorder(Border, 18.dp)
            .padding(horizontal = 20.dp, vertical = 46.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Brand500,
            modifier = Modifier.size(26.dp)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = texto,
            color = Muted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorCard(mensagem: String, onRetry: (() -> Unit)?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .dashedBorder(Border, 18.dp)
            .padding(horizontal = 20.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.CloudOff,
            contentDescription = null,
            tint = Brand500,
            modifier = Modifier.size(26.dp)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = mensagem,
            color = Muted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        if (onRetry != null) {
            TextButton(onClick = onRetry) {
                Text("Tentar novamente", color = Brand700, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun Modifier.dashedBorder(
    color: Color,
    radius: Dp,
    strokeWidth: Dp = 1.dp
): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        topLeft = Offset.Zero,
        size = Size(size.width, size.height),
        cornerRadius = CornerRadius(radius.toPx()),
        style = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(6.dp.toPx(), 5.dp.toPx()),
                phase = 0f
            )
        )
    )
}

@Composable
fun BotaoTopo(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Brand800, Brand600)))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.ArrowUpward,
            contentDescription = "Voltar ao topo",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}
