package com.tagangbiblia.app.data

data class Book(val id: Int, val name: String) {
    /** "I Samuel" -> "1 Samuel" for display. */
    val displayName: String
        get() = when {
            name.startsWith("III ") -> "3 " + name.removePrefix("III ")
            name.startsWith("II ") -> "2 " + name.removePrefix("II ")
            name.startsWith("I ") -> "1 " + name.removePrefix("I ")
            else -> name
        }
}

data class Verse(val number: Int, val text: String)

data class Bookmark(
    val bookId: Int,
    val chapter: Int,
    val verse: Int,
    val text: String,
    val createdAt: Long,
)
