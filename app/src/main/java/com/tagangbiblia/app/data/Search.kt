package com.tagangbiblia.app.data

import java.text.Normalizer

data class SearchHit(val bookId: Int, val chapter: Int, val verse: Int, val text: String)

/**
 * Lower-cases and strips accents one character at a time, so the result has the
 * same length as the input and match positions can be mapped back to the original.
 */
fun fold(s: String): String {
    val sb = StringBuilder(s.length)
    for (ch in s) {
        val base = if (ch.code < 128) ch else
            Normalizer.normalize(ch.toString(), Normalizer.Form.NFD).firstOrNull() ?: ch
        sb.append(base.lowercaseChar())
    }
    return sb.toString()
}

fun searchTerms(query: String): List<String> =
    fold(query).split(Regex("\\s+")).filter { it.isNotEmpty() }

/** In-memory index of every verse; built once, on first search. */
class SearchIndex(rows: List<SearchHit>) {
    private val rows = rows
    private val folded = rows.map { fold(it.text) }

    /** Verses containing every term (any order). Returns (first [limit] hits, total count). */
    fun search(query: String, limit: Int): Pair<List<SearchHit>, Int> {
        val terms = searchTerms(query)
        if (terms.isEmpty()) return emptyList<SearchHit>() to 0
        val hits = ArrayList<SearchHit>()
        var total = 0
        for (i in rows.indices) {
            val f = folded[i]
            if (terms.all { f.contains(it) }) {
                total++
                if (hits.size < limit) hits += rows[i]
            }
        }
        return hits to total
    }
}
