package br.com.prebelli.appkaraokesorocaba.util

import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.abrirLink(url: String) {
    runCatching {
        startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
