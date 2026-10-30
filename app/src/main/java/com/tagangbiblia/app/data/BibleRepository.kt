package com.tagangbiblia.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.FileOutputStream

/**
 * Read access to the bundled Bible SQLite database.
 * The asset is copied to app storage on first launch (or when DB_VERSION changes).
 */
class BibleRepository(context: Context) {

    private val db: SQLiteDatabase

    init {
        val prefs = context.getSharedPreferences("db_meta", Context.MODE_PRIVATE)
        val file = context.getDatabasePath(DB_NAME)
        if (!file.exists() || prefs.getInt("db_version", 0) != DB_VERSION) {
            file.parentFile?.mkdirs()
            val tmp = File(file.parentFile, "$DB_NAME.tmp")
            context.assets.open(DB_NAME).use { input ->
                FileOutputStream(tmp).use { output -> input.copyTo(output) }
            }
            if (file.exists()) file.delete()
            check(tmp.renameTo(file)) { "Could not install Bible database" }
            prefs.edit().putInt("db_version", DB_VERSION).apply()
        }
        db = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READWRITE)
        // The source DB has no lookup index; add one so chapter loads are instant.
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_verses_lookup " +
                "ON TagAngBiblia_verses(book_id, chapter, verse)"
        )
    }

    fun books(): List<Book> {
        val out = ArrayList<Book>()
        db.rawQuery("SELECT id, name FROM TagAngBiblia_books ORDER BY id", null).use { c ->
            while (c.moveToNext()) out += Book(c.getInt(0), c.getString(1))
        }
        return out
    }

    /** bookId -> number of chapters */
    fun chapterCounts(): Map<Int, Int> {
        val out = HashMap<Int, Int>()
        db.rawQuery(
            "SELECT book_id, MAX(chapter) FROM TagAngBiblia_verses GROUP BY book_id", null
        ).use { c ->
            while (c.moveToNext()) out[c.getInt(0)] = c.getInt(1)
        }
        return out
    }

    fun verses(bookId: Int, chapter: Int): List<Verse> {
        val out = ArrayList<Verse>()
        db.rawQuery(
            "SELECT verse, text FROM TagAngBiblia_verses " +
                "WHERE book_id = ? AND chapter = ? ORDER BY verse",
            arrayOf(bookId.toString(), chapter.toString())
        ).use { c ->
            while (c.moveToNext()) out += Verse(c.getInt(0), c.getString(1).trim())
        }
        return out
    }

    fun allVerses(): List<SearchHit> {
        val out = ArrayList<SearchHit>(24000)
        db.rawQuery(
            "SELECT book_id, chapter, verse, text FROM TagAngBiblia_verses " +
                "ORDER BY book_id, chapter, verse", null
        ).use { c ->
            while (c.moveToNext()) out += SearchHit(c.getInt(0), c.getInt(1), c.getInt(2), c.getString(3).trim())
        }
        return out
    }

    fun verseText(bookId: Int, chapter: Int, verse: Int): String? =
        db.rawQuery(
            "SELECT text FROM TagAngBiblia_verses WHERE book_id = ? AND chapter = ? AND verse = ? LIMIT 1",
            arrayOf(bookId.toString(), chapter.toString(), verse.toString())
        ).use { c -> if (c.moveToFirst()) c.getString(0).trim() else null }

    companion object {
        const val DB_NAME = "TagAngBiblia.db"
        /** Bump when the bundled asset changes. */
        const val DB_VERSION = 1
    }
}
