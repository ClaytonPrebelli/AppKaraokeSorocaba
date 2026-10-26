package br.com.prebelli.appkaraokesorocaba.util

/**
 * Compara duas versões no formato 1.2.3 (aceita prefixo "v").
 * Retorna > 0 quando a versão remota é mais nova que a atual.
 */
fun compararVersoes(remota: String, atual: String): Int {
    val numerosRemota = numerosDaVersao(remota)
    val numerosAtual = numerosDaVersao(atual)

    if (numerosRemota.isEmpty() || numerosAtual.isEmpty()) {
        return remota.trim().removePrefix("v").compareTo(atual.trim().removePrefix("v"))
    }

    val tamanho = maxOf(numerosRemota.size, numerosAtual.size)
    for (i in 0 until tamanho) {
        val a = numerosRemota.getOrElse(i) { 0 }
        val b = numerosAtual.getOrElse(i) { 0 }
        if (a != b) return a.compareTo(b)
    }
    return 0
}

fun versaoRemotaMaisNova(remota: String, atual: String): Boolean =
    compararVersoes(remota, atual) > 0

private fun numerosDaVersao(versao: String): List<Int> {
    val limpa = versao.trim().removePrefix("v").removePrefix("V")
    val base = limpa.substringBefore('-').substringBefore('+')
    return base.split('.').mapNotNull { it.trim().toIntOrNull() }
}
