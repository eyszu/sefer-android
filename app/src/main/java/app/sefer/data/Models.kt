package app.sefer.data

data class Verse(val book: Int, val chapter: Int, val verse: Int, val text: String)

data class Bookmark(val book: Int, val chapter: Int, val verse: Int, val created: Long)

/** A bookmark together with its text in the currently selected version. */
data class BookmarkItem(val bookmark: Bookmark, val text: String)

data class Reference(val book: Int, val chapter: Int, val verse: Int?)

fun bookmarkKey(book: Int, chapter: Int, verse: Int): Long =
    book * 1_000_000L + chapter * 1_000L + verse

enum class BibleVersion(
    val key: String,
    val asset: String,
    val language: String,
    val abbr: String,
    val fullName: String,
) {
    TAGALOG("tagalog", "tagalog.db", "Tagalog", "TAG", "Ang Biblia (1905)"),
    ENGLISH("asv", "asv.db", "English", "ASV", "American Standard Version (1901)");

    companion object {
        fun fromKey(key: String?): BibleVersion = entries.firstOrNull { it.key == key } ?: TAGALOG
    }
}
