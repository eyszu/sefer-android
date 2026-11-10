package app.sefer.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

/**
 * Read-only access to the bundled Bible databases (assets/asv.db, assets/tagalog.db).
 * Each file is copied out of the APK once, then opened with SQLite. Call from a background
 * thread: the first call for a version does file I/O.
 */
class BibleRepository(private val context: Context) {

    private val dbs = HashMap<BibleVersion, SQLiteDatabase>()
    private val counts = HashMap<BibleVersion, Map<Int, Int>>()

    @Synchronized
    private fun db(version: BibleVersion): SQLiteDatabase {
        dbs[version]?.let { return it }
        val file = context.getDatabasePath(version.asset)
        val marker = context.getSharedPreferences("sefer_data", Context.MODE_PRIVATE)
        val markerKey = "copied_${version.key}"
        if (!file.exists() || marker.getInt(markerKey, 0) != DATA_VERSION) {
            file.parentFile?.mkdirs()
            val tmp = File(file.parentFile, version.asset + ".tmp")
            context.assets.open(version.asset).use { input ->
                tmp.outputStream().use { output -> input.copyTo(output) }
            }
            if (file.exists()) file.delete()
            check(tmp.renameTo(file)) { "Could not install ${version.asset}" }
            marker.edit().putInt(markerKey, DATA_VERSION).apply()
        }
        val db = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY)
        dbs[version] = db
        return db
    }

    /** Map of book number -> number of chapters, for the books present in this version. */
    @Synchronized
    fun chapterCounts(version: BibleVersion): Map<Int, Int> {
        counts[version]?.let { return it }
        val result = LinkedHashMap<Int, Int>()
        db(version).rawQuery(
            "SELECT book, MAX(chapter) FROM verses GROUP BY book ORDER BY book", null,
        ).use { c ->
            while (c.moveToNext()) result[c.getInt(0)] = c.getInt(1)
        }
        counts[version] = result
        return result
    }

    fun chapter(version: BibleVersion, book: Int, chapter: Int): List<Verse> {
        val out = ArrayList<Verse>()
        db(version).rawQuery(
            "SELECT verse, text FROM verses WHERE book = ? AND chapter = ? ORDER BY verse",
            arrayOf(book.toString(), chapter.toString()),
        ).use { c ->
            while (c.moveToNext()) out.add(Verse(book, chapter, c.getInt(0), c.getString(1)))
        }
        return out
    }

    fun verse(version: BibleVersion, book: Int, chapter: Int, verse: Int): Verse? {
        db(version).rawQuery(
            "SELECT text FROM verses WHERE book = ? AND chapter = ? AND verse = ?",
            arrayOf(book.toString(), chapter.toString(), verse.toString()),
        ).use { c ->
            return if (c.moveToFirst()) Verse(book, chapter, verse, c.getString(0)) else null
        }
    }

    /** Verses containing every whitespace-separated word of [query] (case-insensitive). */
    fun search(version: BibleVersion, query: String, limit: Int): List<Verse> {
        val terms = query.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (terms.isEmpty()) return emptyList()
        val where = terms.joinToString(" AND ") { "text LIKE ? ESCAPE '\\'" }
        val args = terms.map {
            "%" + it.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%"
        }.toTypedArray()
        val out = ArrayList<Verse>()
        db(version).rawQuery(
            "SELECT book, chapter, verse, text FROM verses WHERE $where " +
                "ORDER BY book, chapter, verse LIMIT $limit",
            args,
        ).use { c ->
            while (c.moveToNext()) {
                out.add(Verse(c.getInt(0), c.getInt(1), c.getInt(2), c.getString(3)))
            }
        }
        return out
    }

    companion object {
        /** Bump when the bundled .db files change so installed copies are refreshed. */
        const val DATA_VERSION = 1
    }
}
