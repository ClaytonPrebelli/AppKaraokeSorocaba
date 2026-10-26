package br.com.prebelli.appkaraokesorocaba.data

import androidx.annotation.DrawableRes
import br.com.prebelli.appkaraokesorocaba.R

data class Categoria(
    val key: String,
    val titulo: String,
    val hint: String,
    @DrawableRes val bandeira: Int,
    val filtraPorLetra: Boolean
)

val CATEGORIAS = listOf(
    Categoria("BRA", "Nacionais", "MPB, sertanejo e mais", R.drawable.bandeira_nacional, true),
    Categoria("ING", "Inglês", "Pop, rock e clássicos", R.drawable.bandeira_ingles, true),
    Categoria("ITA", "Italiano", "Canções italianas", R.drawable.bandeira_italiano, false),
    Categoria("ESP", "Espanhol", "Latinas e românticas", R.drawable.bandeira_espanhol, false),
    Categoria("GOS", "Gospel", "Louvor e adoração", R.drawable.bandeira_gospel, false),
    Categoria("INF", "Infantil", "Para a criançada", R.drawable.bandeira_infantil, false)
)

fun categoriaPorKey(key: String?): Categoria =
    CATEGORIAS.firstOrNull { it.key == key } ?: CATEGORIAS.first()

val ALFABETO: List<String> = ('A'..'Z').map { it.toString() } + "#"
