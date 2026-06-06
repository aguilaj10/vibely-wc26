package com.vibely.wc26.core.util

import java.text.Normalizer

/**
 * Text normalization + fuzzy matching utilities used by Search and the OCR-driven
 * Scan flow. Pure JVM, no Android imports.
 */

private val DIACRITICS = Regex("\\p{Mn}+")

/** Strip combining marks (accents) — "Malagón" → "Malagon". */
fun stripAccents(s: String): String =
    Normalizer.normalize(s, Normalizer.Form.NFD).replace(DIACRITICS, "")

/**
 * Canonical form for fuzzy comparison: NFD-stripped, uppercase, single-space.
 * "Luis Malagón " → "LUIS MALAGON".
 */
fun normalize(s: String): String =
    stripAccents(s).uppercase().replace(Regex("\\s+"), " ").trim()

/**
 * Jaro similarity (0..1). 1 means identical.
 * Implementation follows the standard definition; tolerant of transpositions and
 * sub-string drift — good for OCR-mangled player names.
 */
fun jaroSimilarity(a: String, b: String): Double {
    if (a == b) return 1.0
    if (a.isEmpty() || b.isEmpty()) return 0.0

    val matchDistance = (maxOf(a.length, b.length) / 2) - 1
    val aMatched = BooleanArray(a.length)
    val bMatched = BooleanArray(b.length)
    var matches = 0

    for (i in a.indices) {
        val start = maxOf(0, i - matchDistance)
        val end = minOf(i + matchDistance + 1, b.length)
        for (j in start until end) {
            if (bMatched[j] || a[i] != b[j]) continue
            aMatched[i] = true
            bMatched[j] = true
            matches++
            break
        }
    }
    if (matches == 0) return 0.0

    var transpositions = 0
    var k = 0
    for (i in a.indices) {
        if (!aMatched[i]) continue
        while (!bMatched[k]) k++
        if (a[i] != b[k]) transpositions++
        k++
    }

    val m = matches.toDouble()
    return (m / a.length + m / b.length + (m - transpositions / 2.0) / m) / 3.0
}

/**
 * Jaro-Winkler similarity (0..1). Boosts matches that share a common prefix —
 * useful because OCR typically captures the start of names cleanly.
 */
fun jaroWinkler(a: String, b: String, prefixScale: Double = 0.1, maxPrefix: Int = 4): Double {
    val jaro = jaroSimilarity(a, b)
    var prefix = 0
    val limit = minOf(maxPrefix, minOf(a.length, b.length))
    while (prefix < limit && a[prefix] == b[prefix]) prefix++
    return jaro + prefix * prefixScale * (1.0 - jaro)
}
