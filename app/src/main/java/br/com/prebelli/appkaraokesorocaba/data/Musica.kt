package br.com.prebelli.appkaraokesorocaba.data

import org.json.JSONArray
import java.text.Collator
import java.util.Locale

data class Musica(
    val id: Long,
    val nome: String,
    val cantor: String,
    val trecho: String,
    val codigo: Int,
    val categoria: String
)

data class CantorFaixas(
    val cantor: String,
    val musicas: List<Musica>
)

fun parseMusicas(json: String): List<Musica> {
    val array = JSONArray(json)
    val musicas = ArrayList<Musica>(array.length())
    for (i in 0 until array.length()) {
        val item = array.optJSONObject(i) ?: continue
        val cantor = item.optString("cantor").trim()
        if (cantor.isEmpty() && item.optString("nome").isEmpty()) continue
        musicas += Musica(
            id = item.optLong("id"),
            nome = item.optString("nome"),
            cantor = cantor.ifEmpty { "Sem cantor" },
            trecho = item.optString("trecho"),
            codigo = item.optInt("codigo"),
            categoria = item.optString("categoria")
        )
    }
    return musicas
}

fun agruparPorCantor(musicas: List<Musica>): List<CantorFaixas> {
    val collator = Collator.getInstance(Locale("pt", "BR")).apply {
        strength = Collator.PRIMARY
    }
    return musicas
        .sortedWith { a, b -> collator.compare(a.cantor, b.cantor) }
        .groupBy { it.cantor }
        .map { (cantor, faixas) -> CantorFaixas(cantor, faixas) }
}
