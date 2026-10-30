package com.tagangbiblia.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tagangbiblia.app.data.BibleRepository
import com.tagangbiblia.app.data.Book
import com.tagangbiblia.app.data.Bookmark
import com.tagangbiblia.app.data.UserStore
import com.tagangbiblia.app.data.SearchHit
import com.tagangbiblia.app.data.SearchIndex
import com.tagangbiblia.app.data.Verse
import com.tagangbiblia.app.data.fold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Screen { Reader, Books, Chapters, Bookmarks, Search }
enum class ThemeMode { SYSTEM, LIGHT, DARK }

class BibleViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("settings", 0)
    private val userStore = UserStore(app)
    private var repo: BibleRepository? = null

    // ---- loading ----
    var ready by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set

    // ---- data ----
    var books by mutableStateOf<List<Book>>(emptyList()); private set
    var chapterCounts by mutableStateOf<Map<Int, Int>>(emptyMap()); private set
    var verses by mutableStateOf<List<Verse>>(emptyList()); private set
    var bookmarks by mutableStateOf<List<Bookmark>>(emptyList()); private set
    /** Increments every time a chapter finishes loading (lets the UI react even to a reload of the same chapter). */
    var loadId by mutableIntStateOf(0); private set

    // ---- navigation ----
    var screen by mutableStateOf(Screen.Reader)
    var bookId by mutableIntStateOf(prefs.getInt("book", 1)); private set
    var chapter by mutableIntStateOf(prefs.getInt("chapter", 1)); private set
    /** Verse to scroll to after the next chapter load (e.g. from a bookmark). */
    var scrollToVerse by mutableStateOf<Int?>(null)
    /** Book chosen in the picker, while choosing a chapter. */
    var pickerBook by mutableStateOf<Book?>(null)

    // ---- selection ----
    var selected by mutableStateOf<Set<Int>>(emptySet()); private set

    // ---- settings ----
    var fontSize by mutableFloatStateOf(prefs.getFloat("font_size", 20f)); private set
    var themeMode by mutableStateOf(
        runCatching { ThemeMode.valueOf(prefs.getString("theme", "SYSTEM")!!) }
            .getOrDefault(ThemeMode.SYSTEM)
    ); private set

    val currentBook: Book? get() = books.firstOrNull { it.id == bookId }
    val currentTitle: String get() = currentBook?.let { "${it.displayName} $chapter" } ?: ""

    private class Loaded(
        val repo: BibleRepository,
        val books: List<Book>,
        val counts: Map<Int, Int>,
        val book: Int,
        val chapter: Int,
        val verses: List<Verse>,
        val bookmarks: List<Bookmark>,
    )

    init {
        viewModelScope.launch {
            try {
                // Heavy work off the main thread...
                val l = withContext(Dispatchers.IO) {
                    val r = BibleRepository(getApplication())
                    val b = r.books()
                    val counts = r.chapterCounts()
                    var bk = bookId; var ch = chapter
                    if (b.none { it.id == bk } || ch !in 1..(counts[bk] ?: 0)) {
                        bk = b.first().id; ch = 1
                    }
                    Loaded(r, b, counts, bk, ch, r.verses(bk, ch), loadBookmarks(r))
                }
                // ...but publish all UI state on the main thread.
                repo = l.repo
                books = l.books
                chapterCounts = l.counts
                bookId = l.book
                chapter = l.chapter
                verses = l.verses
                bookmarks = l.bookmarks
                loadId++
                ready = true
            } catch (t: Throwable) {
                error = t.message ?: t.toString()
            }
        }
    }

    // ---------- navigation ----------
    fun openChapter(book: Int, ch: Int, verse: Int? = null) {
        val r = repo ?: return
        selected = emptySet()
        scrollToVerse = verse
        bookId = book
        chapter = ch
        screen = Screen.Reader
        prefs.edit().putInt("book", book).putInt("chapter", ch).apply()
        viewModelScope.launch {
            val v = withContext(Dispatchers.IO) { r.verses(book, ch) }
            if (book == bookId && ch == chapter) {   // ignore stale loads
                verses = v
                loadId++
            }
        }
    }

    fun hasNext(): Boolean {
        val last = chapterCounts[bookId] ?: return false
        return chapter < last || books.any { it.id > bookId }
    }

    fun hasPrev(): Boolean = chapter > 1 || books.any { it.id < bookId }

    fun nextChapter() {
        val last = chapterCounts[bookId] ?: return
        if (chapter < last) openChapter(bookId, chapter + 1)
        else books.firstOrNull { it.id > bookId }?.let { openChapter(it.id, 1) }
    }

    fun prevChapter() {
        if (chapter > 1) openChapter(bookId, chapter - 1)
        else books.lastOrNull { it.id < bookId }?.let {
            openChapter(it.id, chapterCounts[it.id] ?: 1)
        }
    }

    // ---------- selection ----------
    fun toggleSelect(verse: Int) {
        selected = if (verse in selected) selected - verse else selected + verse
    }

    fun clearSelection() { selected = emptySet() }

    private fun selectedVerses(): List<Verse> =
        verses.filter { it.number in selected }.sortedBy { it.number }

    /** Formats the selected verses for the clipboard / share sheet. */
    fun selectionAsText(): String {
        val sel = selectedVerses()
        if (sel.isEmpty()) return ""
        val name = currentBook?.displayName ?: ""
        val ref = "$name $chapter:${compactRanges(sel.map { it.number })}"
        val body = if (sel.size == 1) "“${sel[0].text}”"
        else sel.joinToString("\n") { "${it.number} ${it.text}" }
        return "$ref\n$body\n— Ang Biblia (1905)"
    }

    private fun compactRanges(nums: List<Int>): String {
        val parts = ArrayList<String>()
        var start = nums[0]; var prev = nums[0]
        fun flush() { parts += if (start == prev) "$start" else "$start-$prev" }
        for (n in nums.drop(1)) {
            if (n == prev + 1) prev = n else { flush(); start = n; prev = n }
        }
        flush()
        return parts.joinToString(",")
    }

    // ---------- search ----------
    var searchQuery by mutableStateOf(""); private set
    var searchResults by mutableStateOf<List<SearchHit>>(emptyList()); private set
    var searchTotal by mutableIntStateOf(0); private set
    var searching by mutableStateOf(false); private set
    private var searchIndex: SearchIndex? = null
    private var searchJob: Job? = null

    fun updateSearch(q: String) {
        searchQuery = q
        searchJob?.cancel()
        if (q.trim().length < 2) {
            searchResults = emptyList(); searchTotal = 0; searching = false
            return
        }
        searching = true
        searchJob = viewModelScope.launch {
            delay(200) // debounce typing
            val r = repo ?: return@launch
            val (hits, total) = withContext(Dispatchers.IO) {
                val idx = searchIndex ?: SearchIndex(r.allVerses()).also { searchIndex = it }
                idx.search(q, MAX_RESULTS)
            }
            searchResults = hits
            searchTotal = total
            searching = false
        }
    }

    /** "Genesis 1", "gen 1:5", "1 samuel 3:10" -> (book, chapter, verse?) */
    fun referenceMatch(q: String): Triple<Book, Int, Int?>? {
        val m = Regex("^(.+?)\\s+(\\d+)(?:\\s*:\\s*(\\d+))?$").find(q.trim()) ?: return null
        val name = fold(m.groupValues[1]).replace(Regex("\\s+"), " ")
        val ch = m.groupValues[2].toIntOrNull() ?: return null
        val vs = m.groupValues[3].toIntOrNull()
        val candidates = books.filter {
            fold(it.displayName).startsWith(name) || fold(it.name).startsWith(name)
        }
        val book = candidates.firstOrNull { fold(it.displayName) == name } ?: candidates.firstOrNull()
            ?: return null
        if (ch !in 1..(chapterCounts[book.id] ?: 0)) return null
        return Triple(book, ch, vs)
    }

    // ---------- bookmarks ----------
    fun isBookmarked(verse: Int) =
        bookmarks.any { it.bookId == bookId && it.chapter == chapter && it.verse == verse }

    val selectionAllBookmarked: Boolean
        get() = selected.isNotEmpty() && selected.all { isBookmarked(it) }

    fun toggleBookmarkOnSelection() {
        val remove = selectionAllBookmarked
        val b = bookId; val c = chapter; val sel = selected.toList()
        selected = emptySet()
        viewModelScope.launch {
            bookmarks = withContext(Dispatchers.IO) {
                sel.forEach { if (remove) userStore.remove(b, c, it) else userStore.add(b, c, it) }
                loadBookmarks(repo!!)
            }
        }
    }

    fun removeBookmark(bm: Bookmark) {
        viewModelScope.launch {
            bookmarks = withContext(Dispatchers.IO) {
                userStore.remove(bm.bookId, bm.chapter, bm.verse)
                loadBookmarks(repo!!)
            }
        }
    }

    private fun loadBookmarks(r: BibleRepository): List<Bookmark> =
        userStore.all().mapNotNull { k ->
            r.verseText(k.bookId, k.chapter, k.verse)?.let {
                Bookmark(k.bookId, k.chapter, k.verse, it, k.createdAt)
            }
        }

    fun bookName(id: Int) = books.firstOrNull { it.id == id }?.displayName ?: ""

    // ---------- settings ----------
    fun updateFontSize(sp: Float) {
        fontSize = sp.coerceIn(MIN_FONT, MAX_FONT)
        prefs.edit().putFloat("font_size", fontSize).apply()
    }

    fun updateTheme(mode: ThemeMode) {
        themeMode = mode
        prefs.edit().putString("theme", mode.name).apply()
    }

    companion object {
        const val MIN_FONT = 14f
        const val MAX_FONT = 40f
        const val MAX_RESULTS = 300
    }
}
