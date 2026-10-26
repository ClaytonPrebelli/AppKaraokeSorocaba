package br.com.prebelli.appkaraokesorocaba.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.prebelli.appkaraokesorocaba.R
import br.com.prebelli.appkaraokesorocaba.ui.theme.HeaderEnd
import br.com.prebelli.appkaraokesorocaba.ui.theme.HeaderMiddle
import br.com.prebelli.appkaraokesorocaba.ui.theme.HeaderStart
import java.util.Locale

@Composable
fun PageHeader(
    titulo: String,
    eyebrow: String,
    onBack: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(HeaderStart, HeaderMiddle, HeaderEnd))
            )
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            descricao = "Voltar",
            onClick = onBack
        )

        Spacer(Modifier.width(10.dp))

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.logo_don),
                contentDescription = "Don Petisco",
                modifier = Modifier
                    .height(34.dp)
                    .widthIn(max = 52.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color.White)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = eyebrow.uppercase(Locale("pt", "BR")),
                    color = Color.White.copy(alpha = 0.68f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.6.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        HeaderIconButton(
            icon = Icons.Filled.Home,
            descricao = "Início",
            onClick = onHome
        )
    }
}

@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    descricao: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.20f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = descricao,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}
