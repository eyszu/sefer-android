package app.sefer

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.sefer.data.BibleRepository
import app.sefer.data.BibleVersion
import app.sefer.data.BookNames
import app.sefer.data.BookmarkItem
import app.sefer.data.BookmarkStore
import app.sefer.data.Reference
import app.sefer.data.ReferenceParser
import app.sefer.data.Verse
import app.sefer.data.bookmarkKey
import app.sefer.ui.SeferFont
import app.sefer.ui.SeferPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Tab { READ, SEARCH, BOOKMARKS }

/** Asks the reader to scroll: to [verse] if given, otherwise to the top of the chapter. */
data class ScrollTarget(val id: Int, val verse: Int?)

class SeferViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = BibleRepository(app)
    private val bookmarkStore = BookmarkStore(app)
    private val prefs = app.getSharedPreferences("sefer_prefs", Context.MODE_PRIVATE)

    // ---- Settings (persisted) -------------------------------------------------------------
    private val systemIsDark =
        (app.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

    var palette by mutableStateOf(
        SeferPalette.fromKey(prefs.getString("theme", null))
            ?: if (systemIsDark) SeferPalette.DARK else SeferPalette.LIGHT,
    )
        private set
    var font by mutableStateOf(SeferFont.fromKey(prefs.getString("font", null)))
        private set
    var fontSize by mutableIntStateOf(prefs.getInt("font_size", DEFAULT_FONT_SIZE))
        private set
    /** Line height as a percentage of the font size (165 = 1.65x). */
    var lineSpacing by mutableIntStateOf(prefs.getInt("line_spacing", DEFAULT_LINE_SPACING))
        private set
    var version by mutableStateOf(BibleVersion.fromKey(prefs.getString("version", null)))
        private set

    fun selectPalette(p: SeferPalette) { palette = p; prefs.edit().putString("theme", p.key).apply() }
    fun selectFont(f: SeferFont) { font = f; prefs.edit().putString("font", f.key).apply() }
    fun selectFontSize(size: Int) {
        fontSize = size.coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE)
        prefs.edit().putInt("font_size", fontSize).apply()
    }

    fun selectLineSpacing(percent: Int) {
        lineSpacing = (percent / 5 * 5).coerceIn(MIN_LINE_SPACING, MAX_LINE_SPACING)
        prefs.edit().putInt("line_spacing", lineSpacing).apply()
    }

    fun selectVersion(v: BibleVersion) {
        if (v == version) return
        version = v
        prefs.edit().putString("version", v.key).apply()
        loadChapter(verse = null, resetScroll = false)
        refreshBookmarks()
        runSearch(immediate = true)
    }

    // ---- Navigation state -----------------------------------------------------------------
    var ready by mutableStateOf(false); private set
    var tab by mutableStateOf(Tab.READ)
    var showPicker by mutableStateOf(false)
    var showAppearance by mutableStateOf(false)

    var chapterCounts by mutableStateOf<Map<Int, Int>>(emptyMap()); private set
    val bookIds: List<Int> get() = chapterCounts.keys.sorted()

    var book by mutableIntStateOf(prefs.getInt("book", 1)); private set
    var chapter by mutableIntStateOf(prefs.getInt("chapter", 1)); private set
    var verses by mutableStateOf<List<Verse>>(emptyList()); private set
    var scrollTarget by mutableStateOf<ScrollTarget?>(null); private set
    var selectedVerse by mutableStateOf<Int?>(null)

    private var scrollCounter = 0
    private var chapterJob: Job? = null

    init {
        viewModelScope.launch {
            val counts = withContext(Dispatchers.IO) { repo.chapterCounts(version) }
            chapterCounts = counts
            if (book !in counts) { book = counts.keys.minOrNull() ?: 1; chapter = 1 }
            chapter = chapter.coerceIn(1, counts[book] ?: 1)
            ready = true
            loadChapter(verse = null, resetScroll = true)
            refreshBookmarks()
        }
    }

    fun bookName(b: Int = book): String = BookNames.name(version, b)

    fun openChapter(b: Int, c: Int, verse: Int? = null) {
        book = b
        chapter = c
        selectedVerse = verse
        tab = Tab.READ
        showPicker = false
        prefs.edit().putInt("book", b).putInt("chapter", c).apply()
        loadChapter(verse, resetScroll = true)
    }

    fun open(ref: Reference) = openChapter(ref.book, ref.chapter, ref.verse)

    private fun loadChapter(verse: Int?, resetScroll: Boolean) {
        chapterJob?.cancel()
        val v = version
        val b = book
        val c = chapter
        chapterJob = viewModelScope.launch {
            val list = withContext(Dispatchers.IO) { repo.chapter(v, b, c) }
            verses = list
            if (resetScroll) scrollTarget = ScrollTarget(++scrollCounter, verse)
        }
    }

    private fun neighbour(delta: Int): Pair<Int, Int>? {
        val ids = bookIds
        val idx = ids.indexOf(book)
        if (idx < 0) return null
        val max = chapterCounts[book] ?: return null
        return if (delta > 0) {
            if (chapter < max) book to chapter + 1
            else ids.getOrNull(idx + 1)?.let { it to 1 }
        } else {
            if (chapter > 1) book to chapter - 1
            else ids.getOrNull(idx - 1)?.let { it to (chapterCounts[it] ?: 1) }
        }
    }

    val nextChapterRef: Pair<Int, Int>? get() = neighbour(+1)
    val previousChapterRef: Pair<Int, Int>? get() = neighbour(-1)

    // ---- Bookmarks ------------------------------------------------------------------------
    var bookmarkItems by mutableStateOf<List<BookmarkItem>>(emptyList()); private set
    var bookmarkKeys by mutableStateOf<Set<Long>>(emptySet()); private set

    fun isBookmarked(b: Int, c: Int, v: Int) = bookmarkKey(b, c, v) in bookmarkKeys

    fun toggleBookmark(b: Int, c: Int, v: Int) {
        val exists = isBookmarked(b, c, v)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (exists) bookmarkStore.remove(b, c, v) else bookmarkStore.add(b, c, v)
            }
            refreshBookmarks()
        }
    }

    private fun refreshBookmarks() {
        val v = version
        viewModelScope.launch {
            val items = withContext(Dispatchers.IO) {
                bookmarkStore.all().map { bm ->
                    BookmarkItem(bm, repo.verse(v, bm.book, bm.chapter, bm.verse)?.text.orEmpty())
                }
            }
            bookmarkItems = items
            bookmarkKeys = items.map { bookmarkKey(it.bookmark.book, it.bookmark.chapter, it.bookmark.verse) }.toSet()
        }
    }

    // ---- Search ---------------------------------------------------------------------------
    var query by mutableStateOf(""); private set
    var results by mutableStateOf<List<Verse>>(emptyList()); private set
    /** The query [results] belong to (used for highlighting). */
    var resultQuery by mutableStateOf(""); private set
    var searching by mutableStateOf(false); private set
    private var searchJob: Job? = null

    fun onQueryChange(q: String) {
        query = q
        runSearch(immediate = false)
    }

    /** "John 3:16" style jump target for the current query, if it parses as a reference. */
    fun referenceFor(q: String): Reference? = ReferenceParser.parse(q, chapterCounts)

    private fun runSearch(immediate: Boolean) {
        searchJob?.cancel()
        val q = query.trim()
        if (q.length < MIN_QUERY_LENGTH) {
            results = emptyList(); resultQuery = ""; searching = false
            return
        }
        val v = version
        searchJob = viewModelScope.launch {
            searching = true
            if (!immediate) delay(250)
            val found = withContext(Dispatchers.IO) { repo.search(v, q, MAX_RESULTS) }
            results = found
            resultQuery = q
            searching = false
        }
    }

    companion object {
        const val MIN_FONT_SIZE = 12
        const val MAX_FONT_SIZE = 32
        const val DEFAULT_FONT_SIZE = 18
        const val MIN_LINE_SPACING = 120
        const val MAX_LINE_SPACING = 220
        const val DEFAULT_LINE_SPACING = 165
        const val MIN_QUERY_LENGTH = 2
        const val MAX_RESULTS = 500
    }
}
