package com.tagangbiblia.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** Bookmarks live in their own database so the Bible data stays untouched. */
class UserStore(context: Context) :
    SQLiteOpenHelper(context, "user.db", null, 1) {

    data class Key(val bookId: Int, val chapter: Int, val verse: Int, val createdAt: Long)

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE bookmarks (
                book_id INTEGER NOT NULL,
                chapter INTEGER NOT NULL,
                verse INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                PRIMARY KEY (book_id, chapter, verse)
            )"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun all(): List<Key> {
        val out = ArrayList<Key>()
        readableDatabase.rawQuery(
            "SELECT book_id, chapter, verse, created_at FROM bookmarks ORDER BY created_at DESC",
            null
        ).use { c ->
            while (c.moveToNext()) out += Key(c.getInt(0), c.getInt(1), c.getInt(2), c.getLong(3))
        }
        return out
    }

    fun add(bookId: Int, chapter: Int, verse: Int) {
        val v = ContentValues().apply {
            put("book_id", bookId); put("chapter", chapter); put("verse", verse)
            put("created_at", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict("bookmarks", null, v, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun remove(bookId: Int, chapter: Int, verse: Int) {
        writableDatabase.delete(
            "bookmarks", "book_id = ? AND chapter = ? AND verse = ?",
            arrayOf(bookId.toString(), chapter.toString(), verse.toString())
        )
    }
}
