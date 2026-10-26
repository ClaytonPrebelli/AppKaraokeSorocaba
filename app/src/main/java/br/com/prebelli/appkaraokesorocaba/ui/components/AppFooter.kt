package br.com.prebelli.appkaraokesorocaba.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.prebelli.appkaraokesorocaba.BuildConfig
import br.com.prebelli.appkaraokesorocaba.ui.theme.Brand700
import br.com.prebelli.appkaraokesorocaba.ui.theme.Muted
import br.com.prebelli.appkaraokesorocaba.util.abrirLink

@Composable
fun AppFooter(
    onVerificarAtualizacao: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row {
            Text(
                text = "Desenvolvido por ",
                color = Muted,
                fontSize = 12.sp
            )
            Text(
                text = "Clayton Prebelli",
                color = Brand700,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    context.abrirLink(
                        "https://wa.me/5515981746903?text=vi%20seu%20sistema%20de%20karaoke"
                    )
                }
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Versão ${BuildConfig.VERSION_NAME}",
            color = Muted,
            fontSize = 11.sp
        )
        TextButton(onClick = onVerificarAtualizacao) {
            Text(
                text = "Verificar atualização",
                fontSize = 11.5.sp,
                color = Brand700,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
