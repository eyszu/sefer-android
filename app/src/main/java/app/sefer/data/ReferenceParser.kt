package app.sefer.data

/**
 * Turns things like "Daniel 7:25", "1 sam 3", "Mga Awit 23" or "gen" into a [Reference].
 * Matches English and Tagalog book names, full or abbreviated.
 */
object ReferenceParser {
    private val tail = Regex("""^(.*?)[\s.]*(\d+)\s*(?:[:.]\s*(\d+))?$""")
    private val roman = Regex("""^(iii|ii|i)\.?\s+""")

    private val aliases = mapOf(
        "gn" to 1, "ex" to 2, "lv" to 3, "nm" to 4, "dt" to 5, "jb" to 18, "ps" to 19,
        "pss" to 19, "pr" to 20, "ec" to 21, "is" to 23, "dn" to 27, "am" to 30, "ob" to 31,
        "mi" to 33, "na" to 34,
    )

    private val normalizedNames: List<List<String>> by lazy {
        (1..66).map { b ->
            listOf(BookNames.english[b - 1], BookNames.tagalog[b - 1]).map(::normalize).distinct()
        }
    }

    private fun normalize(s: String): String =
        s.lowercase().filter { it in 'a'..'z' || it in '0'..'9' }

    private fun romanToDigit(s: String): String =
        roman.replace(s) { m ->
            when (m.groupValues[1]) {
                "i" -> "1 "
                "ii" -> "2 "
                else -> "3 "
            }
        }

    fun parse(input: String, chapterCounts: Map<Int, Int>): Reference? {
        val q = input.trim()
        if (q.isEmpty() || chapterCounts.isEmpty()) return null

        var chapter = 1
        var verse: Int? = null
        val namePart: String
        val m = tail.matchEntire(q)
        if (m != null && m.groupValues[1].isNotBlank()) {
            namePart = m.groupValues[1]
            chapter = m.groupValues[2].toIntOrNull() ?: return null
            verse = m.groupValues[3].toIntOrNull()
        } else {
            namePart = q
        }

        val key = normalize(romanToDigit(namePart.trim().lowercase()))
        if (key.length < 2) return null
        val book = findBook(key, chapterCounts.keys) ?: return null
        val max = chapterCounts[book] ?: return null
        if (chapter !in 1..max) return null
        if (verse != null && verse < 1) verse = null
        return Reference(book, chapter, verse)
    }

    private fun findBook(key: String, available: Set<Int>): Int? {
        val books = available.filter { it in 1..66 }.sorted()
        books.firstOrNull { b -> normalizedNames[b - 1].any { it == key } }?.let { return it }
        aliases[key]?.takeIf { it in available }?.let { return it }
        if (key.length >= 3) {
            books.firstOrNull { b -> normalizedNames[b - 1].any { it.startsWith(key) } }
                ?.let { return it }
        }
        return null
    }
}
