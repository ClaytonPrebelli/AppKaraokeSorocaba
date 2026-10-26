package br.com.prebelli.appkaraokesorocaba.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import br.com.prebelli.appkaraokesorocaba.ui.theme.Border
import br.com.prebelli.appkaraokesorocaba.ui.theme.Brand500
import br.com.prebelli.appkaraokesorocaba.ui.theme.Brand600
import br.com.prebelli.appkaraokesorocaba.ui.theme.Brand700
import br.com.prebelli.appkaraokesorocaba.ui.theme.DisabledBackground
import br.com.prebelli.appkaraokesorocaba.ui.theme.DisabledText
import br.com.prebelli.appkaraokesorocaba.ui.theme.Muted
import br.com.prebelli.appkaraokesorocaba.ui.theme.MutedLight
import br.com.prebelli.appkaraokesorocaba.ui.theme.TextPrimary

@Composable
fun SearchForm(
    termo: String,
    onTermoChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = termo.trim().length >= 4
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val horizontal = maxWidth >= 560.dp

        if (horizontal) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CampoBusca(
                    termo = termo,
                    onTermoChange = onTermoChange,
                    onSearch = onSearch,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(10.dp))
                BotaoBuscar(
                    habilitado = habilitado,
                    onClick = onSearch,
                    modifier = Modifier.width(132.dp)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CampoBusca(
                    termo = termo,
                    onTermoChange = onTermoChange,
                    onSearch = onSearch,
                    modifier = Modifier.fillMaxWidth()
                )
                BotaoBuscar(
                    habilitado = habilitado,
                    onClick = onSearch,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun CampoBusca(
    termo: String,
    onTermoChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = Muted,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = termo,
            onValueChange = onTermoChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
            cursorBrush = SolidColor(Brand700),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            modifier = Modifier.weight(1f),
            decorationBox = { conteudoInterno ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (termo.isEmpty()) {
                        Text(
                            text = "Música ou cantor...",
                            color = MutedLight,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    conteudoInterno()
                }
            }
        )
    }
}

@Composable
private fun BotaoBuscar(
    habilitado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(shape)
            .background(
                brush = if (habilitado) {
                    Brush.linearGradient(listOf(Brand600, Brand500))
                } else {
                    SolidColor(DisabledBackground)
                },
                shape = shape
            )
            .clickable(
                enabled = habilitado,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Buscar",
            color = if (habilitado) Color.White else DisabledText,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
