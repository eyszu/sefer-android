package app.sefer.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Bookmarks are stored by reference (book/chapter/verse), not by version, so a bookmark made
 * while reading Tagalog also shows when switching to English.
 */
class BookmarkStore(context: Context) : SQLiteOpenHelper(context, "sefer_user.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE bookmarks (" +
                "book INTEGER NOT NULL, chapter INTEGER NOT NULL, verse INTEGER NOT NULL, " +
                "created INTEGER NOT NULL, PRIMARY KEY (book, chapter, verse))",
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun all(): List<Bookmark> {
        val out = ArrayList<Bookmark>()
        readableDatabase.rawQuery(
            "SELECT book, chapter, verse, created FROM bookmarks ORDER BY created DESC", null,
        ).use { c ->
            while (c.moveToNext()) {
                out.add(Bookmark(c.getInt(0), c.getInt(1), c.getInt(2), c.getLong(3)))
            }
        }
        return out
    }

    fun add(book: Int, chapter: Int, verse: Int) {
        val values = ContentValues().apply {
            put("book", book)
            put("chapter", chapter)
            put("verse", verse)
            put("created", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict("bookmarks", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun remove(book: Int, chapter: Int, verse: Int) {
        writableDatabase.delete(
            "bookmarks", "book = ? AND chapter = ? AND verse = ?",
            arrayOf(book.toString(), chapter.toString(), verse.toString()),
        )
    }
}
